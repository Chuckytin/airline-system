package com.airline.repository;

import com.airline.enums.AircraftStatus;
import com.airline.model.Aircraft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {

    Optional<Aircraft> findByCode(String code);

    boolean existsByCode(String code);

    Page<Aircraft> findAllByAirlineId(Long airlineId, Pageable pageable);

    List<Aircraft> findAllByAirlineIdAndStatus(Long airlineId, AircraftStatus status);

}