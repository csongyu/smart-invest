package xyz.csongyu.smartinvestjob.configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;
import xyz.csongyu.smartinvestjob.po.FundOpenFundInfoEmPO;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "job.name", havingValue = "fund-open-fund-daily-em")
public class FundOpenFundDailyEmJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Value("${job.name}")
    private String jobName;

    @Value("${job.fund-name-em.input.path}")
    private String inputPath;

    @Value("${job.fund-name-em.input.file}")
    private String inputFile;

    public FundOpenFundDailyEmJobConfiguration(final JobBuilderFactory jobBuilderFactory,
        final StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job job(final ItemReader<ObjectNode> reader, final ItemProcessor<ObjectNode, FundOpenFundInfoEmPO> processor,
        final ItemWriter<FundOpenFundInfoEmPO> writer) {
        final Step step = this.stepBuilderFactory.get(this.jobName + "-step")
            .<ObjectNode, FundOpenFundInfoEmPO>chunk(1000).reader(reader).processor(processor).writer(writer).build();
        return this.jobBuilderFactory.get(this.jobName).incrementer(new RunIdIncrementer()).start(step).build();
    }

    @Bean
    public ItemReader<ObjectNode> jsonItemReader() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JacksonJsonObjectReader<ObjectNode> jsonObjectReader = new JacksonJsonObjectReader<>(ObjectNode.class);
        jsonObjectReader.setMapper(objectMapper);

        return new JsonItemReaderBuilder<ObjectNode>().jsonObjectReader(jsonObjectReader)
            .resource(new FileSystemResource(Paths.get(this.inputPath, this.inputFile))).name("jsonItemReader").build();
    }

    @Bean
    public ItemProcessor<ObjectNode, FundOpenFundInfoEmPO> itemProcessor() {
        return input -> {
            final List<String> dates = new ArrayList<>();
            final Iterator<String> fields = input.fieldNames();
            while (fields.hasNext()) {
                final String field = fields.next();
                if (field.contains("单位净值")) {
                    dates.add(field.substring(0, field.lastIndexOf("-")));
                }
            }
            final Optional<ZonedDateTime> zonedDateTime =
                dates.stream().map(date -> LocalDate.parse(date, DateTimeFormatter.ofPattern("uuuu-MM-dd"))
                    .atStartOfDay(ZoneId.of("Asia/Shanghai"))).max(ChronoZonedDateTime::compareTo);

            final FundOpenFundInfoEmPO output = new FundOpenFundInfoEmPO();
            output.setCode(input.get("基金代码").textValue());
            if (zonedDateTime.isPresent()) {
                output.setTimestamp(zonedDateTime.get().toEpochSecond());
                final String value = input
                    .get(zonedDateTime.get().format(DateTimeFormatter.ofPattern("uuuu-MM-dd")) + "-单位净值").textValue();
                if (value != null && value.length() > 0) {
                    output.setValue(new BigDecimal(value).setScale(4, RoundingMode.DOWN).doubleValue());
                }
            } else {
                log.error("incorrect parameter, can not get date: " + input);
            }
            final String rate = input.get("日增长率").textValue();
            if (rate != null && rate.length() > 0) {
                output.setRate(new BigDecimal(rate).setScale(2, RoundingMode.DOWN).doubleValue());
            }
            return output;
        };
    }

    @Bean
    public ItemWriter<FundOpenFundInfoEmPO>
        jdbcItemWriter(@Qualifier("clickHouseDataSource") final DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<FundOpenFundInfoEmPO>().beanMapped().dataSource(dataSource).sql(
            "INSERT INTO fund_open_fund_info_em_unit_net_asset_value ( code, timestamp, value, rate ) VALUES ( :code, :timestamp, :value, :rate )")
            .build();
    }
}
