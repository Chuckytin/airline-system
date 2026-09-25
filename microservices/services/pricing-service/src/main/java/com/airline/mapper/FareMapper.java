package com.airline.mapper;

import com.airline.model.Fare;
import com.airline.payload.request.FareRequest;
import com.airline.payload.response.FareResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FareMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = "active")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Fare toEntity(FareRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = {"flightId", "active"})
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Fare entity, FareRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = "totalPrice")
    @Mapping(target = "totalPrice", ignore = true)
    FareResponse toResponse(Fare fare);

}