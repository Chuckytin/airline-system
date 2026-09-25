package com.airline.service.impl;

import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.mapper.FareRuleMapper;
import com.airline.model.Fare;
import com.airline.model.FareRule;
import com.airline.payload.request.FareRuleRequest;
import com.airline.payload.response.FareRuleResponse;
import com.airline.repository.FareRepository;
import com.airline.repository.FareRuleRepository;
import com.airline.service.FareRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FareRuleServiceImpl implements FareRuleService {

    private final FareRuleRepository fareRuleRepository;
    private final FareRepository fareRepository;
    private final FareRuleMapper fareRuleMapper;

    @Override
    @Transactional
    public FareRuleResponse createRule(FareRuleRequest request, Long requesterId) {
        Fare fare = fareRepository.findById(request.getFareId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FARE_NOT_FOUND, "Fare", request.getFareId()
                ));

        if (fareRuleRepository.existsByFareIdAndRuleType(request.getFareId(), request.getRuleType())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.FARE_RULE_ALREADY_EXISTS,
                    "FareRule", "fareId+ruleType",
                    request.getFareId() + "+" + request.getRuleType()
            );
        }

        FareRule rule = fareRuleMapper.toEntity(request);
        rule.setFare(fare);
        rule.setAllowed(request.getAllowed() != null ? request.getAllowed() : true);

        FareRule saved = fareRuleRepository.save(rule);
        return fareRuleMapper.toResponse(saved);
    }

    @Override
    public FareRuleResponse getRuleById(Long id) {
        FareRule rule = fareRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FARE_RULE_NOT_FOUND, "FareRule", id
                ));
        return fareRuleMapper.toResponse(rule);
    }

    @Override
    public List<FareRuleResponse> getRulesByFareId(Long fareId) {
        if (!fareRepository.existsById(fareId)) {
            throw new ResourceNotFoundException(ErrorCode.FARE_NOT_FOUND, "Fare", fareId);
        }
        return fareRuleRepository.findByFareId(fareId)
                .stream()
                .map(fareRuleMapper::toResponse)
                .toList();
    }

    @Override
    public Page<FareRuleResponse> getAllRules(Pageable pageable) {
        return fareRuleRepository.findAll(pageable)
                .map(fareRuleMapper::toResponse);
    }

    @Override
    @Transactional
    public FareRuleResponse updateRule(Long id, FareRuleRequest request, Long requesterId) {
        FareRule existing = fareRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FARE_RULE_NOT_FOUND, "FareRule", id
                ));

        fareRuleMapper.updateEntity(existing, request);
        FareRule updated = fareRuleRepository.save(existing);
        return fareRuleMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRule(Long id, Long requesterId) {
        FareRule rule = fareRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FARE_RULE_NOT_FOUND, "FareRule", id
                ));
        fareRuleRepository.delete(rule);
    }

}