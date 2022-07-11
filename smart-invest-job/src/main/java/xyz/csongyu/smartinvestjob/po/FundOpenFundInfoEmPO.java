package xyz.csongyu.smartinvestjob.po;

import lombok.Data;

@Data
public class FundOpenFundInfoEmPO {
    private String code;

    private long timestamp;

    private Double value;

    private Double rate;
}
