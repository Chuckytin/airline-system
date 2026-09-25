package com.airline.controller;

import com.airline.enums.CabinClass;
import com.airline.enums.FareType;
import com.airline.payload.request.FareRequest;
import com.airline.payload.response.FareResponse;
import com.airline.service.FareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FareController.class)
@DisplayName("FareController Tests")
class FareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FareService fareService;

    private FareRequest validRequest;
    private FareResponse fareResponse;

    @BeforeEach
    void setUp() {
        validRequest = FareRequest.builder()
                .flightId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .fareType(FareType.STANDARD)
                .basePrice(new BigDecimal("100.00"))
                .taxes(new BigDecimal("21.00"))
                .currency("EUR")
                .validFrom(LocalDate.now())
                .build();

        fareResponse = FareResponse.builder()
                .id(1L)
                .flightId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .fareType(FareType.STANDARD)
                .basePrice(new BigDecimal("100.00"))
                .taxes(new BigDecimal("21.00"))
                .totalPrice(new BigDecimal("121.00"))
                .currency("EUR")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("POST /fares - Should create fare and return 201")
    void shouldCreateFare() throws Exception {
        when(fareService.createFare(any(FareRequest.class), eq(1L)))
                .thenReturn(fareResponse);

        mockMvc.perform(post("/api/v1/fares")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cabinClass").value("ECONOMY"))
                .andExpect(jsonPath("$.fareType").value("STANDARD"))
                .andExpect(jsonPath("$.totalPrice").value(121.00));
    }

    @Test
    @DisplayName("GET /fares/{id} - Should return fare")
    void shouldGetFareById() throws Exception {
        when(fareService.getFareById(1L)).thenReturn(fareResponse);

        mockMvc.perform(get("/api/v1/fares/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cabinClass").value("ECONOMY"));
    }

    @Test
    @DisplayName("PUT /fares/{id} - Should update fare")
    void shouldUpdateFare() throws Exception {
        when(fareService.updateFare(eq(1L), any(FareRequest.class), eq(1L)))
                .thenReturn(fareResponse);

        mockMvc.perform(put("/api/v1/fares/1")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /fares/{id} - Should return 204")
    void shouldDeleteFare() throws Exception {
        mockMvc.perform(delete("/api/v1/fares/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /fares - Should return 400 when X-User-Id is missing")
    void shouldReturn400WhenXUserIdMissing() throws Exception {
        mockMvc.perform(post("/api/v1/fares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

}