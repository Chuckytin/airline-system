package com.airline.controller;

import com.airline.payload.request.FareRequest;
import com.airline.payload.response.FareResponse;
import com.airline.service.FareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fares")
public class FareController {

    private final FareService fareService;

    @PostMapping
    public ResponseEntity<FareResponse> createFare(
            @Valid @RequestBody FareRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fareService.createFare(request, requesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FareResponse> getFareById(@PathVariable Long id) {
        return ResponseEntity.ok(fareService.getFareById(id));
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<FareResponse>> getFaresByFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(fareService.getFaresByFlightId(flightId));
    }

    @GetMapping
    public ResponseEntity<Page<FareResponse>> getAllFares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(fareService.getAllFares(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FareResponse> updateFare(
            @PathVariable Long id,
            @Valid @RequestBody FareRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(fareService.updateFare(id, request, requesterId));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<FareResponse> changeActiveStatus(
            @PathVariable Long id,
            @RequestParam Boolean active,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(fareService.changeActiveStatus(id, active, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFare(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        fareService.deleteFare(id, requesterId);
        return ResponseEntity.noContent().build();
    }

}