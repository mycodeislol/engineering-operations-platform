package com.eop.operations.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
@Import(GlobalExceptionHandler.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getIncidents_withCompositeFilters_returnsOk() throws Exception {
        UUID serviceId = UUID.randomUUID();
        UUID envId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/operations/incidents")
                .param("serviceId", serviceId.toString())
                .param("envId", envId.toString())
                .param("severity", "SEV2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
