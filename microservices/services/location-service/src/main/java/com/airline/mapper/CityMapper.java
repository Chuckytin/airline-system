package com.airline.mapper;

import com.airline.model.City;
import com.airline.payload.request.CityRequest;
import com.airline.payload.response.CityResponse;
import org.mapstruct.*;

/**
 * Mapper entre CityRequest/CityResponse y la entidad City.
 * - ERROR: detecta campos sin mapear en compilación.
 * - IGNORE: en updates, solo actualiza campos no nulos.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CityMapper {

    @Mapping(target = "id", ignore = true)
    City toEntity(CityRequest request);

    @Mapping(target = "id", ignore = true)
    City updateEntity(@MappingTarget City entity, CityRequest request);

    CityResponse toResponse(City city);

}