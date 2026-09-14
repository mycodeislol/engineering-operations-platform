package com.eop.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "eop")
public record PlatformProperties(
    @NotBlank(message = "Platform instance identifier (eop.instance-id) must not be blank")
    String instanceId,

    @NotBlank(message = "Platform environment name (eop.environment) must be provided (e.g., local, dev, staging, prod)")
    String environment,

    @NotNull(message = "Database configuration properties (eop.database) must be defined")
    @Valid
    DatabaseProperties database,

    @NotNull(message = "Security configuration properties (eop.security) must be defined")
    @Valid
    SecurityProperties security,

    @NotNull(message = "Kafka event bus configuration properties (eop.kafka) must be defined")
    @Valid
    KafkaProperties kafka,

    @NotNull(message = "Feature toggle configuration properties (eop.features) must be defined")
    @Valid
    FeatureProperties features,

    @NotNull(message = "AI assistant configuration properties (eop.ai) must be defined")
    @Valid
    AiProperties ai
) {}
