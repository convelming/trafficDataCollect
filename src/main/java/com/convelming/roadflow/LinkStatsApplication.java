package com.convelming.roadflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.convelming.roadflow.mapper")
@ComponentScan(basePackages = {"com.convelming.roadflow.*"})
public class LinkStatsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkStatsApplication.class, args);
    }

}
