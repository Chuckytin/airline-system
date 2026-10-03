package com.airline.controller;

import com.airline.payload.request.SeatMapFlagsRequest;
import com.airline.payload.response.SeatMapResponse;
import com.airline.service.SeatMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seat-maps")
@RequiredArgsConstructor
public class SeatMapController {

    private final SeatMapService seatMapService;

    @GetMapping("/{id}")
    public ResponseEntity<SeatMapResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(seatMapService.getSeatMapById(id));
    }

    @GetMapping
    public ResponseEntity<List<SeatMapResponse>> getByCabin(@RequestParam Long cabinId) {
        return ResponseEntity.ok(seatMapService.getSeatMapsByCabin(cabinId));
    }

    @PatchMapping("/{id}/flags")
    public ResponseEntity<SeatMapResponse> updateFlags(
            @PathVariable Long id,
            @RequestBody SeatMapFlagsRequest request) {
        return ResponseEntity.ok(seatMapService.updateFlags(id, request));
    }

}