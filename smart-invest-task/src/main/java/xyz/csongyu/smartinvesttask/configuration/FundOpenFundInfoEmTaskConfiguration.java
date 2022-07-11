package xyz.csongyu.smartinvesttask.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;

import lombok.extern.slf4j.Slf4j;
import xyz.csongyu.smartinvesttask.dao.FundNameEmDao;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "task.name", havingValue = "fund-open-fund-info-em")
public class FundOpenFundInfoEmTaskConfiguration {
    private final FundNameEmDao fundNameEmDao;

    private final CloseableHttpClient closeableHttpClient;

    @Value("${task.name}")
    private String taskName;

    @Value("${task.module.name}")
    private String moduleName;

    @Value("${task.fund-open-fund-info-em.input.url}")
    private String inputUrl;

    @Value("${task.fund-open-fund-info-em.output.path}")
    private String outputPath;

    @Value("${task.fund-open-fund-info-em.output.subpath}")
    private String outputSubpath;

    @Value("${task.fund-open-fund-info-em.output.file}")
    private String outputFile;

    @Value("${task.fund-open-fund-info-em.current-threads}")
    private int currentThreads;

    @Value("${task.fund-open-fund-info-em.timeout}")
    private long timeout;

    public FundOpenFundInfoEmTaskConfiguration(final FundNameEmDao fundNameEmDao,
        final CloseableHttpClient closeableHttpClient) {
        this.fundNameEmDao = fundNameEmDao;
        this.closeableHttpClient = closeableHttpClient;
    }

    @Bean
    @ConditionalOnProperty(value = "task.module.name", havingValue = "unit-net-asset-value")
    public CommandLineRunner unitNetAssetValueRunner() {
        return args -> {
            final LocalDateTime startTime = LocalDateTime.now();
            log.info("running task, task name: {}, module name: {}, start time: {}", this.taskName, this.moduleName,
                startTime);

            final Path directory = Paths.get(this.outputPath, this.outputSubpath);
            Files.createDirectories(directory);
            Files.walk(directory).filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);

            final List<String> fundCodes = this.fundNameEmDao.queryAllFundCodes();
            if (Objects.isNull(fundCodes) || fundCodes.size() <= 0) {
                throw new IllegalArgumentException("empty fund codes");
            }

            // limit the number of threads to prevent full CPU load
            final ExecutorService executorService = Executors.newFixedThreadPool(this.currentThreads);
            final List<CompletableFuture<Pair<String, Boolean>>> futures =
                fundCodes.stream().map(fundCode -> CompletableFuture.supplyAsync(() -> {
                    final HttpGet httpGet = new HttpGet(this.inputUrl + "?fund=" + fundCode + "&indicator=单位净值走势");
                    httpGet.setHeader("Accept", "application/json");
                    try (final CloseableHttpResponse response = this.closeableHttpClient.execute(httpGet)) {
                        if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                            final byte[] bytes = EntityUtils.toByteArray(response.getEntity());
                            final Path file = directory.resolve(this.outputFile.replace("*", fundCode));
                            Files.write(file, bytes);
                            return Pair.of(fundCode, Files.exists(file));
                        } else {
                            log.error("task: {}, module: {}, fund code: {}, unexpected http status: {}", this.taskName,
                                this.moduleName, fundCode, response);
                            return Pair.of(fundCode, Boolean.FALSE);
                        }
                    } catch (final IOException e) {
                        throw new CompletionException(e);
                    }
                }, executorService).exceptionally(e -> {
                    log.error("task: {}, module: {}, fund code: {}, error: {}", this.taskName, this.moduleName,
                        fundCode, e.getMessage(), e);
                    return Pair.of(fundCode, Boolean.FALSE);
                })).collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).orTimeout(this.timeout, TimeUnit.HOURS)
                .join();
            log.info("total cost time: {} minutes", Duration.between(startTime, LocalDateTime.now()).toMinutes());
            final List<Pair<String, Boolean>> result =
                futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

            final long success = result.stream().filter(pair -> Boolean.TRUE.equals(pair.getSecond())).count();
            final List<String> errors = result.stream().filter(pair -> Boolean.FALSE.equals(pair.getSecond()))
                .map(Pair::getFirst).collect(Collectors.toList());
            log.info("number of success: " + success + ", number of failure: " + errors.size() + ", error fund codes: "
                + errors);
        };
    }
}
