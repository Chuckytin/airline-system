package com.airline.controller;

import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
import com.airline.payload.request.SeatAssignmentRequest;
import com.airline.payload.request.SeatStatusUpdateRequest;
import com.airline.payload.response.SeatInstanceResponse;
import com.airline.service.SeatInstanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seat-instances")
@RequiredArgsConstructor
public class SeatInstanceController {

    private final SeatInstanceService seatInstanceService;

    @GetMapping("/{id}")
    public ResponseEntity<SeatInstanceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(seatInstanceService.getSeatInstanceById(id));
    }

    @GetMapping
    public ResponseEntity<List<SeatInstanceResponse>> getByCabin(
            @RequestParam Long cabinId,
            @RequestParam(required = false) SeatStatus status,
            @RequestParam(required = false) SeatType type) {

        if (status != null) {
            return ResponseEntity.ok(seatInstanceService.getSeatsByCabinAndStatus(cabinId, status));
        }
        if (type != null) {
            return ResponseEntity.ok(seatInstanceService.getSeatsByCabinAndType(cabinId, type));
        }
        return ResponseEntity.ok(seatInstanceService.getSeatsByCabin(cabinId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SeatInstanceResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody SeatStatusUpdateRequest request) {
        return ResponseEntity.ok(seatInstanceService.changeStatus(id, request));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<SeatInstanceResponse> assign(
            @PathVariable Long id,
            @Valid @RequestBody SeatAssignmentRequest request) {
        return ResponseEntity.ok(seatInstanceService.assign(id, request));
    }

    @PatchMapping("/{id}/release")
    public ResponseEntity<SeatInstanceResponse> release(@PathVariable Long id) {
        return ResponseEntity.ok(seatInstanceService.release(id));
    }

}