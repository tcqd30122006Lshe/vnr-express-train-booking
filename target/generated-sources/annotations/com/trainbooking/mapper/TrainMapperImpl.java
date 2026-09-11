package com.trainbooking.mapper;

import com.trainbooking.dto.request.CarriageCreationRequest;
import com.trainbooking.dto.request.TrainCreationRequest;
import com.trainbooking.dto.response.CarriageResponse;
import com.trainbooking.dto.response.SeatResponse;
import com.trainbooking.dto.response.TrainResponse;
import com.trainbooking.entity.Carriage;
import com.trainbooking.entity.Seat;
import com.trainbooking.entity.Train;
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
public class TrainMapperImpl implements TrainMapper {

    @Override
    public Train toEntity(TrainCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        Train.TrainBuilder train = Train.builder();

        train.basePricePerKm( request.getBasePricePerKm() );
        train.carriages( carriageCreationRequestListToCarriageList( request.getCarriages() ) );
        train.code( request.getCode() );
        train.name( request.getName() );
        train.speed( request.getSpeed() );

        return train.build();
    }

    @Override
    public TrainResponse toResponse(Train train) {
        if ( train == null ) {
            return null;
        }

        TrainResponse.TrainResponseBuilder trainResponse = TrainResponse.builder();

        trainResponse.basePricePerKm( train.getBasePricePerKm() );
        trainResponse.carriages( carriageListToCarriageResponseList( train.getCarriages() ) );
        trainResponse.code( train.getCode() );
        trainResponse.id( train.getId() );
        trainResponse.name( train.getName() );
        trainResponse.speed( train.getSpeed() );

        return trainResponse.build();
    }

    @Override
    public CarriageResponse toCarriageResponse(Carriage carriage) {
        if ( carriage == null ) {
            return null;
        }

        CarriageResponse.CarriageResponseBuilder carriageResponse = CarriageResponse.builder();

        carriageResponse.carriageNumber( carriage.getCarriageNumber() );
        carriageResponse.carriageType( carriage.getCarriageType() );
        carriageResponse.id( carriage.getId() );
        carriageResponse.seats( seatListToSeatResponseList( carriage.getSeats() ) );
        carriageResponse.totalSeats( carriage.getTotalSeats() );

        return carriageResponse.build();
    }

    @Override
    public SeatResponse toSeatResponse(Seat seat) {
        if ( seat == null ) {
            return null;
        }

        SeatResponse.SeatResponseBuilder seatResponse = SeatResponse.builder();

        seatResponse.id( seat.getId() );
        seatResponse.seatNumber( seat.getSeatNumber() );
        seatResponse.seatType( seat.getSeatType() );

        return seatResponse.build();
    }

    protected Carriage carriageCreationRequestToCarriage(CarriageCreationRequest carriageCreationRequest) {
        if ( carriageCreationRequest == null ) {
            return null;
        }

        Carriage.CarriageBuilder carriage = Carriage.builder();

        carriage.carriageNumber( carriageCreationRequest.getCarriageNumber() );
        carriage.carriageType( carriageCreationRequest.getCarriageType() );
        carriage.totalSeats( carriageCreationRequest.getTotalSeats() );

        return carriage.build();
    }

    protected List<Carriage> carriageCreationRequestListToCarriageList(List<CarriageCreationRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<Carriage> list1 = new ArrayList<Carriage>( list.size() );
        for ( CarriageCreationRequest carriageCreationRequest : list ) {
            list1.add( carriageCreationRequestToCarriage( carriageCreationRequest ) );
        }

        return list1;
    }

    protected List<CarriageResponse> carriageListToCarriageResponseList(List<Carriage> list) {
        if ( list == null ) {
            return null;
        }

        List<CarriageResponse> list1 = new ArrayList<CarriageResponse>( list.size() );
        for ( Carriage carriage : list ) {
            list1.add( toCarriageResponse( carriage ) );
        }

        return list1;
    }

    protected List<SeatResponse> seatListToSeatResponseList(List<Seat> list) {
        if ( list == null ) {
            return null;
        }

        List<SeatResponse> list1 = new ArrayList<SeatResponse>( list.size() );
        for ( Seat seat : list ) {
            list1.add( toSeatResponse( seat ) );
        }

        return list1;
    }
}
