package com.eop.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@EnableConfigurationProperties(PlatformProperties.class)
public class CoreConfigurationValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(CoreConfigurationValidator.class);
    private final PlatformProperties platformProperties;

    public CoreConfigurationValidator(PlatformProperties platformProperties) {
        this.platformProperties = Objects.requireNonNull(platformProperties, "Platform properties must not be null");
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Validating platform environment configuration for instance: [{}], environment: [{}]",
                platformProperties.instanceId(), platformProperties.environment());

        validateCrossCuttingInvariants();

        log.info("Platform configuration validation successful. Automated Incident Creation: [{}], Kafka Decoupled: [{}]",
                platformProperties.features().autoIncidentCreationEnabled(),
                platformProperties.features().kafkaDistributionEnabled());
    }

    private void validateCrossCuttingInvariants() {
        if ("prod".equalsIgnoreCase(platformProperties.environment()) || "production".equalsIgnoreCase(platformProperties.environment())) {
            if (platformProperties.database().url().contains("localhost") || platformProperties.database().url().contains("127.0.0.1")) {
                throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: Production environment cannot point to localhost database: "
                        + platformProperties.database().url());
            }
            if (platformProperties.security().jwtSecret().contains("default") || platformProperties.security().jwtSecret().contains("secret123")) {
                throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: Weak JWT secret detected in production environment.");
            }
        }

        if (platformProperties.security().tokenValiditySeconds() >= platformProperties.security().refreshTokenValiditySeconds()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: Access token validity ("
                    + platformProperties.security().tokenValiditySeconds()
                    + "s) must be strictly less than refresh token validity ("
                    + platformProperties.security().refreshTokenValiditySeconds() + "s)");
        }

        if (platformProperties.features().ragKnowledgeEnabled() && !platformProperties.ai().pgvectorEnabled()) {
            throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: RAG knowledge feature is enabled but pgvector is disabled.");
        }
    }
}
