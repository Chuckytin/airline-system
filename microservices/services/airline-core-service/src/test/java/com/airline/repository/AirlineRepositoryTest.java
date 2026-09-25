package com.airline.repository;

import com.airline.config.JpaAuditingConfig;
import com.airline.enums.AirlineStatus;
import com.airline.model.Airline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("AirlineRepository Tests")
class AirlineRepositoryTest {

    @Autowired
    private AirlineRepository airlineRepository;

    private Airline airline;

    @BeforeEach
    void setUp() {
        airline = Airline.builder()
                .iataCode("AA")
                .icaoCode("AAL")
                .name("American Airlines")
                .alias("American")
                .logoUrl("https://example.com/aa.png")
                .website("https://www.aa.com")
                .status(AirlineStatus.ACTIVE)
                .alliance("Oneworld")
                .ownerId(1L)
                .headquartersCityId(1L)
                .build();
    }

    @Test
    @DisplayName("Should save and find airline by ID")
    void shouldSaveAndFindById() {
        Airline saved = airlineRepository.save(airline);

        assertThat(saved.getId()).isNotNull();
        Optional<Airline> found = airlineRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getIataCode()).isEqualTo("AA");
    }

    @Test
    @DisplayName("Should check exists by IATA code")
    void shouldCheckExistsByIataCode() {
        airlineRepository.save(airline);

        assertThat(airlineRepository.existsByIataCode("AA")).isTrue();
        assertThat(airlineRepository.existsByIataCode("XX")).isFalse();
    }

    @Test
    @DisplayName("Should check exists by ICAO code")
    void shouldCheckExistsByIcaoCode() {
        airlineRepository.save(airline);

        assertThat(airlineRepository.existsByIcaoCode("AAL")).isTrue();
        assertThat(airlineRepository.existsByIcaoCode("XXX")).isFalse();
    }

    @Test
    @DisplayName("Should check exists by ownerId")
    void shouldCheckExistsByOwnerId() {
        airlineRepository.save(airline);

        assertThat(airlineRepository.existsByOwnerId(1L)).isTrue();
        assertThat(airlineRepository.existsByOwnerId(999L)).isFalse();
    }

    @Test
    @DisplayName("Should find airline by ownerId")
    void shouldFindByOwnerId() {
        airlineRepository.save(airline);

        Optional<Airline> found = airlineRepository.findByOwnerId(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getIataCode()).isEqualTo("AA");
    }

    @Test
    @DisplayName("Should auto-populate audit fields")
    void shouldAutoPopulateAuditFields() {
        Airline saved = airlineRepository.save(airline);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

}