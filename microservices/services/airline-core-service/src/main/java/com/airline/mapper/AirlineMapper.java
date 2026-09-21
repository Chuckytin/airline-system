package com.airline.mapper;

import com.airline.model.Airline;
import com.airline.payload.request.AirlineRequest;
import com.airline.payload.response.AirlineDropdownItem;
import com.airline.payload.response.AirlineResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AirlineMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "updatedById", ignore = true)
    Airline toEntity(AirlineRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "updatedById", ignore = true)
    void updateEntity(@MappingTarget Airline entity, AirlineRequest request);

    AirlineResponse toResponse(Airline airline);

    @BeanMapping(ignoreUnmappedSourceProperties = {
            "alias", "website", "status", "alliance", "support",
            "ownerId", "headquartersCityId", "updatedById", "createdAt", "updatedAt"
    })
    AirlineDropdownItem toDropdownItem(Airline airline);

}