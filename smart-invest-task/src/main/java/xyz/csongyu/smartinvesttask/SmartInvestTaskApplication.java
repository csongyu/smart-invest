package xyz.csongyu.smartinvesttask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@SpringBootApplication
@EnableTask
public class SmartInvestTaskApplication {
    public static void main(final String[] args) {
        SpringApplication.run(SmartInvestTaskApplication.class, args);
    }
}
