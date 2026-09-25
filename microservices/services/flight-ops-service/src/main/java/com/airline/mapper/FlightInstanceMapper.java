package com.airline.mapper;

import com.airline.model.FlightInstance;
import com.airline.payload.request.FlightInstanceRequest;
import com.airline.payload.response.FlightInstanceResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FlightInstanceMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {
            "flightId", "scheduleId", "status", "active"
    })
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flight", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "status", ignore = true)
    FlightInstance toEntity(FlightInstanceRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = {
            "flightId", "scheduleId", "status", "active"
    })
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flight", ignore = true)
    @Mapping(target = "schedule", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(@MappingTarget FlightInstance entity, FlightInstanceRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = "formattedDuration")
    @Mapping(target = "flightId", source = "flight.id")
    @Mapping(target = "scheduleId", source = "schedule.id")
    @Mapping(target = "formattedDuration", ignore = true)
    FlightInstanceResponse toResponse(FlightInstance instance);

}