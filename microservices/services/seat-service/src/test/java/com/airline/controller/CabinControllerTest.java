package com.airline.controller;

import com.airline.enums.CabinClass;
import com.airline.payload.request.CabinRequest;
import com.airline.payload.response.CabinAvailabilityResponse;
import com.airline.payload.response.CabinResponse;
import com.airline.service.CabinService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CabinController.class)
@DisplayName("CabinController Tests")
class CabinControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CabinService cabinService;

    private CabinRequest validRequest;
    private CabinResponse cabinResponse;

    @BeforeEach
    void setUp() {
        validRequest = CabinRequest.builder()
                .flightInstanceId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .rowStart(1)
                .rowEnd(10)
                .columnLayout("ABC-DEF")
                .basePrice(new BigDecimal("50.00"))
                .build();

        cabinResponse = CabinResponse.builder()
                .id(1L)
                .flightInstanceId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .rowStart(1)
                .rowEnd(10)
                .columnLayout("ABC-DEF")
                .totalSeats(60)
                .availableSeats(60)
                .currency("EUR")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("POST /cabins - Should create cabin and return 201")
    void shouldCreateCabin() throws Exception {
        when(cabinService.createCabin(any(CabinRequest.class), eq(1L))).thenReturn(cabinResponse);

        mockMvc.perform(post("/api/v1/cabins")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.totalSeats").value(60));
    }

    @Test
    @DisplayName("POST /cabins - Should return 400 when layout invalid")
    void shouldReturn400WhenInvalidLayout() throws Exception {
        CabinRequest invalid = CabinRequest.builder()
                .flightInstanceId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .rowStart(1)
                .rowEnd(10)
                .columnLayout("abc-def")
                .basePrice(BigDecimal.TEN)
                .build();

        mockMvc.perform(post("/api/v1/cabins")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /cabins/{id} - Should return cabin")
    void shouldGetCabinById() throws Exception {
        when(cabinService.getCabinById(1L)).thenReturn(cabinResponse);

        mockMvc.perform(get("/api/v1/cabins/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cabinClass").value("ECONOMY"));
    }

    @Test
    @DisplayName("GET /cabins/{id}/availability - Should return availability")
    void shouldGetAvailability() throws Exception {
        CabinAvailabilityResponse availability = CabinAvailabilityResponse.builder()
                .cabinId(1L)
                .flightInstanceId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(60)
                .availableSeats(40)
                .occupiedSeats(15)
                .blockedSeats(3)
                .reservedSeats(2)
                .build();

        when(cabinService.getAvailability(1L)).thenReturn(availability);

        mockMvc.perform(get("/api/v1/cabins/1/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats").value(40))
                .andExpect(jsonPath("$.occupiedSeats").value(15));
    }

    @Test
    @DisplayName("GET /cabins?flightInstanceId=X - Should return list")
    void shouldGetByFlightInstance() throws Exception {
        when(cabinService.getCabinsByFlightInstance(1L)).thenReturn(List.of(cabinResponse));

        mockMvc.perform(get("/api/v1/cabins").param("flightInstanceId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("PUT /cabins/{id} - Should update")
    void shouldUpdateCabin() throws Exception {
        when(cabinService.updateCabin(eq(1L), any(CabinRequest.class), eq(1L))).thenReturn(cabinResponse);

        mockMvc.perform(put("/api/v1/cabins/1")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("PATCH /cabins/{id}/active - Should change active status")
    void shouldChangeActive() throws Exception {
        when(cabinService.changeActiveStatus(1L, false, 1L)).thenReturn(cabinResponse);

        mockMvc.perform(patch("/api/v1/cabins/1/active")
                        .param("active", "false")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /cabins/{id} - Should return 204")
    void shouldDeleteCabin() throws Exception {
        mockMvc.perform(delete("/api/v1/cabins/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());

        verify(cabinService).deleteCabin(1L, 1L);
    }

    @Test
    @DisplayName("POST /cabins - Should return 400 when X-User-Id is missing")
    void shouldReturn400WhenUserIdMissing() throws Exception {
        mockMvc.perform(post("/api/v1/cabins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
}