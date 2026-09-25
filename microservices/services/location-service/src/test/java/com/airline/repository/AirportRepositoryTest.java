package com.airline.repository;

import com.airline.embeddable.Address;
import com.airline.embeddable.GeoCode;
import com.airline.model.Airport;
import com.airline.model.City;
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
@DisplayName("AirportRepository Tests")
class AirportRepositoryTest {

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private CityRepository cityRepository;

    private City city;
    private Airport airport;

    @BeforeEach
    void setUp() {
        city = City.builder()
                .name("Madrid")
                .cityCode("MAD")
                .countryCode("ES")
                .countryName("Spain")
                .build();
        city = cityRepository.save(city);

        airport = Airport.builder()
                .iataCode("MAD")
                .name("Adolfo Suárez Madrid-Barajas")
                .address(Address.builder()
                        .street("Avenida de la Hispanidad")
                        .postalCode("28042")
                        .build())
                .geoCode(GeoCode.builder()
                        .latitude(40.471926)
                        .longitude(-3.56264)
                        .build())
                .timeZone("Europe/Madrid")
                .city(city)
                .build();
    }

    @Test
    @DisplayName("Should save and find airport by ID")
    void shouldSaveAndFindById() {
        Airport saved = airportRepository.save(airport);

        assertThat(saved.getId()).isNotNull();
        Optional<Airport> found = airportRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getIataCode()).isEqualTo("MAD");
    }

    @Test
    @DisplayName("Should check exists by IATA code")
    void shouldCheckExistsByIataCode() {
        airportRepository.save(airport);

        assertThat(airportRepository.existsByIataCode("MAD")).isTrue();
        assertThat(airportRepository.existsByIataCode("XXX")).isFalse();
    }

    @Test
    @DisplayName("Should find airports by cityId paginated")
    void shouldFindByCityIdPaginated() {
        airportRepository.save(airport);

        Airport airport2 = Airport.builder()
                .iataCode("BCN")
                .name("Josep Tarradellas Barcelona-El Prat")
                .timeZone("Europe/Madrid")
                .city(city)
                .build();
        airportRepository.save(airport2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Airport> result = airportRepository.findByCityId(city.getId(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Airport::getIataCode)
                .containsExactlyInAnyOrder("MAD", "BCN");
    }
}