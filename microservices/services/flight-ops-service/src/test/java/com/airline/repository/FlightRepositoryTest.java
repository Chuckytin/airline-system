package com.airline.repository;

import com.airline.model.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("FlightRepository Tests")
class FlightRepositoryTest {

    @Autowired
    private FlightRepository flightRepository;

    private Flight flight;

    @BeforeEach
    void setUp() {
        flight = Flight.builder()
                .flightNumber("AA123")
                .airlineId(1L)
                .departureAirportId(1L)
                .arrivalAirportId(2L)
                .estimatedDurationMinutes(75)
                .distanceKm(483)
                .build();
    }

    @Test
    @DisplayName("Should save and find flight by ID")
    void shouldSaveAndFindById() {
        // When
        Flight saved = flightRepository.save(flight);

        // Then
        assertThat(saved.getId()).isNotNull();
        Optional<Flight> found = flightRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFlightNumber()).isEqualTo("AA123");
    }

    @Test
    @DisplayName("Should find flight by flight number")
    void shouldFindByFlightNumber() {
        // Given
        flightRepository.save(flight);

        // When
        Optional<Flight> found = flightRepository.findByFlightNumber("AA123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFlightNumber()).isEqualTo("AA123");
    }

    @Test
    @DisplayName("Should return empty when flight number not found")
    void shouldReturnEmptyWhenNotFound() {
        // When
        Optional<Flight> found = flightRepository.findByFlightNumber("XX999");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check exists by airlineId and flightNumber")
    void shouldCheckExistsByAirlineIdAndFlightNumber() {
        // Given
        flightRepository.save(flight);

        // When
        boolean exists = flightRepository.existsByAirlineIdAndFlightNumber(1L, "AA123");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when checking non-existent flight")
    void shouldReturnFalseWhenNotExists() {
        // When
        boolean exists = flightRepository.existsByAirlineIdAndFlightNumber(1L, "XX999");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find flights by airlineId paginated")
    void shouldFindByAirlineIdPaginated() {
        // Given
        flightRepository.save(flight);

        Flight flight2 = Flight.builder()
                .flightNumber("IB456")
                .airlineId(1L)
                .departureAirportId(2L)
                .arrivalAirportId(1L)
                .build();
        flightRepository.save(flight2);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Flight> result = flightRepository.findByAirlineId(1L, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Flight::getFlightNumber)
                .containsExactlyInAnyOrder("AA123", "IB456");
    }

}