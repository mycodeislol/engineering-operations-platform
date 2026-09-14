-- ====================================================================
-- EOP Database Migration: V1__init_identity_schema.sql
-- Bounded Context: Identity & Organization Hierarchy
-- ====================================================================

-- 1. Organizations Table
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Teams Table
CREATE TABLE teams (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    CONSTRAINT fk_teams_organization FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX idx_teams_org_id ON teams(org_id);

-- 3. Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    org_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_users_organization FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_org_id ON users(org_id);
