package com.airline.integration;

import com.airline.payload.request.FlightRequest;
import com.airline.payload.response.FlightResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Flight Integration Tests")
class FlightIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Full CRUD flow for Flight")
    void fullCrudFlow() throws Exception {
        // 1. CREATE
        FlightRequest createRequest = FlightRequest.builder()
                .flightNumber("AA123")
                .airlineId(1L)
                .departureAirportId(1L)
                .arrivalAirportId(2L)
                .estimatedDurationMinutes(75)
                .distanceKm(483)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/flights")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("AA123"))
                .andReturn();

        FlightResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                FlightResponse.class
        );

        // 2. READ
        mockMvc.perform(get("/api/v1/flights/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.flightNumber").value("AA123"));

        // 3. UPDATE
        FlightRequest updateRequest = FlightRequest.builder()
                .flightNumber("AA123")
                .airlineId(1L)
                .departureAirportId(1L)
                .arrivalAirportId(2L)
                .estimatedDurationMinutes(80)
                .distanceKm(490)
                .build();

        mockMvc.perform(put("/api/v1/flights/{id}", created.getId())
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedDurationMinutes").value(80));

        // 4. DELETE
        mockMvc.perform(delete("/api/v1/flights/{id}", created.getId())
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());

        // 5. VERIFY DELETED
        mockMvc.perform(get("/api/v1/flights/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

}