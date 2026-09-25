package com.airline.integration;

import com.airline.payload.request.CityRequest;
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
@DisplayName("City Integration Tests")
class CityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Full CRUD flow for City")
    void fullCrudFlow() throws Exception {
        // 1. CREATE
        CityRequest createRequest = CityRequest.builder()
                .name("Madrid")
                .cityCode("MAD")
                .countryCode("ES")
                .countryName("Spain")
                .regionCode("MD")
                .timeZoneOffset("+02:00")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cityCode").value("MAD"))
                .andReturn();

        CityResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                CityResponse.class
        );

        // 2. READ
        mockMvc.perform(get("/api/v1/cities/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.cityCode").value("MAD"));

        // 3. UPDATE
        CityRequest updateRequest = CityRequest.builder()
                .name("Madrid Capital")
                .cityCode("MAD")
                .countryCode("ES")
                .countryName("Spain")
                .regionCode("MD")
                .timeZoneOffset("+02:00")
                .build();

        mockMvc.perform(put("/api/v1/cities/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Madrid Capital"));

        // 4. DELETE
        mockMvc.perform(delete("/api/v1/cities/{id}", created.getId()))
                .andExpect(status().isOk());

        // 5. VERIFY DELETED
        mockMvc.perform(get("/api/v1/cities/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }
}