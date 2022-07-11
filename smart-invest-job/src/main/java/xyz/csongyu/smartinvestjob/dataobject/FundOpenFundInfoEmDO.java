package xyz.csongyu.smartinvestjob.dataobject;

import java.util.Objects;

import org.springframework.batch.item.ResourceAware;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FundOpenFundInfoEmDO implements ResourceAware {
    private String code;

    @JsonProperty("净值日期")
    private String date;

    @JsonProperty("单位净值")
    private String value;

    @JsonProperty("日增长率")
    private String rate;

    @Override
    public void setResource(final Resource resource) {
        final String filename = resource.getFilename();
        if (Objects.nonNull(filename)) {
            this.code = filename.substring(0, filename.lastIndexOf(".json"));
        }
    }
}
