package com.eop.operations.infrastructure.web;

import com.eop.operations.application.GetIncidentsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
@Import(GlobalExceptionHandler.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetIncidentsService getIncidentsService;

    @Test
    void getIncidents_withCompositeFilters_returnsOk() throws Exception {
        UUID serviceId = UUID.randomUUID();
        UUID envId = UUID.randomUUID();

        when(getIncidentsService.execute(eq(serviceId), eq(envId), eq("SEV2"))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/operations/incidents")
                .param("serviceId", serviceId.toString())
                .param("envId", envId.toString())
                .param("severity", "SEV2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
