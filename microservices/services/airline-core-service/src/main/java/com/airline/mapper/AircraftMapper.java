package com.airline.mapper;

import com.airline.model.Aircraft;
import com.airline.payload.request.AircraftRequest;
import com.airline.payload.response.AircraftResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AircraftMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {
            "status", "available", "airlineId"
    })
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "airline", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "economySeats", defaultValue = "0")
    @Mapping(target = "premiumEconomySeats", defaultValue = "0")
    @Mapping(target = "businessSeats", defaultValue = "0")
    @Mapping(target = "firstClassSeats", defaultValue = "0")
    Aircraft toEntity(AircraftRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = {
            "status", "available", "airlineId"
    })
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "airline", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "available", ignore = true)
    void updateEntity(@MappingTarget Aircraft entity, AircraftRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = {
            "totalSeats", "operational"
    })
    @Mapping(target = "airlineId", source = "airline.id")
    @Mapping(target = "airlineName", source = "airline.name")
    @Mapping(target = "airlineIataCode", source = "airline.iataCode")
    @Mapping(target = "totalSeats", ignore = true)
    @Mapping(target = "operational", ignore = true)
    @Mapping(target = "requiresMaintenance", ignore = true)
    AircraftResponse toResponse(Aircraft aircraft);

}