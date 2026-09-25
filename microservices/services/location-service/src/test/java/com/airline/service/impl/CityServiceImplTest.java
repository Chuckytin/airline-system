package com.airline.service.impl;

import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.mapper.CityMapper;
import com.airline.model.City;
import com.airline.payload.request.CityRequest;
import com.airline.payload.response.CityResponse;
import com.airline.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CityServiceImpl Tests")
class CityServiceImplTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityMapper cityMapper;

    @InjectMocks
    private CityServiceImpl cityService;

    private CityRequest validRequest;
    private City cityEntity;
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

        cityEntity = City.builder()
                .id(1L)
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

    @Nested
    @DisplayName("createCity")
    class CreateCityTests {

        @Test
        @DisplayName("Should create city successfully")
        void shouldCreateCitySuccessfully() {
            when(cityRepository.existsByCityCode("MAD")).thenReturn(false);
            when(cityMapper.toEntity(validRequest)).thenReturn(cityEntity);
            when(cityRepository.save(cityEntity)).thenReturn(cityEntity);
            when(cityMapper.toResponse(cityEntity)).thenReturn(cityResponse);

            CityResponse result = cityService.createCity(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCityCode()).isEqualTo("MAD");

            verify(cityRepository).existsByCityCode("MAD");
            verify(cityRepository).save(cityEntity);
        }

        @Test
        @DisplayName("Should throw ResourceAlreadyExistsException when cityCode already exists")
        void shouldThrowWhenCityCodeExists() {
            when(cityRepository.existsByCityCode("MAD")).thenReturn(true);

            assertThatThrownBy(() -> cityService.createCity(validRequest))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("MAD");

            verify(cityRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getCityById")
    class GetCityByIdTests {

        @Test
        @DisplayName("Should return city when exists")
        void shouldReturnCityWhenExists() {
            when(cityRepository.findById(1L)).thenReturn(Optional.of(cityEntity));
            when(cityMapper.toResponse(cityEntity)).thenReturn(cityResponse);

            CityResponse result = cityService.getCityById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(cityRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not exists")
        void shouldThrowWhenNotFound() {
            when(cityRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cityService.getCityById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("updateCity")
    class UpdateCityTests {

        @Test
        @DisplayName("Should update city successfully")
        void shouldUpdateCitySuccessfully() {
            CityRequest updateRequest = CityRequest.builder()
                    .name("Madrid Updated")
                    .cityCode("MAD")
                    .countryCode("ES")
                    .countryName("Spain")
                    .build();

            when(cityRepository.findById(1L)).thenReturn(Optional.of(cityEntity));
            when(cityRepository.save(cityEntity)).thenReturn(cityEntity);
            when(cityMapper.toResponse(cityEntity)).thenReturn(cityResponse);

            CityResponse result = cityService.updateCity(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(cityMapper).updateEntity(cityEntity, updateRequest);
            verify(cityRepository).save(cityEntity);
        }

        @Test
        @DisplayName("Should throw ResourceAlreadyExistsException when new cityCode already exists")
        void shouldThrowWhenNewCityCodeExists() {
            CityRequest updateRequest = CityRequest.builder()
                    .name("Madrid")
                    .cityCode("BCN") // distinto al actual
                    .countryCode("ES")
                    .countryName("Spain")
                    .build();

            when(cityRepository.findById(1L)).thenReturn(Optional.of(cityEntity));
            when(cityRepository.existsByCityCode("BCN")).thenReturn(true);

            assertThatThrownBy(() -> cityService.updateCity(1L, updateRequest))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("BCN");

            verify(cityRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when city not found")
        void shouldThrowWhenCityNotFound() {
            when(cityRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cityService.updateCity(999L, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("deleteCityById")
    class DeleteCityTests {

        @Test
        @DisplayName("Should delete city successfully")
        void shouldDeleteCitySuccessfully() {
            when(cityRepository.existsById(1L)).thenReturn(true);

            cityService.deleteCityById(1L);

            verify(cityRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when city not found")
        void shouldThrowWhenNotFound() {
            when(cityRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> cityService.deleteCityById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(cityRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getAllCities / search / byCountryCode / exists")
    class QueryTests {

        @Test
        @DisplayName("Should return all cities paginated")
        void shouldReturnAllCities() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<City> page = new PageImpl<>(List.of(cityEntity));

            when(cityRepository.findAll(pageable)).thenReturn(page);
            when(cityMapper.toResponse(cityEntity)).thenReturn(cityResponse);

            Page<CityResponse> result = cityService.getAllCities(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getCityCode()).isEqualTo("MAD");
        }

        @Test
        @DisplayName("Should search cities by keyword")
        void shouldSearchCities() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<City> page = new PageImpl<>(List.of(cityEntity));

            when(cityRepository.searchByKeyword("mad", pageable)).thenReturn(page);
            when(cityMapper.toResponse(cityEntity)).thenReturn(cityResponse);

            Page<CityResponse> result = cityService.searchCities("mad", pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return cities by countryCode")
        void shouldReturnCitiesByCountryCode() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<City> page = new PageImpl<>(List.of(cityEntity));

            when(cityRepository.findByCountryCodeIgnoreCase("ES", pageable)).thenReturn(page);
            when(cityMapper.toResponse(cityEntity)).thenReturn(cityResponse);

            Page<CityResponse> result = cityService.getCitiesByCountryCode("ES", pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return true when city exists")
        void shouldReturnTrueWhenCityExists() {
            when(cityRepository.existsByCityCode("MAD")).thenReturn(true);

            assertThat(cityService.cityExists("MAD")).isTrue();
        }
    }
}