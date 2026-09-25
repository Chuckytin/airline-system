package com.airline.controller;

import com.airline.enums.AirlineStatus;
import com.airline.payload.response.AirlineResponse;
import com.airline.service.AirlineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirlineController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AirlineController Tests")
class AirlineControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AirlineService airlineService;

    private AirlineResponse airlineResponse;

    @BeforeEach
    void setUp() {
        airlineResponse = AirlineResponse.builder()
                .id(1L)
                .iataCode("AA")
                .icaoCode("AAL")
                .name("American Airlines")
                .status(AirlineStatus.ACTIVE)
                .ownerId(1L)
                .build();
    }

    @Test
    @DisplayName("POST /airlines - Should return 201")
    void shouldCreateAirline() throws Exception {
        when(airlineService.createAirline(any(), eq(1L))).thenReturn(airlineResponse);

        mockMvc.perform(post("/api/v1/airlines")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "iataCode": "AA",
                                    "icaoCode": "AAL",
                                    "name": "American Airlines"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iataCode").value("AA"))
                .andExpect(jsonPath("$.name").value("American Airlines"));
    }

    @Test
    @DisplayName("GET /airlines/{id} - Should return 200")
    void shouldGetAirlineById() throws Exception {
        when(airlineService.getAirlineById(1L)).thenReturn(airlineResponse);

        mockMvc.perform(get("/api/v1/airlines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("AA"));
    }

    @Test
    @DisplayName("GET /airlines/dropdown - Should return list")
    void shouldGetDropdown() throws Exception {
        when(airlineService.getAirlinesForDropdown()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/airlines/dropdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("DELETE /airlines/{id} - Should return 204")
    void shouldDeleteAirline() throws Exception {
        mockMvc.perform(delete("/api/v1/airlines/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());
    }

}