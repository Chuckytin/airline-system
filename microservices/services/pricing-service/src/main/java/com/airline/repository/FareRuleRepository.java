package com.airline.repository;

import com.airline.enums.FareRuleType;
import com.airline.model.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {

    List<FareRule> findByFareId(Long fareId);

    Optional<FareRule> findByFareIdAndRuleType(Long fareId, FareRuleType ruleType);

    List<FareRule> findByFareIdAndAllowedTrue(Long fareId);

    boolean existsByFareIdAndRuleType(Long fareId, FareRuleType ruleType);

}