package com.airline.controller;

import com.airline.enums.AircraftStatus;
import com.airline.payload.response.AircraftResponse;
import com.airline.service.AircraftService;
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

@WebMvcTest(AircraftController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AircraftController Tests")
class AircraftControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AircraftService aircraftService;

    private AircraftResponse aircraftResponse;

    @BeforeEach
    void setUp() {
        aircraftResponse = AircraftResponse.builder()
                .id(1L)
                .code("EC-AAA")
                .model("Boeing 737-800")
                .manufacturer("Boeing")
                .totalSeats(166)
                .status(AircraftStatus.ACTIVE)
                .available(true)
                .operational(true)
                .airlineId(1L)
                .build();
    }

    @Test
    @DisplayName("POST /aircraft - Should return 201")
    void shouldCreateAircraft() throws Exception {
        when(aircraftService.createAircraft(any(), eq(1L))).thenReturn(aircraftResponse);

        mockMvc.perform(post("/api/v1/aircraft")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "EC-AAA",
                                    "model": "Boeing 737-800",
                                    "manufacturer": "Boeing",
                                    "economySeats": 150,
                                    "businessSeats": 16,
                                    "airlineId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("EC-AAA"));
    }

    @Test
    @DisplayName("GET /aircraft/{id} - Should return 200")
    void shouldGetAircraftById() throws Exception {
        when(aircraftService.getAircraftById(1L)).thenReturn(aircraftResponse);

        mockMvc.perform(get("/api/v1/aircraft/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EC-AAA"));
    }

    @Test
    @DisplayName("GET /aircraft/code/{code} - Should return 200")
    void shouldGetAircraftByCode() throws Exception {
        when(aircraftService.getAircraftByCode("EC-AAA")).thenReturn(aircraftResponse);

        mockMvc.perform(get("/api/v1/aircraft/code/EC-AAA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EC-AAA"));
    }

    @Test
    @DisplayName("PATCH /aircraft/{id}/status - Should change status")
    void shouldChangeStatus() throws Exception {
        when(aircraftService.changeStatus(eq(1L), eq(AircraftStatus.MAINTENANCE), eq(1L)))
                .thenReturn(aircraftResponse);

        mockMvc.perform(patch("/api/v1/aircraft/1/status?status=MAINTENANCE")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk());
    }

}