package xyz.csongyu.smartinvesttask.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.clickhouse.jdbc.ClickHouseDataSource;

@Component
public class FundNameEmDaoImpl implements FundNameEmDao {
    private final ClickHouseDataSource dataSource;

    public FundNameEmDaoImpl(@Qualifier("clickHouseDataSource") final ClickHouseDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<String> queryAllFundCodes() throws SQLException {
        final String querySql = "SELECT code FROM fund_name_em";
        final List<String> result = new ArrayList<>();
        try (final Connection connection = this.dataSource.getConnection();
            final Statement statement = connection.createStatement()) {
            try (final ResultSet resultSet = statement.executeQuery(querySql)) {
                while (resultSet.next()) {
                    final String code = resultSet.getString("code");
                    result.add(code);
                }
            }
        }
        return result;
    }
}
