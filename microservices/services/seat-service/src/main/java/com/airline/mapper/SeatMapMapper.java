package com.airline.mapper;

import com.airline.model.SeatMap;
import com.airline.payload.response.SeatMapResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SeatMapMapper {

    @Mapping(target = "cabinId", source = "cabin.id")
    SeatMapResponse toResponse(SeatMap seatMap);

}