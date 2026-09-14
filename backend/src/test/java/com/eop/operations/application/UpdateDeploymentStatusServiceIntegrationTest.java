package com.eop.operations.application;

import com.eop.EopApplication;
import com.eop.operations.application.dto.DeploymentStatusResponseDto;
import com.eop.operations.application.dto.UpdateDeploymentStatusCommand;
import com.eop.operations.application.port.in.UpdateDeploymentStatusUseCase;
import com.eop.operations.domain.exception.InvalidDeploymentStateTransitionException;
import com.eop.operations.domain.model.DeploymentStatus;
import com.eop.operations.domain.model.IncidentSeverity;
import com.eop.operations.domain.model.IncidentStatus;
import com.eop.operations.infrastructure.persistence.DeploymentJpaEntity;
import com.eop.operations.infrastructure.persistence.IncidentJpaEntity;
import com.eop.operations.infrastructure.persistence.SpringDataDeploymentRepository;
import com.eop.operations.infrastructure.persistence.SpringDataIncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = EopApplication.class)
@Testcontainers
class UpdateDeploymentStatusServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("eop_test_db")
            .withUsername("eop_test_user")
            .withPassword("eop_test_password");

    @Autowired
    private UpdateDeploymentStatusUseCase updateDeploymentStatusUseCase;

    @Autowired
    private SpringDataDeploymentRepository deploymentJpaRepository;

    @Autowired
    private SpringDataIncidentRepository incidentJpaRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID serviceId;
    private UUID envId;
    private UUID buildId;

    @BeforeEach
    void setUp() {
        incidentJpaRepository.deleteAll();
        deploymentJpaRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM builds");
        jdbcTemplate.update("DELETE FROM pipelines");
        jdbcTemplate.update("DELETE FROM environments");
        jdbcTemplate.update("DELETE FROM services");
        jdbcTemplate.update("DELETE FROM repositories");
        jdbcTemplate.update("DELETE FROM projects");
        jdbcTemplate.update("DELETE FROM teams");
        jdbcTemplate.update("DELETE FROM organizations");

        UUID orgId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID repoId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        envId = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();
        buildId = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO organizations (id, name, domain) VALUES (?, ?, ?)",
                orgId, "Test Org", "test.org");
        jdbcTemplate.update("INSERT INTO teams (id, org_id, name, description) VALUES (?, ?, ?, ?)",
                teamId, orgId, "Test Team", "Description");
        jdbcTemplate.update("INSERT INTO projects (id, team_id, name, key, status) VALUES (?, ?, ?, ?, ?)",
                projectId, teamId, "Test Project", "PROJ-" + UUID.randomUUID().toString().substring(0, 8), "ACTIVE");
        jdbcTemplate.update("INSERT INTO repositories (id, project_id, name, url, language) VALUES (?, ?, ?, ?, ?)",
                repoId, projectId, "test-repo", "https://git.test/repo", "Java");
        jdbcTemplate.update("INSERT INTO services (id, project_id, repo_id, name, type) VALUES (?, ?, ?, ?, ?)",
                serviceId, projectId, repoId, "svc-" + UUID.randomUUID().toString().substring(0, 8), "BACKEND");
        jdbcTemplate.update("INSERT INTO environments (id, service_id, name, url) VALUES (?, ?, ?, ?)",
                envId, serviceId, "production", "https://prod.test");
        jdbcTemplate.update("INSERT INTO pipelines (id, service_id, name, trigger_type) VALUES (?, ?, ?, ?)",
                pipelineId, serviceId, "deploy-pipeline", "MANUAL");
        jdbcTemplate.update("INSERT INTO builds (id, pipeline_id, commit_sha, status, duration_ms) VALUES (?, ?, ?, ?, ?)",
                buildId, pipelineId, "a1b2c3d4e5f67890123456789012345678901234", "SUCCESS", 120000L);
    }

    @Test
    @DisplayName("State Machine Guard: Should throw InvalidDeploymentStateTransitionException when modifying terminal state")
    void stateMachineGuard_shouldRejectTransitionFromTerminalSuccessToFailed() {
        // Arrange: Seed deployment in RUNNING status
        UUID deploymentId = UUID.randomUUID();
        DeploymentJpaEntity entity = new DeploymentJpaEntity(
                deploymentId,
                buildId,
                serviceId,
                envId,
                DeploymentStatus.RUNNING,
                null,
                Instant.now(),
                null
        );
        deploymentJpaRepository.saveAndFlush(entity);

        // Transition: RUNNING -> SUCCESS (Terminal)
        UpdateDeploymentStatusCommand successCommand = new UpdateDeploymentStatusCommand(
                deploymentId,
                DeploymentStatus.SUCCESS,
                null
        );
        DeploymentStatusResponseDto successResponse = updateDeploymentStatusUseCase.updateStatus(successCommand);
        assertThat(successResponse.status()).isEqualTo(DeploymentStatus.SUCCESS);
        assertThat(successResponse.completedAt()).isNotNull();

        // Act & Assert: Attempt illegal transition SUCCESS -> FAILED
        UpdateDeploymentStatusCommand illegalFailureCommand = new UpdateDeploymentStatusCommand(
                deploymentId,
                DeploymentStatus.FAILED,
                "Attempted late failure injection"
        );

        assertThatThrownBy(() -> updateDeploymentStatusUseCase.updateStatus(illegalFailureCommand))
                .isInstanceOf(InvalidDeploymentStateTransitionException.class)
                .hasMessageContaining("Illegal deployment state transition")
                .hasMessageContaining("Cannot transition from [SUCCESS] to [FAILED]");

        // Verify state unchanged in PostgreSQL database
        DeploymentJpaEntity postCheckEntity = deploymentJpaRepository.findById(deploymentId).orElseThrow();
        assertThat(postCheckEntity.getStatus()).isEqualTo(DeploymentStatus.SUCCESS);
        assertThat(postCheckEntity.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("Automated Incident Automation: Should create SEV-2 incident and update state on deployment failure")
    void automatedIncident_shouldGenerateSev2IncidentOnDeploymentFailure() {
        // Arrange: Seed deployment in RUNNING status
        UUID deploymentId = UUID.randomUUID();
        DeploymentJpaEntity entity = new DeploymentJpaEntity(
                deploymentId,
                buildId,
                serviceId,
                envId,
                DeploymentStatus.RUNNING,
                null,
                Instant.now(),
                null
        );
        deploymentJpaRepository.saveAndFlush(entity);

        String failureDiagnostic = "Database migration constraint failure: fk_service_catalog broken";

        // Act: Transition RUNNING -> FAILED
        UpdateDeploymentStatusCommand failCommand = new UpdateDeploymentStatusCommand(
                deploymentId,
                DeploymentStatus.FAILED,
                failureDiagnostic
        );
        DeploymentStatusResponseDto response = updateDeploymentStatusUseCase.updateStatus(failCommand);

        // Assert Use Case Response
        assertThat(response.status()).isEqualTo(DeploymentStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo(failureDiagnostic);
        assertThat(response.triggeredIncidentId()).isNotNull();

        // Verify Deployment in PostgreSQL
        DeploymentJpaEntity updatedDeployment = deploymentJpaRepository.findById(deploymentId).orElseThrow();
        assertThat(updatedDeployment.getStatus()).isEqualTo(DeploymentStatus.FAILED);
        assertThat(updatedDeployment.getFailureReason()).isEqualTo(failureDiagnostic);
        assertThat(updatedDeployment.getCompletedAt()).isNotNull();

        // Verify Automated SEV-2 Incident in PostgreSQL
        Optional<IncidentJpaEntity> incidentOpt = incidentJpaRepository.findByDeployId(deploymentId);
        assertThat(incidentOpt).isPresent();

        IncidentJpaEntity createdIncident = incidentOpt.get();
        assertThat(createdIncident.getId()).isEqualTo(response.triggeredIncidentId());
        assertThat(createdIncident.getServiceId()).isEqualTo(serviceId);
        assertThat(createdIncident.getEnvId()).isEqualTo(envId);
        assertThat(createdIncident.getDeployId()).isEqualTo(deploymentId);
        assertThat(createdIncident.getSeverity()).isEqualTo(IncidentSeverity.SEV_2);
        assertThat(createdIncident.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(createdIncident.getTitle()).contains("Automated Alert: Deployment Failure");
        assertThat(createdIncident.getSummary()).contains(failureDiagnostic);
    }

    @Test
    @DisplayName("Transactional Isolation & Rollback Consistency: Uncommitted mutations must rollback upon exception")
    void transactionalIsolation_shouldRollbackConsistencyUponFailure() {
        UUID deploymentId = UUID.randomUUID();
        DeploymentJpaEntity initial = new DeploymentJpaEntity(
                deploymentId,
                buildId,
                serviceId,
                envId,
                DeploymentStatus.RUNNING,
                null,
                Instant.now(),
                null
        );
        deploymentJpaRepository.saveAndFlush(initial);

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // Execute transaction block that fails midway
        assertThatThrownBy(() -> txTemplate.execute(status -> {
            DeploymentJpaEntity entity = deploymentJpaRepository.findById(deploymentId).orElseThrow();
            entity.setStatus(DeploymentStatus.FAILED);
            entity.setFailureReason("Transient failure before rollback");
            deploymentJpaRepository.saveAndFlush(entity);

            // Intentionally trigger rollback
            throw new RuntimeException("Simulated unexpected mid-transaction crash");
        })).isInstanceOf(RuntimeException.class)
           .hasMessage("Simulated unexpected mid-transaction crash");

        // Verify database state rolled back to RUNNING
        DeploymentJpaEntity postRollbackEntity = deploymentJpaRepository.findById(deploymentId).orElseThrow();
        assertThat(postRollbackEntity.getStatus()).isEqualTo(DeploymentStatus.RUNNING);
        assertThat(postRollbackEntity.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("Foreign Key Cascading & Relational Integrity: Incident deploy_id reflects foreign key constraints")
    void relationalIntegrity_shouldMaintainChainOfCustody() {
        UUID deploymentId = UUID.randomUUID();
        DeploymentJpaEntity deployment = new DeploymentJpaEntity(
                deploymentId,
                buildId,
                serviceId,
                envId,
                DeploymentStatus.RUNNING,
                null,
                Instant.now(),
                null
        );
        deploymentJpaRepository.saveAndFlush(deployment);

        // Trigger failure to produce linked incident
        updateDeploymentStatusUseCase.updateStatus(
                new UpdateDeploymentStatusCommand(deploymentId, DeploymentStatus.FAILED, "Cascading test failure")
        );

        IncidentJpaEntity incident = incidentJpaRepository.findByDeployId(deploymentId).orElseThrow();
        assertThat(incident.getDeployId()).isEqualTo(deploymentId);

        // Direct database verification via JDBC template
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM incidents WHERE deploy_id = ?",
                Integer.class,
                deploymentId
        );
        assertThat(count).isEqualTo(1);
    }
}
