package com.eop.operations.infrastructure.web;

import com.eop.operations.application.GetDeploymentsService;
import com.eop.operations.application.UpdateDeploymentStatusService;
import com.eop.operations.infrastructure.web.dto.DeploymentResponse;
import com.eop.operations.infrastructure.web.dto.UpdateDeploymentStatusRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeploymentController.class)
@Import(GlobalExceptionHandler.class)
class DeploymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UpdateDeploymentStatusService updateStatusService;

    @MockitoBean
    private GetDeploymentsService getDeploymentsService;

    @Test
    void getDeployments_returnsList() throws Exception {
        UUID depId = UUID.randomUUID();
        when(getDeploymentsService.execute()).thenReturn(List.of(
                new DeploymentResponse(
                        depId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "RUNNING",
                        null,
                        Instant.now(),
                        null
                )
        ));

        mockMvc.perform(get("/api/v1/operations/deployments")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(depId.toString()))
                .andExpect(jsonPath("$[0].status").value("RUNNING"));
    }

    @Test
    void updateStatus_validRequest_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new UpdateDeploymentStatusRequest("SUCCESS", "ci-pipeline");

        mockMvc.perform(patch("/api/v1/operations/deployments/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(updateStatusService).execute(id, "SUCCESS", "ci-pipeline");
    }

    @Test
    void updateStatus_invalidStateTransition_returnsConflict() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new UpdateDeploymentStatusRequest("FAILED", "ci-pipeline");

        doThrow(new IllegalStateException("Invalid transition from terminal state"))
                .when(updateStatusService).execute(eq(id), anyString(), anyString());

        mockMvc.perform(patch("/api/v1/operations/deployments/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("State Transition Conflict"))
                .andExpect(jsonPath("$.detail").value("Invalid transition from terminal state"));
    }

    @Test
    void updateStatus_missingStatus_returnsBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        String jsonPayload = "{\"triggeredBy\": \"ci-pipeline\"}";

        mockMvc.perform(patch("/api/v1/operations/deployments/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }
}
