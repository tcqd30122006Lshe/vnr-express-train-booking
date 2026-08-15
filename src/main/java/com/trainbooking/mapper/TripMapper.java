package com.trainbooking.mapper;

import com.trainbooking.dto.response.TripResponse;
import com.trainbooking.dto.response.TripStopResponse;
import com.trainbooking.entity.Trip;
import com.trainbooking.entity.TripStop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripMapper {
    @Mapping(source = "train.code",target="trainCode")
    @Mapping(source = "route.name",target = "routeName")
    TripResponse toResponse(Trip trip);

    @Mapping(source = "station.id",target = "stationId")
    @Mapping(source = "station.code",target = "stationCode")
    @Mapping(source = "station.name",target = "stationName")
    TripStopResponse toTripStopResponse(TripStop tripStop);

}
