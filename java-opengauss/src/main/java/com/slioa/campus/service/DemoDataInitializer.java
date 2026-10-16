package com.slioa.campus.service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataInitializer {
    @Bean
    ApplicationRunner seedDemoData(CampusService campusService) {
        return args -> campusService.initializeDemoData();
    }
}