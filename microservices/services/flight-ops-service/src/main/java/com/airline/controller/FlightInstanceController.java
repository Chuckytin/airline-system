package com.airline.controller;

import com.airline.enums.FlightStatus;
import com.airline.payload.request.FlightInstanceRequest;
import com.airline.payload.response.FlightInstanceResponse;
import com.airline.service.FlightInstanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/flight-instances")
public class FlightInstanceController {

    private final FlightInstanceService flightInstanceService;

    @PostMapping
    public ResponseEntity<FlightInstanceResponse> createInstance(
            @Valid @RequestBody FlightInstanceRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flightInstanceService.createInstance(request, requesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightInstanceResponse> getInstanceById(@PathVariable Long id) {
        return ResponseEntity.ok(flightInstanceService.getInstanceById(id));
    }

    @GetMapping
    public ResponseEntity<Page<FlightInstanceResponse>> getAllInstances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "departureDateTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(flightInstanceService.getAllInstances(pageable));
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<Page<FlightInstanceResponse>> getInstancesByFlight(
            @PathVariable Long flightId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("departureDateTime").ascending());
        return ResponseEntity.ok(flightInstanceService.getInstancesByFlightId(flightId, pageable));
    }

    @GetMapping("/date/{flightDate}")
    public ResponseEntity<Page<FlightInstanceResponse>> getInstancesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate flightDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("departureDateTime").ascending());
        return ResponseEntity.ok(flightInstanceService.getInstancesByDate(flightDate, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightInstanceResponse> updateInstance(
            @PathVariable Long id,
            @Valid @RequestBody FlightInstanceRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(flightInstanceService.updateInstance(id, request, requesterId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FlightInstanceResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam FlightStatus status,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(flightInstanceService.changeStatus(id, status, requesterId));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<FlightInstanceResponse> changeActiveStatus(
            @PathVariable Long id,
            @RequestParam Boolean active,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(flightInstanceService.changeActiveStatus(id, active, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstance(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        flightInstanceService.deleteInstance(id, requesterId);
        return ResponseEntity.noContent().build();
    }

}