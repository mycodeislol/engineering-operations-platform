package com.eop.shared.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SecurityProperties(
    @NotBlank(message = "eop.security.jwt-secret must not be blank")
    @Size(min = 32, message = "eop.security.jwt-secret must be at least 256 bits (32 characters)")
    String jwtSecret,

    @NotBlank(message = "eop.security.jwt-issuer must not be blank")
    String jwtIssuer,

    @Positive(message = "eop.security.token-validity-seconds must be greater than 0")
    long tokenValiditySeconds,

    @Positive(message = "eop.security.refresh-token-validity-seconds must be greater than 0")
    long refreshTokenValiditySeconds
) {}
