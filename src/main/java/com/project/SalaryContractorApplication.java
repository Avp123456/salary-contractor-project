package com.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
//main starter file

@SpringBootApplication
@ComponentScan(basePackages = "com.project")
public class SalaryContractorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalaryContractorApplication.class, args);
    }
}
 