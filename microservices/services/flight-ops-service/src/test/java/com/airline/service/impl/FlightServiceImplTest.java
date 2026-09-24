package com.airline.service.impl;

import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.FlightMapper;
import com.airline.model.Flight;
import com.airline.payload.request.FlightRequest;
import com.airline.payload.response.FlightResponse;
import com.airline.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightServiceImpl Tests")
class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightMapper flightMapper;

    @InjectMocks
    private FlightServiceImpl flightService;

    private FlightRequest validRequest;
    private Flight flightEntity;
    private FlightResponse flightResponse;

    @BeforeEach
    void setUp() {
        validRequest = FlightRequest.builder()
                .flightNumber("AA123")
                .airlineId(1L)
                .departureAirportId(1L)
                .arrivalAirportId(2L)
                .estimatedDurationMinutes(75)
                .distanceKm(483)
                .build();

        flightEntity = Flight.builder()
                .id(1L)
                .flightNumber("AA123")
                .airlineId(1L)
                .departureAirportId(1L)
                .arrivalAirportId(2L)
                .estimatedDurationMinutes(75)
                .distanceKm(483)
                .build();

        flightResponse = FlightResponse.builder()
                .id(1L)
                .flightNumber("AA123")
                .airlineId(1L)
                .departureAirportId(1L)
                .arrivalAirportId(2L)
                .estimatedDurationMinutes(75)
                .distanceKm(483)
                .build();
    }

    @Nested
    @DisplayName("createFlight")
    class CreateFlightTests {

        @Test
        @DisplayName("Should create flight successfully")
        void shouldCreateFlightSuccessfully() {
            // Given
            when(flightRepository.existsByAirlineIdAndFlightNumber(1L, "AA123")).thenReturn(false);
            when(flightMapper.toEntity(validRequest)).thenReturn(flightEntity);
            when(flightRepository.save(flightEntity)).thenReturn(flightEntity);
            when(flightMapper.toResponse(flightEntity)).thenReturn(flightResponse);

            // When
            FlightResponse result = flightService.createFlight(validRequest, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFlightNumber()).isEqualTo("AA123");

            verify(flightRepository).existsByAirlineIdAndFlightNumber(1L, "AA123");
            verify(flightRepository).save(flightEntity);
        }

        @Test
        @DisplayName("Should throw ValidationException when departure and arrival airports are the same")
        void shouldThrowWhenDepartureAndArrivalAreSame() {
            // Given
            FlightRequest invalidRequest = FlightRequest.builder()
                    .flightNumber("AA123")
                    .airlineId(1L)
                    .departureAirportId(1L)
                    .arrivalAirportId(1L)   // ---> Mismo aeropuerto
                    .build();

            // When / Then
            assertThatThrownBy(() -> flightService.createFlight(invalidRequest, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Departure and arrival airports must be different");

            verify(flightRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceAlreadyExistsException when flight number already exists")
        void shouldThrowWhenFlightNumberExists() {
            // Given
            when(flightRepository.existsByAirlineIdAndFlightNumber(1L, "AA123")).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> flightService.createFlight(validRequest, 1L))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Flight already exists with flightNumber: AA123");

            verify(flightRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getFlightById")
    class GetFlightByIdTests {

        @Test
        @DisplayName("Should return flight when exists")
        void shouldReturnFlightWhenExists() {
            // Given
            when(flightRepository.findById(1L)).thenReturn(Optional.of(flightEntity));
            when(flightMapper.toResponse(flightEntity)).thenReturn(flightResponse);

            // When
            FlightResponse result = flightService.getFlightById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(flightRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not exists")
        void shouldThrowWhenNotFound() {
            // Given
            when(flightRepository.findById(999L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> flightService.getFlightById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Flight not found with id: 999");
        }
    }

    @Nested
    @DisplayName("updateFlight")
    class UpdateFlightTests {

        @Test
        @DisplayName("Should update flight successfully")
        void shouldUpdateFlightSuccessfully() {
            // Given
            FlightRequest updateRequest = FlightRequest.builder()
                    .flightNumber("AA123")
                    .airlineId(1L)
                    .departureAirportId(1L)
                    .arrivalAirportId(2L)
                    .estimatedDurationMinutes(80)
                    .distanceKm(490)
                    .build();

            when(flightRepository.findById(1L)).thenReturn(Optional.of(flightEntity));
            when(flightRepository.save(flightEntity)).thenReturn(flightEntity);
            when(flightMapper.toResponse(flightEntity)).thenReturn(flightResponse);

            // When
            FlightResponse result = flightService.updateFlight(1L, updateRequest, 1L);

            // Then
            assertThat(result).isNotNull();
            verify(flightMapper).updateEntity(flightEntity, updateRequest);
            verify(flightRepository).save(flightEntity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when flight not found")
        void shouldThrowWhenFlightNotFound() {
            // Given
            when(flightRepository.findById(999L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> flightService.updateFlight(999L, validRequest, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Flight not found with id: 999");
        }
    }

    @Nested
    @DisplayName("deleteFlight")
    class DeleteFlightTests {

        @Test
        @DisplayName("Should delete flight successfully")
        void shouldDeleteFlightSuccessfully() {
            // Given
            when(flightRepository.findById(1L)).thenReturn(Optional.of(flightEntity));

            // When
            flightService.deleteFlight(1L, 1L);

            // Then
            verify(flightRepository).delete(flightEntity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when flight not found")
        void shouldThrowWhenNotFound() {
            // Given
            when(flightRepository.findById(999L)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> flightService.deleteFlight(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Flight not found with id: 999");

            verify(flightRepository, never()).delete(any());
        }
    }

}