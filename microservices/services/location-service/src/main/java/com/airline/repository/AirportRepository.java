package com.airline.repository;

import com.airline.model.Airport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {

    boolean existsByIataCode(String iataCode);

    Page<Airport> findByCityId(Long cityId, Pageable pageable);

}
