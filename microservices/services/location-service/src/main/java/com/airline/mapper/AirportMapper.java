package com.airline.mapper;

import com.airline.model.Airport;
import com.airline.payload.request.AirportRequest;
import com.airline.payload.response.AirportResponse;
import org.mapstruct.*;

/**
 * Mapper entre AirportRequest/AirportResponse y la entidad Airport.
 * - ERROR: detecta campos sin mapear en compilación.
 * - IGNORE: en updates, solo actualiza campos no nulos.
 * - uses: delega la conversión City -> CityResponse en CityMapper.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CityMapper.class}
)
public interface AirportMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = "cityId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "city", ignore = true)
    Airport toEntity(AirportRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = "cityId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "city", ignore = true)
    Airport updateEntity(@MappingTarget Airport entity, AirportRequest request);

    @Mapping(target = "detailedName", ignore = true)
    AirportResponse toResponse(Airport airport);

}