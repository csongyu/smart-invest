package xyz.csongyu.smartinvestjob.configuration;

import java.nio.file.Paths;

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

import xyz.csongyu.smartinvestjob.dataobject.FundNameEmDO;

@Configuration
@ConditionalOnProperty(value = "job.name", havingValue = "fund-name-em")
public class FundNameEmJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    @Value("${job.name}")
    private String jobName;

    @Value("${job.fund-name-em.input.path}")
    private String inputPath;

    @Value("${job.fund-name-em.input.file}")
    private String inputFile;

    public FundNameEmJobConfiguration(final JobBuilderFactory jobBuilderFactory,
        final StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job job(final ItemReader<FundNameEmDO> reader, final ItemProcessor<FundNameEmDO, FundNameEmDO> processor,
        final ItemWriter<FundNameEmDO> writer) {
        final Step step = this.stepBuilderFactory.get(this.jobName + "-step").<FundNameEmDO, FundNameEmDO>chunk(1000)
            .reader(reader).processor(processor).writer(writer).build();
        return this.jobBuilderFactory.get(this.jobName).incrementer(new RunIdIncrementer()).start(step).build();
    }

    @Bean
    public ItemReader<FundNameEmDO> jsonItemReader() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JacksonJsonObjectReader<FundNameEmDO> jsonObjectReader =
            new JacksonJsonObjectReader<>(FundNameEmDO.class);
        jsonObjectReader.setMapper(objectMapper);

        return new JsonItemReaderBuilder<FundNameEmDO>().jsonObjectReader(jsonObjectReader)
            .resource(new FileSystemResource(Paths.get(this.inputPath, this.inputFile))).name("jsonItemReader").build();
    }

    @Bean
    public ItemProcessor<FundNameEmDO, FundNameEmDO> itemProcessor() {
        return fundNameEmDO -> fundNameEmDO;
    }

    @Bean
    public ItemWriter<FundNameEmDO> jdbcItemWriter(@Qualifier("clickHouseDataSource") final DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<FundNameEmDO>().beanMapped().dataSource(dataSource).sql(
            "INSERT INTO fund_name_em ( code, name, type, abbr_pinyin, pinyin ) VALUES ( :code, :name, :type, :abbrPinyin, :pinyin )")
            .build();
    }
}
