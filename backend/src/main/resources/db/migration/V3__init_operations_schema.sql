-- ====================================================================
-- EOP Database Migration: V3__init_operations_schema.sql
-- Bounded Context: CI/CD Delivery & Operations Incident Management
-- ====================================================================

-- 1. Pipelines Table
CREATE TABLE pipelines (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_pipelines_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_pipelines_service_id ON pipelines(service_id);

-- 2. Builds Table
CREATE TABLE builds (
    id UUID PRIMARY KEY,
    pipeline_id UUID NOT NULL,
    commit_sha VARCHAR(40) NOT NULL,
    status VARCHAR(50) NOT NULL,
    duration_ms BIGINT,
    CONSTRAINT fk_builds_pipeline FOREIGN KEY (pipeline_id) REFERENCES pipelines(id) ON DELETE CASCADE
);

CREATE INDEX idx_builds_pipeline_id ON builds(pipeline_id);

-- 3. Deployments Table
CREATE TABLE deployments (
    id UUID PRIMARY KEY,
    build_id UUID NOT NULL,
    service_id UUID NOT NULL,
    env_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    failure_reason TEXT,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_deployments_build FOREIGN KEY (build_id) REFERENCES builds(id) ON DELETE CASCADE,
    CONSTRAINT fk_deployments_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_deployments_environment FOREIGN KEY (env_id) REFERENCES environments(id) ON DELETE CASCADE
);

-- Strict composite index & status index as required by EOP architectural blueprint
CREATE INDEX idx_deployments_service_env ON deployments (service_id, env_id);
CREATE INDEX idx_deployments_status ON deployments (status);

-- 4. Incidents Table
CREATE TABLE incidents (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    env_id UUID NOT NULL,
    deploy_id UUID,
    severity VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ,
    CONSTRAINT fk_incidents_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_incidents_environment FOREIGN KEY (env_id) REFERENCES environments(id) ON DELETE CASCADE,
    CONSTRAINT fk_incidents_deployment FOREIGN KEY (deploy_id) REFERENCES deployments(id) ON DELETE SET NULL
);

-- Strict composite index & status/deploy_id indexes as defined in architectural blueprint
CREATE INDEX idx_incidents_service_env ON incidents (service_id, env_id);
CREATE INDEX idx_incidents_deploy_id ON incidents (deploy_id);
CREATE INDEX idx_incidents_status ON incidents (status);

-- 5. Alerts Table
CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    source VARCHAR(100) NOT NULL,
    payload JSONB,
    CONSTRAINT fk_alerts_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE
);

CREATE INDEX idx_alerts_incident_id ON alerts(incident_id);

-- 6. Changes Table
CREATE TABLE changes (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    is_approved BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_changes_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_changes_service_id ON changes(service_id);
