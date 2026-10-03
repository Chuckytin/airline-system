package com.airline.mapper;

import com.airline.model.Cabin;
import com.airline.payload.request.CabinRequest;
import com.airline.payload.response.CabinResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {SeatMapMapper.class, SeatInstanceMapper.class}
)
public interface CabinMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {})
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalSeats", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "seatMaps", ignore = true)
    @Mapping(target = "seatInstances", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cabin toEntity(CabinRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = {"flightInstanceId", "cabinClass"})
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flightInstanceId", ignore = true)
    @Mapping(target = "cabinClass", ignore = true)
    @Mapping(target = "totalSeats", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "seatMaps", ignore = true)
    @Mapping(target = "seatInstances", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Cabin entity, CabinRequest request);

    /**
     * Mapeo básico: SIN colecciones de hijos.
     * Se usa en getCabinById y en la paginación.
     */
    @BeanMapping(ignoreUnmappedSourceProperties = {"seatMaps", "seatInstances"})
    @Mapping(target = "seatMaps", ignore = true)
    @Mapping(target = "seatInstances", ignore = true)
    CabinResponse toBasicResponse(Cabin cabin);

    /**
     * Mapeo con detalles: CON colecciones.
     * Se usa SOLO en getCabinWithDetails.
     */
    @BeanMapping(ignoreUnmappedSourceProperties = {"seatMaps", "seatInstances"})
    CabinResponse toResponse(Cabin cabin);

}