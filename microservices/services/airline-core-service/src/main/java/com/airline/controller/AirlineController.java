package com.airline.controller;

import com.airline.enums.AirlineStatus;
import com.airline.payload.request.AirlineRequest;
import com.airline.payload.response.AirlineDropdownItem;
import com.airline.payload.response.AirlineResponse;
import com.airline.service.AirlineService;
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
@RequestMapping("/api/v1/airlines")
public class AirlineController {

    private final AirlineService airlineService;

    /**
     * TODO
     * TEMPORAL: el ownerId se recibe del header {@code X-User-Id}.
     * Cuando se implemente el API Gateway, se obtendrá del JWT.
     */
    @PostMapping
    public ResponseEntity<AirlineResponse> createAirline(
            @Valid @RequestBody AirlineRequest request,
            @RequestHeader("X-User-Id") Long ownerId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(airlineService.createAirline(request, ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirlineResponse> getAirlineById(@PathVariable Long id) {
        return ResponseEntity.ok(airlineService.getAirlineById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<AirlineResponse> getAirlineByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(airlineService.getAirlineByOwnerId(ownerId));
    }

    @GetMapping
    public ResponseEntity<Page<AirlineResponse>> getAllAirlines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(airlineService.getAllAirlines(pageable));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<List<AirlineDropdownItem>> getAirlinesForDropdown() {
        return ResponseEntity.ok(airlineService.getAirlinesForDropdown());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirlineResponse> updateAirline(
            @PathVariable Long id,
            @Valid @RequestBody AirlineRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(airlineService.updateAirline(id, request, requesterId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AirlineResponse> changeStatusByAdmin(
            @PathVariable Long id,
            @RequestParam AirlineStatus status,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(airlineService.changeStatusByAdmin(id, status, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAirline(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long ownerId
    ) {
        airlineService.deleteAirline(id, ownerId);
        return ResponseEntity.noContent().build();
    }

}