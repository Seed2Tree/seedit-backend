package com.seedit.feature.settlement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class SettlementConfiguration {
    @Bean
    public Clock settlementClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
