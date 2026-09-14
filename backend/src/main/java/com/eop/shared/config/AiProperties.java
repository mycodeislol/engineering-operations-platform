package com.eop.shared.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AiProperties(
    @NotBlank(message = "eop.ai.model must not be blank")
    String model,

    @NotBlank(message = "eop.ai.embedding-model must not be blank")
    String embeddingModel,

    @Min(value = 128, message = "Embedding vector dimensions must be at least 128")
    @Max(value = 4096, message = "Embedding vector dimensions cannot exceed 4096")
    int vectorDimensions,

    @Positive(message = "AI request timeout must be positive milliseconds")
    long requestTimeoutMs,

    boolean pgvectorEnabled
) {}
