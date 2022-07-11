package xyz.csongyu.smartinvestscheduler.configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;
import xyz.csongyu.smartinvestscheduler.service.LaunchTaskService;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "schedule.fund-name-em.enable", havingValue = "true")
public class FundNameEmConfiguration {
    private final LaunchTaskService launchTaskService;

    public FundNameEmConfiguration(final LaunchTaskService launchTaskService) {
        this.launchTaskService = launchTaskService;
    }

    @Scheduled(cron = "${schedule.fund-name-em.cron}")
    public void schedule() throws IOException {
        log.info("schedule task fne");

        final List<String> properties =
            Arrays.asList("app.job-fund-name-em.job.name=fund-name-em", "app.task-fund-name-em.task.name=fund-name-em",
                "app.task-initialize-schema-fund-name-em.task.module.name=fund-name-em",
                "app.task-initialize-schema-fund-name-em.task.name=initialize-schema");

        final int status = this.launchTaskService.call("fne", properties);
        if (HttpStatus.SC_CREATED != status) {
            throw new RuntimeException("schedule failure");
        }
    }
}
