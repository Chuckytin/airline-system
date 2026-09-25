package com.airline.controller;

import com.airline.payload.request.AirportRequest;
import com.airline.payload.response.AirportResponse;
import com.airline.payload.response.CityResponse;
import com.airline.service.AirportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
@DisplayName("AirportController Tests")
class AirportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AirportService airportService;

    private AirportRequest validRequest;
    private AirportResponse airportResponse;

    @BeforeEach
    void setUp() {
        validRequest = AirportRequest.builder()
                .iataCode("MAD")
                .name("Adolfo Suárez Madrid-Barajas")
                .timeZone("Europe/Madrid")
                .cityId(1L)
                .build();

        airportResponse = AirportResponse.builder()
                .id(1L)
                .iataCode("MAD")
                .name("Adolfo Suárez Madrid-Barajas")
                .timeZone("Europe/Madrid")
                .city(CityResponse.builder()
                        .id(1L)
                        .name("Madrid")
                        .cityCode("MAD")
                        .countryCode("ES")
                        .countryName("Spain")
                        .build())
                .build();
    }

    @Test
    @DisplayName("POST /airports - Should create airport and return 201")
    void shouldCreateAirport() throws Exception {
        when(airportService.createAirport(any(AirportRequest.class))).thenReturn(airportResponse);

        mockMvc.perform(post("/api/v1/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.iataCode").value("MAD"))
                .andExpect(jsonPath("$.detailedName").value("ADOLFO SUÁREZ MADRID-BARAJAS/ES"));
    }

    @Test
    @DisplayName("GET /airports/{id} - Should return airport")
    void shouldGetAirportById() throws Exception {
        when(airportService.getAirportById(1L)).thenReturn(airportResponse);

        mockMvc.perform(get("/api/v1/airports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.iataCode").value("MAD"));
    }

    @Test
    @DisplayName("PUT /airports/{id} - Should update airport")
    void shouldUpdateAirport() throws Exception {
        when(airportService.updateAirport(eq(1L), any(AirportRequest.class))).thenReturn(airportResponse);

        mockMvc.perform(put("/api/v1/airports/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /airports/{id} - Should delete airport")
    void shouldDeleteAirport() throws Exception {
        mockMvc.perform(delete("/api/v1/airports/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /airports - Should return paginated airports")
    void shouldGetAllAirports() throws Exception {
        Page<AirportResponse> page = new PageImpl<>(List.of(airportResponse));
        when(airportService.getAllAirports(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/airports")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].iataCode").value("MAD"));
    }

    @Test
    @DisplayName("GET /airports/city/{cityId} - Should return airports by city")
    void shouldGetAirportsByCity() throws Exception {
        Page<AirportResponse> page = new PageImpl<>(List.of(airportResponse));
        when(airportService.getAirportsByCityId(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/airports/city/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].iataCode").value("MAD"));
    }

    @Test
    @DisplayName("POST /airports - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        AirportRequest invalidRequest = AirportRequest.builder()
                .iataCode("XX")          // @Size(min=3)
                .name("")                // @NotBlank
                .timeZone("BadFormat")   // @Pattern
                .cityId(null)            // @NotNull
                .build();

        mockMvc.perform(post("/api/v1/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}