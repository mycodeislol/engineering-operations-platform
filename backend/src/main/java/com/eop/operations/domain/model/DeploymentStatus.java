package com.eop.operations.domain.model;

public enum DeploymentStatus {
    QUEUED,
    RUNNING,
    SUCCESS,
    FAILED;

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED;
    }
}
