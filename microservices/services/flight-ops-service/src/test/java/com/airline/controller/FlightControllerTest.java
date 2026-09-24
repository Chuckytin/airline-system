package com.airline.controller;

import com.airline.payload.request.FlightRequest;
import com.airline.payload.response.FlightResponse;
import com.airline.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FlightController.class)
@DisplayName("FlightController Tests")
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FlightService flightService;

    private FlightRequest validRequest;
    private FlightResponse flightResponse;

    @BeforeEach
    void setUp() {
        validRequest = FlightRequest.builder()
                .flightNumber("AA123")
                .airlineId(1L)
                .departureAirportId(1L)
                .arrivalAirportId(2L)
                .estimatedDurationMinutes(75)
                .distanceKm(483)
                .build();

        flightResponse = FlightResponse.builder()
                .id(1L)
                .flightNumber("AA123")
                .airlineId(1L)
                .departureAirportId(1L)
                .arrivalAirportId(2L)
                .estimatedDurationMinutes(75)
                .distanceKm(483)
                .build();
    }

    @Test
    @DisplayName("POST /flights - Should create flight and return 201")
    void shouldCreateFlight() throws Exception {
        // Given
        when(flightService.createFlight(any(FlightRequest.class), eq(1L)))
                .thenReturn(flightResponse);

        // When / Then
        mockMvc.perform(post("/api/v1/flights")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.flightNumber").value("AA123"))
                .andExpect(jsonPath("$.airlineId").value(1L));
    }

    @Test
    @DisplayName("GET /flights/{id} - Should return flight")
    void shouldGetFlightById() throws Exception {
        // Given
        when(flightService.getFlightById(1L)).thenReturn(flightResponse);

        // When / Then
        mockMvc.perform(get("/api/v1/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.flightNumber").value("AA123"));
    }

    @Test
    @DisplayName("PUT /flights/{id} - Should update flight")
    void shouldUpdateFlight() throws Exception {
        // Given
        when(flightService.updateFlight(eq(1L), any(FlightRequest.class), eq(1L)))
                .thenReturn(flightResponse);

        // When / Then
        mockMvc.perform(put("/api/v1/flights/1")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /flights/{id} - Should delete flight and return 204")
    void shouldDeleteFlight() throws Exception {
        // When / Then
        mockMvc.perform(delete("/api/v1/flights/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /flights - Should return 400 when X-User-Id is missing")
    void shouldReturn400WhenXUserIdMissing() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/v1/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /flights - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Given
        FlightRequest invalidRequest = FlightRequest.builder()
                .flightNumber("")   // <--- @NotBlank
                .airlineId(null)    // <--- @NotNull
                .build();

        // When / Then
        mockMvc.perform(post("/api/v1/flights")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

}