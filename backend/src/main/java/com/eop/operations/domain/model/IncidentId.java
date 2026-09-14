package com.eop.operations.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record IncidentId(UUID value) implements Serializable {
    public IncidentId {
        Objects.requireNonNull(value, "Incident UUID cannot be null");
    }

    public static IncidentId generate() {
        return new IncidentId(UUID.randomUUID());
    }

    public static IncidentId of(String uuidString) {
        return new IncidentId(UUID.fromString(uuidString));
    }

    public static IncidentId of(UUID uuid) {
        return new IncidentId(uuid);
    }
}
