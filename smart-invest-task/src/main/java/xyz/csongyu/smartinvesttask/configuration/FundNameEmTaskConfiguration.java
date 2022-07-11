package xyz.csongyu.smartinvesttask.configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "task.name", havingValue = "fund-name-em")
public class FundNameEmTaskConfiguration {
    @Value("${task.name}")
    private String taskName;

    @Value("${task.fund-name-em.input.url}")
    private String inputUrl;

    @Value("${task.fund-name-em.output.path}")
    private String outputPath;

    @Value("${task.fund-name-em.output.file}")
    private String outputFile;

    @Value("${httpclient.connect-timeout}")
    private int connectTimeout;

    @Value("${httpclient.socket-timeout}")
    private int socketTimeout;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            log.info("running task, task name: {}", this.taskName);

            final Path directory = Paths.get(this.outputPath);
            Files.createDirectories(directory);
            Files.walk(directory).filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);

            final Path file = directory.resolve(this.outputFile);
            Request.Get(this.inputUrl).connectTimeout(this.connectTimeout).socketTimeout(this.socketTimeout)
                .addHeader("Accept", "application/json").execute().saveContent(file.toFile());
        };
    }
}
