package com.airline.controller;

import com.airline.payload.request.FareRuleRequest;
import com.airline.payload.response.FareRuleResponse;
import com.airline.service.FareRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fare-rules")
public class FareRuleController {

    private final FareRuleService fareRuleService;

    @PostMapping
    public ResponseEntity<FareRuleResponse> createRule(
            @Valid @RequestBody FareRuleRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fareRuleService.createRule(request, requesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FareRuleResponse> getRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(fareRuleService.getRuleById(id));
    }

    @GetMapping("/fare/{fareId}")
    public ResponseEntity<List<FareRuleResponse>> getRulesByFare(@PathVariable Long fareId) {
        return ResponseEntity.ok(fareRuleService.getRulesByFareId(fareId));
    }

    @GetMapping
    public ResponseEntity<Page<FareRuleResponse>> getAllRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(fareRuleService.getAllRules(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FareRuleResponse> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody FareRuleRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(fareRuleService.updateRule(id, request, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        fareRuleService.deleteRule(id, requesterId);
        return ResponseEntity.noContent().build();
    }

}