package com.trainbooking.mapper;

import com.trainbooking.dto.response.BookingResponse;
import com.trainbooking.dto.response.TicketResponse;
import com.trainbooking.entity.Booking;
import com.trainbooking.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "seatId", source = "seat.id")
    @Mapping(target = "seatNumber", source = "seat.seatNumber")
    @Mapping(target = "seatType", source = "seat.seatType")
    @Mapping(target = "carriageNumber", source = "seat.carriage.carriageNumber")
    @Mapping(target = "carriageType", source = "seat.carriage.carriageType")
    TicketResponse toTicketResponse(Ticket ticket);


    BookingResponse toBookingResponse(Booking booking);
}