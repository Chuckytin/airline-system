package com.airline.service.impl;

import com.airline.config.PricingProperties;
import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.mapper.BaggagePolicyMapper;
import com.airline.model.BaggagePolicy;
import com.airline.model.Fare;
import com.airline.payload.request.BaggagePolicyRequest;
import com.airline.payload.response.BaggagePolicyResponse;
import com.airline.repository.BaggagePolicyRepository;
import com.airline.repository.FareRepository;
import com.airline.service.BaggagePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BaggagePolicyServiceImpl implements BaggagePolicyService {

    private final BaggagePolicyRepository baggagePolicyRepository;
    private final FareRepository fareRepository;
    private final BaggagePolicyMapper baggagePolicyMapper;
    private final PricingProperties pricingProperties;

    @Override
    @Transactional
    public BaggagePolicyResponse createPolicy(BaggagePolicyRequest request, Long requesterId) {
        Fare fare = fareRepository.findById(request.getFareId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FARE_NOT_FOUND, "Fare", request.getFareId()
                ));

        if (baggagePolicyRepository.existsByFareId(request.getFareId())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.BAGGAGE_POLICY_ALREADY_EXISTS,
                    "BaggagePolicy", "fareId", request.getFareId()
            );
        }

        BaggagePolicy policy = baggagePolicyMapper.toEntity(request);

        policy.setCabinClass(fare.getCabinClass());

        PricingProperties.BaggageDefaults defaults = pricingProperties.getBaggage();

        if (policy.getCarryOnPieces() == null) {
            policy.setCarryOnPieces(defaults.getCarryOnPieces());
        }
        if (policy.getCarryOnWeightKg() == null) {
            policy.setCarryOnWeightKg(defaults.getCarryOnWeightKg());
        }
        if (policy.getCheckedPieces() == null) {
            policy.setCheckedPieces(defaults.getCheckedPieces());
        }
        if (policy.getCheckedWeightKg() == null) {
            policy.setCheckedWeightKg(defaults.getCheckedWeightKg());
        }
        if (policy.getExtraBagPrice() == null) {
            policy.setExtraBagPrice(defaults.getExtraBagPrice());
        }
        if (policy.getCurrency() == null) {
            policy.setCurrency(pricingProperties.getDefaultCurrency());
        }

        policy.setFare(fare);

        BaggagePolicy saved = baggagePolicyRepository.save(policy);
        return baggagePolicyMapper.toResponse(saved);
    }

    @Override
    public BaggagePolicyResponse getPolicyById(Long id) {
        BaggagePolicy policy = baggagePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.BAGGAGE_POLICY_NOT_FOUND, "BaggagePolicy", id
                ));
        return baggagePolicyMapper.toResponse(policy);
    }

    @Override
    public BaggagePolicyResponse getPolicyByFareId(Long fareId) {
        BaggagePolicy policy = baggagePolicyRepository.findByFareId(fareId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.BAGGAGE_POLICY_NOT_FOUND,
                        "BaggagePolicy not found for fare: " + fareId
                ));
        return baggagePolicyMapper.toResponse(policy);
    }

    @Override
    public Page<BaggagePolicyResponse> getAllPolicies(Pageable pageable) {
        return baggagePolicyRepository.findAll(pageable)
                .map(baggagePolicyMapper::toResponse);
    }

    @Override
    @Transactional
    public BaggagePolicyResponse updatePolicy(Long id, BaggagePolicyRequest request, Long requesterId) {
        BaggagePolicy existing = baggagePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.BAGGAGE_POLICY_NOT_FOUND, "BaggagePolicy", id
                ));

        baggagePolicyMapper.updateEntity(existing, request);
        BaggagePolicy updated = baggagePolicyRepository.save(existing);
        return baggagePolicyMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deletePolicy(Long id, Long requesterId) {
        BaggagePolicy policy = baggagePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.BAGGAGE_POLICY_NOT_FOUND, "BaggagePolicy", id
                ));
        baggagePolicyRepository.delete(policy);
    }

}