package com.airline.repository;

import com.airline.model.FlightInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface FlightInstanceRepository extends JpaRepository<FlightInstance, Long> {

    /**
     * Busca una instancia por ID cargando el flight y el schedule (JOIN FETCH).
     */
    @Query("""
            SELECT fi FROM FlightInstance fi
            JOIN FETCH fi.flight
            LEFT JOIN FETCH fi.schedule
            WHERE fi.id = :id
            """)
    Optional<FlightInstance> findByIdWithDetails(@Param("id") Long id);

    /**
     * Busca instancias por flightId (paginado).
     */
    Page<FlightInstance> findByFlightId(Long flightId, Pageable pageable);

    /**
     * Busca instancias por fecha (paginado).
     */
    Page<FlightInstance> findByFlightDate(LocalDate flightDate, Pageable pageable);

}