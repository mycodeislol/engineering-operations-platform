package com.eop.shared.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record KafkaProperties(
    @NotBlank(message = "eop.kafka.bootstrap-servers must not be blank")
    String bootstrapServers,

    @NotBlank(message = "eop.kafka.consumer-group-id must not be blank")
    String consumerGroupId,

    @Min(value = 1, message = "Producer retry count must be at least 1")
    int producerRetries,

    @Positive(message = "Kafka request timeout must be positive milliseconds")
    int requestTimeoutMs
) {}
