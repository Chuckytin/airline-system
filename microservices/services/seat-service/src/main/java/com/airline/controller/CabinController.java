package com.airline.controller;

import com.airline.payload.request.CabinRequest;
import com.airline.payload.response.CabinAvailabilityResponse;
import com.airline.payload.response.CabinResponse;
import com.airline.service.CabinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cabins")
@RequiredArgsConstructor
public class CabinController {

    private final CabinService cabinService;

    @PostMapping
    public ResponseEntity<CabinResponse> create(
            @Valid @RequestBody CabinRequest request,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cabinService.createCabin(request, requesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CabinResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cabinService.getCabinById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<CabinResponse> getWithDetails(@PathVariable Long id) {
        return ResponseEntity.ok(cabinService.getCabinWithDetails(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<CabinAvailabilityResponse> getAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(cabinService.getAvailability(id));
    }

    @GetMapping
    public ResponseEntity<List<CabinResponse>> getByFlightInstance(
            @RequestParam Long flightInstanceId) {
        return ResponseEntity.ok(cabinService.getCabinsByFlightInstance(flightInstanceId));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<CabinResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(cabinService.getAllCabins(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CabinResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CabinRequest request,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(cabinService.updateCabin(id, request, requesterId));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<CabinResponse> changeActive(
            @PathVariable Long id,
            @RequestParam Boolean active,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(cabinService.changeActiveStatus(id, active, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId) {
        cabinService.deleteCabin(id, requesterId);
        return ResponseEntity.noContent().build();
    }

}