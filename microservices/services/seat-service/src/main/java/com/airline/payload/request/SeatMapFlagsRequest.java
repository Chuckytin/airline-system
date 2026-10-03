package com.airline.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapFlagsRequest {

    private Boolean hasExtraLegroom;
    private Boolean isExitRow;

}