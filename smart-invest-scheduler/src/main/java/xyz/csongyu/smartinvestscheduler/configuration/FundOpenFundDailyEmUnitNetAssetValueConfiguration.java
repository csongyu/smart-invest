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
@ConditionalOnProperty(value = "schedule.fund-open-fund-daily-em.enable", havingValue = "true")
public class FundOpenFundDailyEmUnitNetAssetValueConfiguration {
    private final LaunchTaskService launchTaskService;

    public FundOpenFundDailyEmUnitNetAssetValueConfiguration(final LaunchTaskService launchTaskService) {
        this.launchTaskService = launchTaskService;
    }

    @Scheduled(cron = "${schedule.fund-open-fund-daily-em.cron}")
    public void schedule() throws IOException {
        final List<String> properties =
            Arrays.asList("app.job-fund-open-fund-daily-em.job.name=fund-open-fund-daily-em",
                "app.task-fund-open-fund-daily-em.task.name=fund-open-fund-daily-em");

        final int status = this.launchTaskService.call("fofde", properties);
        if (HttpStatus.SC_CREATED != status) {
            throw new RuntimeException("schedule failure");
        }
    }
}
