package com.airline.mapper;

import com.airline.model.BaggagePolicy;
import com.airline.payload.request.BaggagePolicyRequest;
import com.airline.payload.response.BaggagePolicyResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BaggagePolicyMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = "fareId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fare", ignore = true)
    @Mapping(target = "cabinClass", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BaggagePolicy toEntity(BaggagePolicyRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = "fareId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fare", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget BaggagePolicy entity, BaggagePolicyRequest request);

    @Mapping(target = "fareId", source = "fare.id")
    BaggagePolicyResponse toResponse(BaggagePolicy policy);

}