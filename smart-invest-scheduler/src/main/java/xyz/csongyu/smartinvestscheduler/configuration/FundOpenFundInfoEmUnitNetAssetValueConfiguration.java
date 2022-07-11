package xyz.csongyu.smartinvestscheduler.configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import xyz.csongyu.smartinvestscheduler.service.LaunchTaskService;

@Configuration
@ConditionalOnProperty(value = "schedule.fund-open-fund-info-em.unit-net-asset-value.enable", havingValue = "true")
public class FundOpenFundInfoEmUnitNetAssetValueConfiguration {
    private final LaunchTaskService launchTaskService;

    public FundOpenFundInfoEmUnitNetAssetValueConfiguration(final LaunchTaskService launchTaskService) {
        this.launchTaskService = launchTaskService;
    }

    @Scheduled(cron = "${schedule.fund-open-fund-info-em.unit-net-asset-value.cron}")
    public void schedule() throws IOException {
        final List<String> properties = Arrays.asList(
            "app.job-fund-open-fund-info-em-unit-net-asset-value.job.module.name=unit-net-asset-value",
            "app.job-fund-open-fund-info-em-unit-net-asset-value.job.name=fund-open-fund-info-em",
            "app.task-fund-open-fund-info-em-unit-net-asset-value.task.module.name=unit-net-asset-value",
            "app.task-fund-open-fund-info-em-unit-net-asset-value.task.name=fund-open-fund-info-em",
            "app.task-initialize-schema-fund-open-fund-info-em-unit-net-asset-value.task.module.name=fund-open-fund-info-em-unit-net-asset-value",
            "app.task-initialize-schema-fund-open-fund-info-em-unit-net-asset-value.task.name=initialize-schema");

        final int status = this.launchTaskService.call("fofieunav", properties);
        if (HttpStatus.SC_CREATED != status) {
            throw new RuntimeException("schedule failure");
        }
    }
}
