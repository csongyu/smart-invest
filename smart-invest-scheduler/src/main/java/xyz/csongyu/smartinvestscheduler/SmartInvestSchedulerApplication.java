package xyz.csongyu.smartinvestscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmartInvestSchedulerApplication {
    public static void main(final String[] args) {
        SpringApplication.run(SmartInvestSchedulerApplication.class, args);
    }
}
