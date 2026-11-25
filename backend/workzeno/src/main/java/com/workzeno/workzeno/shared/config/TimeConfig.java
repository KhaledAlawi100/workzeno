package com.workzeno.workzeno.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    public static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Riyadh");

    @Bean
    public Clock applicationClock() {
        return Clock.system(BUSINESS_ZONE);
    }
}