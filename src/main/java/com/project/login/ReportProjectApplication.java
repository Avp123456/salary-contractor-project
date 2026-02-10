package com.project.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
//main starter file

@SpringBootApplication
@ComponentScan(basePackages = "com.project")
public class ReportProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportProjectApplication.class, args);
    }
}
 