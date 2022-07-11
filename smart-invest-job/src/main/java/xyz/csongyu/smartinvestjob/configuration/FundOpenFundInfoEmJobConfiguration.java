package xyz.csongyu.smartinvestjob.configuration;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import xyz.csongyu.smartinvestjob.dataobject.FundOpenFundInfoEmDO;
import xyz.csongyu.smartinvestjob.po.FundOpenFundInfoEmPO;

@Configuration
@ConditionalOnProperty(value = "job.name", havingValue = "fund-open-fund-info-em")
public class FundOpenFundInfoEmJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Value("${job.name}")
    private String jobName;

    @Value("${job.fund-open-fund-info-em.input.path}")
    private String inputPath;

    @Value("${job.fund-open-fund-info-em.input.subpath}")
    private String inputSubpath;

    @Value("${job.fund-open-fund-info-em.input.file}")
    private String inputFile;

    public FundOpenFundInfoEmJobConfiguration(final JobBuilderFactory jobBuilderFactory,
        final StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job job(final ItemReader<FundOpenFundInfoEmDO> reader,
        final ItemProcessor<FundOpenFundInfoEmDO, FundOpenFundInfoEmPO> processor,
        final ItemWriter<FundOpenFundInfoEmPO> writer) {
        final Step step =
            this.stepBuilderFactory.get(this.jobName + "-step").<FundOpenFundInfoEmDO, FundOpenFundInfoEmPO>chunk(1000)
                .reader(reader).processor(processor).writer(writer).build();
        return this.jobBuilderFactory.get(this.jobName).incrementer(new RunIdIncrementer()).start(step).build();
    }

    @Bean
    public MultiResourceItemReader<FundOpenFundInfoEmDO> multiResourceItemReader() throws IOException {
        final ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        final Resource[] resources =
            patternResolver.getResources("file:" + Paths.get(this.inputPath, this.inputSubpath, this.inputFile));

        final MultiResourceItemReader<FundOpenFundInfoEmDO> itemReader = new MultiResourceItemReader<>();
        itemReader.setDelegate(this.jsonItemReader());
        itemReader.setResources(resources);
        return itemReader;
    }

    public JsonItemReader<FundOpenFundInfoEmDO> jsonItemReader() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JacksonJsonObjectReader<FundOpenFundInfoEmDO> jsonObjectReader =
            new JacksonJsonObjectReader<>(FundOpenFundInfoEmDO.class);
        jsonObjectReader.setMapper(objectMapper);

        return new JsonItemReaderBuilder<FundOpenFundInfoEmDO>().jsonObjectReader(jsonObjectReader)
            .name("jsonItemReader").build();
    }

    @Bean
    public ItemProcessor<FundOpenFundInfoEmDO, FundOpenFundInfoEmPO> itemProcessor() {
        return input -> {
            final FundOpenFundInfoEmPO output = new FundOpenFundInfoEmPO();
            output.setCode(input.getCode());
            final Matcher matcher = Pattern.compile("\\d{4}-\\d{2}-\\d{2}").matcher(input.getDate());
            if (matcher.find()) {
                final String date = matcher.group();
                final ZonedDateTime zonedDateTime = LocalDate.parse(date, DateTimeFormatter.ofPattern("uuuu-MM-dd"))
                    .atStartOfDay(ZoneId.of("Asia/Shanghai"));
                output.setTimestamp(zonedDateTime.toEpochSecond());
            }
            output.setValue(new BigDecimal(input.getValue()).setScale(4, RoundingMode.DOWN).doubleValue());
            output.setRate(new BigDecimal(input.getRate()).setScale(2, RoundingMode.DOWN).doubleValue());
            return output;
        };
    }

    @Bean
    @ConditionalOnProperty(value = "job.module.name", havingValue = "unit-net-asset-value")
    public ItemWriter<FundOpenFundInfoEmPO>
        jdbcItemWriter(@Qualifier("clickHouseDataSource") final DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<FundOpenFundInfoEmPO>().beanMapped().dataSource(dataSource).sql(
            "INSERT INTO fund_open_fund_info_em_unit_net_asset_value ( code, timestamp, value, rate ) VALUES ( :code, :timestamp, :value, :rate )")
            .build();
    }
}
