package com.airline.repository;

import com.airline.model.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AirlineRepository extends JpaRepository<Airline, Long> {

    boolean existsByIataCode(String iataCode);

    boolean existsByIcaoCode(String icaoCode);

    boolean existsByOwnerId(Long ownerId);

    Optional<Airline> findByOwnerId(Long ownerId);

}