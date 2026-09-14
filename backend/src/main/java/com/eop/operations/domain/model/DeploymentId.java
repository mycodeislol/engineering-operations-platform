package com.eop.operations.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record DeploymentId(UUID value) implements Serializable {
    public DeploymentId {
        Objects.requireNonNull(value, "Deployment UUID cannot be null");
    }

    public static DeploymentId generate() {
        return new DeploymentId(UUID.randomUUID());
    }

    public static DeploymentId of(String uuidString) {
        return new DeploymentId(UUID.fromString(uuidString));
    }

    public static DeploymentId of(UUID uuid) {
        return new DeploymentId(uuid);
    }
}
