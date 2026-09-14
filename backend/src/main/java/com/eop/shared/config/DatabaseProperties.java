package com.eop.shared.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record DatabaseProperties(
    @NotBlank(message = "eop.database.url cannot be blank")
    String url,

    @NotBlank(message = "eop.database.username cannot be blank")
    String username,

    @NotBlank(message = "eop.database.password cannot be blank")
    String password,

    @Min(value = 5, message = "Minimum pool size must be at least 5 connections")
    @Max(value = 200, message = "Maximum pool size cannot exceed 200 connections")
    int maxPoolSize,

    @Positive(message = "Connection timeout must be positive milliseconds")
    long connectionTimeoutMs,

    boolean autoMigrate
) {}
