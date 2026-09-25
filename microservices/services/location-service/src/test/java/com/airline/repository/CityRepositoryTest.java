package com.airline.repository;

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
@DisplayName("CityRepository Tests")
class CityRepositoryTest {

    @Autowired
    private CityRepository cityRepository;

    private City city;

    @BeforeEach
    void setUp() {
        city = City.builder()
                .name("Madrid")
                .cityCode("MAD")
                .countryCode("ES")
                .countryName("Spain")
                .regionCode("MD")
                .timeZoneOffset("+02:00")
                .build();
    }

    @Test
    @DisplayName("Should save and find city by ID")
    void shouldSaveAndFindById() {
        City saved = cityRepository.save(city);

        assertThat(saved.getId()).isNotNull();
        Optional<City> found = cityRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCityCode()).isEqualTo("MAD");
    }

    @Test
    @DisplayName("Should check exists by cityCode")
    void shouldCheckExistsByCityCode() {
        cityRepository.save(city);

        assertThat(cityRepository.existsByCityCode("MAD")).isTrue();
        assertThat(cityRepository.existsByCityCode("XXX")).isFalse();
    }

    @Test
    @DisplayName("Should find cities by countryCode ignoring case, paginated")
    void shouldFindByCountryCodeIgnoreCasePaginated() {
        cityRepository.save(city);

        City city2 = City.builder()
                .name("Barcelona")
                .cityCode("BCN")
                .countryCode("ES")
                .countryName("Spain")
                .build();
        cityRepository.save(city2);

        City city3 = City.builder()
                .name("Paris")
                .cityCode("PAR")
                .countryCode("FR")
                .countryName("France")
                .build();
        cityRepository.save(city3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<City> result = cityRepository.findByCountryCodeIgnoreCase("es", pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(City::getCityCode)
                .containsExactlyInAnyOrder("MAD", "BCN");
    }

    @Test
    @DisplayName("Should search cities by keyword in name, cityCode, countryCode, countryName or regionCode")
    void shouldSearchByKeyword() {
        cityRepository.save(city);

        City city2 = City.builder()
                .name("Barcelona")
                .cityCode("BCN")
                .countryCode("ES")
                .countryName("Spain")
                .regionCode("CT")
                .build();
        cityRepository.save(city2);

        City city3 = City.builder()
                .name("Paris")
                .cityCode("PAR")
                .countryCode("FR")
                .countryName("France")
                .build();
        cityRepository.save(city3);

        Pageable pageable = PageRequest.of(0, 10);

        // Por nombre
        Page<City> byName = cityRepository.searchByKeyword("madrid", pageable);
        assertThat(byName.getTotalElements()).isEqualTo(1);
        assertThat(byName.getContent().getFirst().getCityCode()).isEqualTo("MAD");

        // Por cityCode
        Page<City> byCityCode = cityRepository.searchByKeyword("bcn", pageable);
        assertThat(byCityCode.getTotalElements()).isEqualTo(1);

        // Por countryCode
        Page<City> byCountryCode = cityRepository.searchByKeyword("fr", pageable);
        assertThat(byCountryCode.getTotalElements()).isEqualTo(1);

        // Por countryName
        Page<City> byCountryName = cityRepository.searchByKeyword("spain", pageable);
        assertThat(byCountryName.getTotalElements()).isEqualTo(2);

        // Por regionCode
        Page<City> byRegionCode = cityRepository.searchByKeyword("ct", pageable);
        assertThat(byRegionCode.getTotalElements()).isEqualTo(1);
    }
}