package com.airline.mapper;

import com.airline.model.FareRule;
import com.airline.payload.request.FareRuleRequest;
import com.airline.payload.response.FareRuleResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FareRuleMapper {

    @BeanMapping(ignoreUnmappedSourceProperties = {"fareId", "allowed"})
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fare", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "allowed", ignore = true)
    FareRule toEntity(FareRuleRequest request);

    @BeanMapping(ignoreUnmappedSourceProperties = {"fareId", "allowed"})
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fare", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget FareRule entity, FareRuleRequest request);

    @Mapping(target = "fareId", source = "fare.id")
    FareRuleResponse toResponse(FareRule rule);

}