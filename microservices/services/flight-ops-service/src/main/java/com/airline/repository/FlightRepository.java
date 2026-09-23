package com.airline.repository;

import com.airline.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    Page<Flight> findByAirlineId(Long airlineId, Pageable pageable);

    boolean existsByAirlineIdAndFlightNumber(Long airlineId, String flightNumber);

}