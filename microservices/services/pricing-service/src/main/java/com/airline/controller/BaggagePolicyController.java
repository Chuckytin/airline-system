package com.airline.controller;

import com.airline.payload.request.BaggagePolicyRequest;
import com.airline.payload.response.BaggagePolicyResponse;
import com.airline.service.BaggagePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/baggage-policies")
public class BaggagePolicyController {

    private final BaggagePolicyService baggagePolicyService;

    @PostMapping
    public ResponseEntity<BaggagePolicyResponse> createPolicy(
            @Valid @RequestBody BaggagePolicyRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(baggagePolicyService.createPolicy(request, requesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaggagePolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(baggagePolicyService.getPolicyById(id));
    }

    @GetMapping("/fare/{fareId}")
    public ResponseEntity<BaggagePolicyResponse> getPolicyByFare(@PathVariable Long fareId) {
        return ResponseEntity.ok(baggagePolicyService.getPolicyByFareId(fareId));
    }

    @GetMapping
    public ResponseEntity<Page<BaggagePolicyResponse>> getAllPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(baggagePolicyService.getAllPolicies(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaggagePolicyResponse> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody BaggagePolicyRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(baggagePolicyService.updatePolicy(id, request, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        baggagePolicyService.deletePolicy(id, requesterId);
        return ResponseEntity.noContent().build();
    }

}