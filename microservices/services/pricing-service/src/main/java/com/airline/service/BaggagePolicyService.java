package com.airline.service;

import com.airline.payload.request.BaggagePolicyRequest;
import com.airline.payload.response.BaggagePolicyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BaggagePolicyService {

    BaggagePolicyResponse createPolicy(BaggagePolicyRequest request, Long requesterId);

    BaggagePolicyResponse getPolicyById(Long id);

    BaggagePolicyResponse getPolicyByFareId(Long fareId);

    Page<BaggagePolicyResponse> getAllPolicies(Pageable pageable);

    BaggagePolicyResponse updatePolicy(Long id, BaggagePolicyRequest request, Long requesterId);

    void deletePolicy(Long id, Long requesterId);

}