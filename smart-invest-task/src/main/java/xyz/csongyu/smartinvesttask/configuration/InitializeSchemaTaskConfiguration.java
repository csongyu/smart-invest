package xyz.csongyu.smartinvesttask.configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.clickhouse.jdbc.ClickHouseDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "task.name", havingValue = "initialize-schema")
public class InitializeSchemaTaskConfiguration {
    private final ClickHouseDataSource dataSource;

    @Value("${task.name}")
    private String taskName;

    @Value("${task.module.name}")
    private String moduleName;

    public InitializeSchemaTaskConfiguration(@Qualifier("clickHouseDataSource") final ClickHouseDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    @ConditionalOnProperty(value = "task.module.name", havingValue = "fund-name-em")
    public CommandLineRunner fundNameEmRunner() {
        return args -> {
            log.info("initialize schema, task name: {}, module name: {}", this.taskName, this.moduleName);

            final String createSql =
                "CREATE TABLE IF NOT EXISTS fund_name_em ( code String, name String, type String, abbr_pinyin String, pinyin String ) ENGINE = MergeTree() PRIMARY KEY code";
            this.execute(createSql);

            final String truncateSql = "TRUNCATE TABLE IF EXISTS fund_name_em";
            this.execute(truncateSql);
        };
    }

    @Bean
    @ConditionalOnProperty(value = "task.module.name", havingValue = "fund-open-fund-info-em-unit-net-asset-value")
    public CommandLineRunner fundOpenFundInfoEmRunner() {
        return args -> {
            log.info("initialize schema, task name: {}, module name: {}", this.taskName, this.moduleName);

            final String createSql =
                "CREATE TABLE IF NOT EXISTS fund_open_fund_info_em_unit_net_asset_value ( code String, timestamp DateTime, value Decimal(8,4), rate Decimal(6,2) ) ENGINE = MergeTree() PRIMARY KEY ( code, timestamp )";
            this.execute(createSql);

            final String truncateSql = "TRUNCATE TABLE IF EXISTS fund_open_fund_info_em_unit_net_asset_value";
            this.execute(truncateSql);
        };
    }

    private void execute(final String sql) throws SQLException {
        try (final Connection connection = this.dataSource.getConnection();
            final Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
