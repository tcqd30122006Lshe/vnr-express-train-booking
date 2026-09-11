package com.trainbooking.mapper;

import com.trainbooking.dto.request.StationRequest;
import com.trainbooking.dto.response.StationResponse;
import com.trainbooking.entity.Station;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-21T20:53:20+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class StationMapperImpl implements StationMapper {

    @Override
    public Station toEntity(StationRequest request) {
        if ( request == null ) {
            return null;
        }

        Station.StationBuilder station = Station.builder();

        station.code( request.getCode() );
        station.name( request.getName() );
        station.province( request.getProvince() );

        return station.build();
    }

    @Override
    public StationResponse toResponse(Station station) {
        if ( station == null ) {
            return null;
        }

        StationResponse.StationResponseBuilder stationResponse = StationResponse.builder();

        stationResponse.code( station.getCode() );
        stationResponse.id( station.getId() );
        stationResponse.name( station.getName() );
        stationResponse.province( station.getProvince() );

        return stationResponse.build();
    }
}
