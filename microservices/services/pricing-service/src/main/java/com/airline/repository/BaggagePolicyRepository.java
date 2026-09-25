package com.airline.repository;

import com.airline.model.BaggagePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaggagePolicyRepository extends JpaRepository<BaggagePolicy, Long> {

    Optional<BaggagePolicy> findByFareId(Long fareId);

    boolean existsByFareId(Long fareId);

}