package com.airline.service;

import com.airline.payload.request.FareRuleRequest;
import com.airline.payload.response.FareRuleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FareRuleService {

    FareRuleResponse createRule(FareRuleRequest request, Long requesterId);

    FareRuleResponse getRuleById(Long id);

    List<FareRuleResponse> getRulesByFareId(Long fareId);

    Page<FareRuleResponse> getAllRules(Pageable pageable);

    FareRuleResponse updateRule(Long id, FareRuleRequest request, Long requesterId);

    void deleteRule(Long id, Long requesterId);

}