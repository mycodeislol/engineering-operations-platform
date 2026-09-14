-- ====================================================================
-- EOP Database Migration: V2__init_engineering_schema.sql
-- Bounded Context: Engineering Core Topology
-- ====================================================================

-- 1. Projects Table
CREATE TABLE projects (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    key VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_projects_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE
);

CREATE INDEX idx_projects_team_id ON projects(team_id);

-- 2. Repositories Table
CREATE TABLE repositories (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    language VARCHAR(50) NOT NULL,
    CONSTRAINT fk_repositories_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_repositories_project_id ON repositories(project_id);

-- 3. Services (Service Catalog) Table
CREATE TABLE services (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    repo_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    CONSTRAINT fk_services_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_services_repository FOREIGN KEY (repo_id) REFERENCES repositories(id) ON DELETE CASCADE
);

CREATE INDEX idx_services_project_id ON services(project_id);
CREATE INDEX idx_services_repo_id ON services(repo_id);

-- 4. Environments Table
CREATE TABLE environments (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    url VARCHAR(255),
    CONSTRAINT fk_environments_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_environments_service_id ON environments(service_id);

-- 5. Service Dependencies Table
CREATE TABLE service_dependencies (
    source_id UUID NOT NULL,
    target_id UUID NOT NULL,
    dependency_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (source_id, target_id),
    CONSTRAINT fk_service_dependencies_source FOREIGN KEY (source_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_service_dependencies_target FOREIGN KEY (target_id) REFERENCES services(id) ON DELETE CASCADE
);
