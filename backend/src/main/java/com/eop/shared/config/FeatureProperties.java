package com.eop.shared.config;

public record FeatureProperties(
    boolean autoIncidentCreationEnabled,
    boolean ragKnowledgeEnabled,
    boolean kafkaDistributionEnabled
) {}
