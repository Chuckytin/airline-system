package com.airline.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Aircraft Integration Tests")
class AircraftIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Full flow: create airline → create aircraft → read → change status → delete")
    void fullAircraftFlow() throws Exception {

        // 1. Crear airline y capturar su ID real
        MvcResult airlineResult = mockMvc.perform(post("/api/v1/airlines")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "iataCode": "AA",
                                    "icaoCode": "AAL",
                                    "name": "American Airlines",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode airlineJson = objectMapper.readTree(airlineResult.getResponse().getContentAsString());
        long airlineId = airlineJson.get("id").asLong();

        // 2. Crear aircraft usando el airlineId real
        MvcResult aircraftResult = mockMvc.perform(post("/api/v1/aircraft")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "EC-AAA",
                                    "model": "Boeing 737-800",
                                    "manufacturer": "Boeing",
                                    "economySeats": 150,
                                    "businessSeats": 16,
                                    "airlineId": %d
                                }
                                """.formatted(airlineId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("EC-AAA"))
                .andExpect(jsonPath("$.totalSeats").value(166))
                .andReturn();

        JsonNode aircraftJson = objectMapper.readTree(aircraftResult.getResponse().getContentAsString());
        long aircraftId = aircraftJson.get("id").asLong();

        // 3. READ usando el id real
        mockMvc.perform(get("/api/v1/aircraft/" + aircraftId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EC-AAA"));

        // 4. CHANGE STATUS
        mockMvc.perform(patch("/api/v1/aircraft/" + aircraftId + "/status?status=MAINTENANCE")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"))
                .andExpect(jsonPath("$.operational").value(false));

        // 5. DELETE
        mockMvc.perform(delete("/api/v1/aircraft/" + aircraftId)
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());
    }
}