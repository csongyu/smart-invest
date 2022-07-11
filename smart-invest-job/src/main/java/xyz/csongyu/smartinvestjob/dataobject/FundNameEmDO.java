package xyz.csongyu.smartinvestjob.dataobject;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FundNameEmDO {
    @JsonProperty("基金代码")
    private String code;

    @JsonProperty("基金简称")
    private String name;

    @JsonProperty("基金类型")
    private String type;

    @JsonProperty("拼音缩写")
    private String abbrPinyin;

    @JsonProperty("拼音全称")
    private String pinyin;
}
