package com.trainbooking.mapper;

import com.trainbooking.dto.response.TripResponse;
import com.trainbooking.dto.response.TripStopResponse;
import com.trainbooking.entity.Route;
import com.trainbooking.entity.Station;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.Trip;
import com.trainbooking.entity.TripStop;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-21T20:53:20+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class TripMapperImpl implements TripMapper {

    @Override
    public TripResponse toResponse(Trip trip) {
        if ( trip == null ) {
            return null;
        }

        TripResponse.TripResponseBuilder tripResponse = TripResponse.builder();

        tripResponse.trainCode( tripTrainCode( trip ) );
        tripResponse.routeName( tripRouteName( trip ) );
        tripResponse.arrivalTime( trip.getArrivalTime() );
        tripResponse.departureTime( trip.getDepartureTime() );
        tripResponse.id( trip.getId() );
        tripResponse.status( trip.getStatus() );
        tripResponse.tripCode( trip.getTripCode() );
        tripResponse.tripStops( tripStopListToTripStopResponseList( trip.getTripStops() ) );

        return tripResponse.build();
    }

    @Override
    public TripStopResponse toTripStopResponse(TripStop tripStop) {
        if ( tripStop == null ) {
            return null;
        }

        TripStopResponse.TripStopResponseBuilder tripStopResponse = TripStopResponse.builder();

        tripStopResponse.stationId( tripStopStationId( tripStop ) );
        tripStopResponse.stationCode( tripStopStationCode( tripStop ) );
        tripStopResponse.stationName( tripStopStationName( tripStop ) );
        tripStopResponse.arrivalTime( tripStop.getArrivalTime() );
        tripStopResponse.departureTime( tripStop.getDepartureTime() );
        tripStopResponse.orderIndex( tripStop.getOrderIndex() );

        return tripStopResponse.build();
    }

    private String tripTrainCode(Trip trip) {
        if ( trip == null ) {
            return null;
        }
        Train train = trip.getTrain();
        if ( train == null ) {
            return null;
        }
        String code = train.getCode();
        if ( code == null ) {
            return null;
        }
        return code;
    }

    private String tripRouteName(Trip trip) {
        if ( trip == null ) {
            return null;
        }
        Route route = trip.getRoute();
        if ( route == null ) {
            return null;
        }
        String name = route.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    protected List<TripStopResponse> tripStopListToTripStopResponseList(List<TripStop> list) {
        if ( list == null ) {
            return null;
        }

        List<TripStopResponse> list1 = new ArrayList<TripStopResponse>( list.size() );
        for ( TripStop tripStop : list ) {
            list1.add( toTripStopResponse( tripStop ) );
        }

        return list1;
    }

    private Long tripStopStationId(TripStop tripStop) {
        if ( tripStop == null ) {
            return null;
        }
        Station station = tripStop.getStation();
        if ( station == null ) {
            return null;
        }
        Long id = station.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String tripStopStationCode(TripStop tripStop) {
        if ( tripStop == null ) {
            return null;
        }
        Station station = tripStop.getStation();
        if ( station == null ) {
            return null;
        }
        String code = station.getCode();
        if ( code == null ) {
            return null;
        }
        return code;
    }

    private String tripStopStationName(TripStop tripStop) {
        if ( tripStop == null ) {
            return null;
        }
        Station station = tripStop.getStation();
        if ( station == null ) {
            return null;
        }
        String name = station.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
