package com.airline.mapper;

import com.airline.model.FlightSchedule;
import com.airline.payload.request.FlightScheduleRequest;
import com.airline.payload.response.FlightScheduleResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FlightScheduleMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"flightId", "active"})
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flight", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    FlightSchedule toEntity(FlightScheduleRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = {"flightId", "active"})
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flight", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget FlightSchedule entity, FlightScheduleRequest request);

    @Mapping(target = "flightId", source = "flight.id")
    FlightScheduleResponse toResponse(FlightSchedule schedule);

}