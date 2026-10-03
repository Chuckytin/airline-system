package com.airline.payload.request;

import com.airline.enums.SeatStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusUpdateRequest {

    @NotNull(message = "Status is mandatory")
    private SeatStatus status;

}