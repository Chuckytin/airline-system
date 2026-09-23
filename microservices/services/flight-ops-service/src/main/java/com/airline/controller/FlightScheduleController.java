package com.airline.controller;

import com.airline.payload.request.FlightScheduleRequest;
import com.airline.payload.response.FlightScheduleResponse;
import com.airline.service.FlightScheduleService;
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
@RequestMapping("/api/v1/flight-schedules")
public class FlightScheduleController {

    private final FlightScheduleService flightScheduleService;

    @PostMapping
    public ResponseEntity<FlightScheduleResponse> createSchedule(
            @Valid @RequestBody FlightScheduleRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flightScheduleService.createSchedule(request, requesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(flightScheduleService.getScheduleById(id));
    }

    @GetMapping
    public ResponseEntity<Page<FlightScheduleResponse>> getAllSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(flightScheduleService.getAllSchedules(pageable));
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<FlightScheduleResponse>> getSchedulesByFlight(
            @PathVariable Long flightId
    ) {
        return ResponseEntity.ok(flightScheduleService.getSchedulesByFlightId(flightId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody FlightScheduleRequest request,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(flightScheduleService.updateSchedule(id, request, requesterId));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<FlightScheduleResponse> changeActiveStatus(
            @PathVariable Long id,
            @RequestParam Boolean active,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        return ResponseEntity.ok(flightScheduleService.changeActiveStatus(id, active, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId
    ) {
        flightScheduleService.deleteSchedule(id, requesterId);
        return ResponseEntity.noContent().build();
    }

}