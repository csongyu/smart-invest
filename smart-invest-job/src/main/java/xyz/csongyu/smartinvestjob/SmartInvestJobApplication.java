package xyz.csongyu.smartinvestjob;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask
@EnableBatchProcessing
@SpringBootApplication
public class SmartInvestJobApplication {
    public static void main(final String[] args) {
        SpringApplication.run(SmartInvestJobApplication.class, args);
    }
}
