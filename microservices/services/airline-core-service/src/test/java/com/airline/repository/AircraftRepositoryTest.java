package com.airline.repository;

import com.airline.config.JpaAuditingConfig;
import com.airline.enums.AircraftStatus;
import com.airline.enums.AirlineStatus;
import com.airline.model.Aircraft;
import com.airline.model.Airline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("AircraftRepository Tests")
class AircraftRepositoryTest {

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private AirlineRepository airlineRepository;

    private Airline airline;
    private Aircraft aircraft;

    @BeforeEach
    void setUp() {
        airline = Airline.builder()
                .iataCode("AA")
                .icaoCode("AAL")
                .name("American Airlines")
                .status(AirlineStatus.ACTIVE)
                .ownerId(1L)
                .build();
        airline = airlineRepository.save(airline);

        aircraft = Aircraft.builder()
                .code("EC-AAA")
                .model("Boeing 737-800")
                .manufacturer("Boeing")
                .economySeats(150)
                .premiumEconomySeats(20)
                .businessSeats(16)
                .firstClassSeats(0)
                .rangeKm(5000)
                .cruisingSpeedKmh(840)
                .maxAltitudeFt(41000)
                .yearOfManufacture(2018)
                .registrationDate(LocalDate.of(2018, 6, 15))
                .nextMaintenanceDate(LocalDate.now().plusMonths(6))
                .status(AircraftStatus.ACTIVE)
                .available(true)
                .airline(airline)
                .currentAirportId(1L)
                .build();
    }

    @Test
    @DisplayName("Should save and find aircraft by ID")
    void shouldSaveAndFindById() {
        Aircraft saved = aircraftRepository.save(aircraft);

        assertThat(saved.getId()).isNotNull();
        Optional<Aircraft> found = aircraftRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("EC-AAA");
    }

    @Test
    @DisplayName("Should find aircraft by code")
    void shouldFindByCode() {
        aircraftRepository.save(aircraft);

        Optional<Aircraft> found = aircraftRepository.findByCode("EC-AAA");

        assertThat(found).isPresent();
        assertThat(found.get().getModel()).isEqualTo("Boeing 737-800");
    }

    @Test
    @DisplayName("Should check exists by code")
    void shouldCheckExistsByCode() {
        aircraftRepository.save(aircraft);

        assertThat(aircraftRepository.existsByCode("EC-AAA")).isTrue();
        assertThat(aircraftRepository.existsByCode("XX-XXX")).isFalse();
    }

    @Test
    @DisplayName("Should find all aircraft by airlineId paginated")
    void shouldFindAllByAirlineIdPaginated() {
        aircraftRepository.save(aircraft);

        Aircraft aircraft2 = Aircraft.builder()
                .code("EC-BBB")
                .model("Airbus A320")
                .manufacturer("Airbus")
                .economySeats(180)
                .status(AircraftStatus.ACTIVE)
                .available(true)
                .airline(airline)
                .build();
        aircraftRepository.save(aircraft2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Aircraft> result = aircraftRepository.findAllByAirlineId(airline.getId(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find all aircraft by airlineId and status")
    void shouldFindAllByAirlineIdAndStatus() {
        aircraftRepository.save(aircraft);

        Aircraft aircraft2 = Aircraft.builder()
                .code("EC-BBB")
                .model("Airbus A320")
                .manufacturer("Airbus")
                .economySeats(180)
                .status(AircraftStatus.MAINTENANCE)
                .available(false)
                .airline(airline)
                .build();
        aircraftRepository.save(aircraft2);

        List<Aircraft> active = aircraftRepository.findAllByAirlineIdAndStatus(
                airline.getId(), AircraftStatus.ACTIVE);

        assertThat(active).hasSize(1);
        assertThat(active.getFirst().getCode()).isEqualTo("EC-AAA");
    }

    @Test
    @DisplayName("Should auto-populate audit fields")
    void shouldAutoPopulateAuditFields() {
        Aircraft saved = aircraftRepository.save(aircraft);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

}