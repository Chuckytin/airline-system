package com.airline.integration;

import com.airline.payload.request.AirportRequest;
import com.airline.payload.request.CityRequest;
import com.airline.payload.response.AirportResponse;
import com.airline.payload.response.CityResponse;
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
@DisplayName("Airport Integration Tests")
class AirportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Full CRUD flow for Airport")
    void fullCrudFlow() throws Exception {
        // 1. Crear City (necesaria para el Airport)
        CityRequest cityRequest = CityRequest.builder()
                .name("Madrid")
                .cityCode("MAD")
                .countryCode("ES")
                .countryName("Spain")
                .build();

        MvcResult cityResult = mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cityRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        CityResponse city = objectMapper.readValue(
                cityResult.getResponse().getContentAsString(),
                CityResponse.class
        );

        // 2. CREATE Airport
        AirportRequest createRequest = AirportRequest.builder()
                .iataCode("MAD")
                .name("Adolfo Suárez Madrid-Barajas")
                .timeZone("Europe/Madrid")
                .cityId(city.getId())
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iataCode").value("MAD"))
                .andExpect(jsonPath("$.city.id").value(city.getId()))
                .andReturn();

        AirportResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                AirportResponse.class
        );

        // 3. READ
        mockMvc.perform(get("/api/v1/airports/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.iataCode").value("MAD"));

        // 4. UPDATE
        AirportRequest updateRequest = AirportRequest.builder()
                .iataCode("MAD")
                .name("Madrid-Barajas Updated")
                .timeZone("Europe/Madrid")
                .cityId(city.getId())
                .build();

        mockMvc.perform(put("/api/v1/airports/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Madrid-Barajas Updated"));

        // 5. DELETE
        mockMvc.perform(delete("/api/v1/airports/{id}", created.getId()))
                .andExpect(status().isOk());

        // 6. VERIFY DELETED
        mockMvc.perform(get("/api/v1/airports/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }
}