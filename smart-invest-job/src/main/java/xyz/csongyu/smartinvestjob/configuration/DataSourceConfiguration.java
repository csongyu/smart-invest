package xyz.csongyu.smartinvestjob.configuration;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.clickhouse.jdbc.ClickHouseDataSource;

@Configuration
@EnableConfigurationProperties(ClickHouseProperties.class)
public class DataSourceConfiguration {
    private final ClickHouseProperties clickHouseProperties;

    public DataSourceConfiguration(final ClickHouseProperties clickHouseProperties) {
        this.clickHouseProperties = clickHouseProperties;
    }

    @Bean
    public ClickHouseDataSource clickHouseDataSource() throws SQLException {
        final Properties properties = new Properties();
        properties.setProperty("user", this.clickHouseProperties.getUser());
        properties.setProperty("password", this.clickHouseProperties.getPassword());
        return new ClickHouseDataSource(this.clickHouseProperties.getUrl(), properties);
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(@Qualifier("dataSourceProperties") final DataSourceProperties properties) {
        return DataSourceBuilder.create().url(properties.getUrl()).username(properties.getUsername())
            .password(properties.getPassword()).driverClassName(properties.getDriverClassName()).build();
    }
}
