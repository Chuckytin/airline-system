package com.airline.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAssignmentRequest {

    @NotNull(message = "Booking ID is mandatory")
    private Long bookingId;

    @NotNull(message = "Passenger ID is mandatory")
    private Long passengerId;

}