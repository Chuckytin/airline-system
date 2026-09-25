package com.airline.service.impl;

import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.mapper.AirportMapper;
import com.airline.model.Airport;
import com.airline.model.City;
import com.airline.payload.request.AirportRequest;
import com.airline.payload.response.AirportResponse;
import com.airline.payload.response.CityResponse;
import com.airline.repository.AirportRepository;
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
@DisplayName("AirportServiceImpl Tests")
class AirportServiceImplTest {

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private AirportMapper airportMapper;

    @InjectMocks
    private AirportServiceImpl airportService;

    private AirportRequest validRequest;
    private Airport airportEntity;
    private AirportResponse airportResponse;
    private City city;

    @BeforeEach
    void setUp() {
        city = City.builder()
                .id(1L)
                .name("Madrid")
                .cityCode("MAD")
                .countryCode("ES")
                .countryName("Spain")
                .build();

        validRequest = AirportRequest.builder()
                .iataCode("MAD")
                .name("Adolfo Suárez Madrid-Barajas")
                .timeZone("Europe/Madrid")
                .cityId(1L)
                .build();

        airportEntity = Airport.builder()
                .id(1L)
                .iataCode("MAD")
                .name("Adolfo Suárez Madrid-Barajas")
                .timeZone("Europe/Madrid")
                .city(city)
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

    @Nested
    @DisplayName("createAirport")
    class CreateAirportTests {

        @Test
        @DisplayName("Should create airport successfully")
        void shouldCreateAirportSuccessfully() {
            when(airportRepository.existsByIataCode("MAD")).thenReturn(false);
            when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
            when(airportMapper.toEntity(validRequest)).thenReturn(airportEntity);
            when(airportRepository.save(airportEntity)).thenReturn(airportEntity);
            when(airportMapper.toResponse(airportEntity)).thenReturn(airportResponse);

            AirportResponse result = airportService.createAirport(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getIataCode()).isEqualTo("MAD");

            verify(airportRepository).existsByIataCode("MAD");
            verify(cityRepository).findById(1L);
            verify(airportRepository).save(airportEntity);
        }

        @Test
        @DisplayName("Should throw ResourceAlreadyExistsException when IATA already exists")
        void shouldThrowWhenIataExists() {
            when(airportRepository.existsByIataCode("MAD")).thenReturn(true);

            assertThatThrownBy(() -> airportService.createAirport(validRequest))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("MAD");

            verify(airportRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when city not found")
        void shouldThrowWhenCityNotFound() {
            when(airportRepository.existsByIataCode("MAD")).thenReturn(false);
            when(cityRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airportService.createAirport(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("1");

            verify(airportRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAirportById")
    class GetAirportByIdTests {

        @Test
        @DisplayName("Should return airport when exists")
        void shouldReturnAirportWhenExists() {
            when(airportRepository.findById(1L)).thenReturn(Optional.of(airportEntity));
            when(airportMapper.toResponse(airportEntity)).thenReturn(airportResponse);

            AirportResponse result = airportService.getAirportById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not exists")
        void shouldThrowWhenNotFound() {
            when(airportRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airportService.getAirportById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("updateAirport")
    class UpdateAirportTests {

        @Test
        @DisplayName("Should update airport successfully without changing IATA")
        void shouldUpdateAirportSuccessfully() {
            AirportRequest updateRequest = AirportRequest.builder()
                    .iataCode("MAD")
                    .name("Madrid-Barajas Updated")
                    .timeZone("Europe/Madrid")
                    .cityId(1L)
                    .build();

            when(airportRepository.findById(1L)).thenReturn(Optional.of(airportEntity));
            when(airportRepository.save(airportEntity)).thenReturn(airportEntity);
            when(airportMapper.toResponse(airportEntity)).thenReturn(airportResponse);

            AirportResponse result = airportService.updateAirport(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(airportMapper).updateEntity(airportEntity, updateRequest);
            verify(airportRepository).save(airportEntity);
        }

        @Test
        @DisplayName("Should throw ResourceAlreadyExistsException when new IATA already exists")
        void shouldThrowWhenNewIataExists() {
            AirportRequest updateRequest = AirportRequest.builder()
                    .iataCode("BCN")
                    .name("Madrid-Barajas")
                    .cityId(1L)
                    .build();

            when(airportRepository.findById(1L)).thenReturn(Optional.of(airportEntity));
            when(airportRepository.existsByIataCode("BCN")).thenReturn(true);

            assertThatThrownBy(() -> airportService.updateAirport(1L, updateRequest))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("BCN");

            verify(airportRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when airport not found")
        void shouldThrowWhenAirportNotFound() {
            when(airportRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airportService.updateAirport(999L, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("deleteAirportById")
    class DeleteAirportTests {

        @Test
        @DisplayName("Should delete airport successfully")
        void shouldDeleteAirportSuccessfully() {
            when(airportRepository.existsById(1L)).thenReturn(true);

            airportService.deleteAirportById(1L);

            verify(airportRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when airport not found")
        void shouldThrowWhenNotFound() {
            when(airportRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> airportService.deleteAirportById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(airportRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getAirportsByCityId")
    class GetAirportsByCityTests {

        @Test
        @DisplayName("Should return airports by cityId paginated")
        void shouldReturnAirportsByCityId() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Airport> page = new PageImpl<>(List.of(airportEntity));

            when(cityRepository.existsById(1L)).thenReturn(true);
            when(airportRepository.findByCityId(1L, pageable)).thenReturn(page);
            when(airportMapper.toResponse(airportEntity)).thenReturn(airportResponse);

            Page<AirportResponse> result = airportService.getAirportsByCityId(1L, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when city not found")
        void shouldThrowWhenCityNotFound() {
            Pageable pageable = PageRequest.of(0, 10);

            when(cityRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> airportService.getAirportsByCityId(999L, pageable))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(airportRepository, never()).findByCityId(any(), any());
        }
    }
}