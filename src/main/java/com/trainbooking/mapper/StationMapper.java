package com.trainbooking.mapper;

import com.trainbooking.dto.request.StationRequest;
import com.trainbooking.dto.response.StationResponse;
import com.trainbooking.entity.Station;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StationMapper {

    @Mapping(target = "id", ignore = true)
    Station toEntity(StationRequest request);

    StationResponse toResponse(Station station);
}
