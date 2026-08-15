package com.trainbooking.mapper;

import com.trainbooking.dto.request.TrainCreationRequest;
import com.trainbooking.dto.response.CarriageResponse;
import com.trainbooking.dto.response.SeatResponse;
import com.trainbooking.dto.response.TrainResponse;
import com.trainbooking.entity.Carriage;
import com.trainbooking.entity.Seat;
import com.trainbooking.entity.Train;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainMapper {

    @Mapping(target = "id", ignore = true)
    Train toEntity(TrainCreationRequest request);

    TrainResponse toResponse(Train train);

    CarriageResponse toCarriageResponse(Carriage carriage);

    SeatResponse toSeatResponse(Seat seat);
}
