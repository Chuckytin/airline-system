package com.airline.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Airline Integration Tests")
class AirlineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Full flow: create → read → update → delete")
    void fullAirlineFlow() throws Exception {
        // CREATE
        mockMvc.perform(post("/api/v1/airlines")
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
                .andExpect(jsonPath("$.iataCode").value("AA"))
                .andExpect(jsonPath("$.ownerId").value(1));

        // READ
        mockMvc.perform(get("/api/v1/airlines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iataCode").value("AA"));

        // UPDATE
        mockMvc.perform(put("/api/v1/airlines/1")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "iataCode": "AA",
                                    "icaoCode": "AAL",
                                    "name": "American Airlines Updated",
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("American Airlines Updated"));

        // DELETE
        mockMvc.perform(delete("/api/v1/airlines/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());
    }

}