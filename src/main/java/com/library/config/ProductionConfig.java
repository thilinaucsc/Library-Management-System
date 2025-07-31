package com.library.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for production-specific beans and settings.
 * Provides enhanced monitoring, metrics, and application information.
 */
@Configuration
@Profile({"prod", "staging"})
public class ProductionConfig {

    /**
     * Custom info contributor for actuator /info endpoint.
     * Provides application build and runtime information.
     */
    @Bean
    public InfoContributor customInfoContributor() {
        return new InfoContributor() {
            @Override
            public void contribute(Info.Builder builder) {
                Map<String, Object> appInfo = new HashMap<>();
                appInfo.put("name", "Library Management System API");
                appInfo.put("description", "RESTful API for managing library books and borrowers");
                appInfo.put("version", "1.0.0");
                appInfo.put("startup-time", Instant.now().toString());
                
                Map<String, Object> buildInfo = new HashMap<>();
                buildInfo.put("java-version", System.getProperty("java.version"));
                buildInfo.put("spring-boot-version", "3.5.4");
                buildInfo.put("build-time", Instant.now().toString());
                
                builder.withDetail("app", appInfo);
                builder.withDetail("build", buildInfo);
            }
        };
    }

    /**
     * Enable @Timed annotations for method-level metrics.
     * Allows detailed performance monitoring of service methods.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}