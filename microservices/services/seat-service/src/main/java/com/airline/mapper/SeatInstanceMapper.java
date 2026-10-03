package com.airline.mapper;

import com.airline.model.SeatInstance;
import com.airline.payload.response.SeatInstanceResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SeatInstanceMapper {

    @Mapping(target = "cabinId", source = "cabin.id")
    @Mapping(target = "cabinClass", source = "cabin.cabinClass")
    SeatInstanceResponse toResponse(SeatInstance seatInstance);

}