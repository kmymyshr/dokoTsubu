package com.example.dokotsubu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ServletComponentScan(basePackages = { "api", "servlet", "util" })
public class DokoTsubuApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DokoTsubuApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DokoTsubuApplication.class);
    }
}
