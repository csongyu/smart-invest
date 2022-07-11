package xyz.csongyu.smartinvesttask.dao;

import java.sql.SQLException;
import java.util.List;

public interface FundNameEmDao {
    List<String> queryAllFundCodes() throws SQLException;
}
