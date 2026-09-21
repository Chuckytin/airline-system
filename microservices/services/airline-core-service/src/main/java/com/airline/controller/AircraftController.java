package com.airline.controller;

import com.airline.enums.AircraftStatus;
import com.airline.payload.request.AircraftRequest;
import com.airline.payload.response.AircraftResponse;
import com.airline.service.AircraftService;
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
@RequestMapping("/api/v1/aircraft")
public class AircraftController {

    private final AircraftService aircraftService;

    @PostMapping
    public ResponseEntity<AircraftResponse> createAircraft(
            @Valid @RequestBody AircraftRequest request,
            @RequestHeader("X-User-Id") Long ownerId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aircraftService.createAircraft(request, ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AircraftResponse> getAircraftById(@PathVariable Long id) {
        return ResponseEntity.ok(aircraftService.getAircraftById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<AircraftResponse> getAircraftByCode(@PathVariable String code) {
        return ResponseEntity.ok(aircraftService.getAircraftByCode(code));
    }

    @GetMapping
    public ResponseEntity<Page<AircraftResponse>> getAllAircraft(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(aircraftService.getAllAircraft(pageable));
    }

    @GetMapping("/airline/{airlineId}")
    public ResponseEntity<Page<AircraftResponse>> getAircraftByAirline(
            @PathVariable Long airlineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(aircraftService.getAircraftByAirline(airlineId, pageable));
    }

    @GetMapping("/airline/{airlineId}/status/{status}")
    public ResponseEntity<List<AircraftResponse>> getAircraftByAirlineAndStatus(
            @PathVariable Long airlineId,
            @PathVariable AircraftStatus status
    ) {
        return ResponseEntity.ok(aircraftService.getAircraftByAirlineAndStatus(airlineId, status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AircraftResponse> updateAircraft(
            @PathVariable Long id,
            @Valid @RequestBody AircraftRequest request,
            @RequestHeader("X-User-Id") Long ownerId
    ) {
        return ResponseEntity.ok(aircraftService.updateAircraft(id, request, ownerId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AircraftResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam AircraftStatus status,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(aircraftService.changeStatus(id, status, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraft(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        aircraftService.deleteAircraft(id, requesterId);
        return ResponseEntity.noContent().build();
    }

}