package com.airline.controller;

import com.airline.payload.request.CityRequest;
import com.airline.payload.response.CityResponse;
import com.airline.service.CityService;
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

@WebMvcTest(CityController.class)
@DisplayName("CityController Tests")
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CityService cityService;

    private CityRequest validRequest;
    private CityResponse cityResponse;

    @BeforeEach
    void setUp() {
        validRequest = CityRequest.builder()
                .name("Madrid")
                .cityCode("MAD")
                .countryCode("ES")
                .countryName("Spain")
                .regionCode("MD")
                .timeZoneOffset("+02:00")
                .build();

        cityResponse = CityResponse.builder()
                .id(1L)
                .name("Madrid")
                .cityCode("MAD")
                .countryCode("ES")
                .countryName("Spain")
                .regionCode("MD")
                .timeZoneOffset("+02:00")
                .build();
    }

    @Test
    @DisplayName("POST /cities - Should create city and return 201")
    void shouldCreateCity() throws Exception {
        when(cityService.createCity(any(CityRequest.class))).thenReturn(cityResponse);

        mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cityCode").value("MAD"));
    }

    @Test
    @DisplayName("GET /cities/{id} - Should return city")
    void shouldGetCityById() throws Exception {
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        mockMvc.perform(get("/api/v1/cities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cityCode").value("MAD"));
    }

    @Test
    @DisplayName("PUT /cities/{id} - Should update city")
    void shouldUpdateCity() throws Exception {
        when(cityService.updateCity(eq(1L), any(CityRequest.class))).thenReturn(cityResponse);

        mockMvc.perform(put("/api/v1/cities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /cities/{id} - Should delete city and return 200")
    void shouldDeleteCity() throws Exception {
        mockMvc.perform(delete("/api/v1/cities/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /cities - Should return paginated cities")
    void shouldGetAllCities() throws Exception {
        Page<CityResponse> page = new PageImpl<>(List.of(cityResponse));
        when(cityService.getAllCities(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/cities")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cityCode").value("MAD"));
    }

    @Test
    @DisplayName("GET /cities/search - Should return cities matching keyword")
    void shouldSearchCities() throws Exception {
        Page<CityResponse> page = new PageImpl<>(List.of(cityResponse));
        when(cityService.searchCities(eq("mad"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/cities/search")
                        .param("keyword", "mad")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cityCode").value("MAD"));
    }

    @Test
    @DisplayName("GET /cities/country/{countryCode} - Should return cities by country")
    void shouldGetCitiesByCountry() throws Exception {
        Page<CityResponse> page = new PageImpl<>(List.of(cityResponse));
        when(cityService.getCitiesByCountryCode(eq("ES"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/cities/country/ES")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cityCode").value("MAD"));
    }

    @Test
    @DisplayName("GET /cities/exists - Should return boolean")
    void shouldCheckCityExists() throws Exception {
        when(cityService.cityExists("MAD")).thenReturn(true);

        mockMvc.perform(get("/api/v1/cities/exists")
                        .param("cityCode", "MAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /cities - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        CityRequest invalidRequest = CityRequest.builder()
                .name("")           // @NotBlank
                .cityCode("XX")     // @Size(min=3)
                .countryCode("E")   // @Size(min=2)
                .countryName("")    // @NotBlank
                .build();

        mockMvc.perform(post("/api/v1/cities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}