package com.trainbooking.mapper;

import com.trainbooking.dto.response.BookingResponse;
import com.trainbooking.dto.response.TicketResponse;
import com.trainbooking.entity.Booking;
import com.trainbooking.entity.Carriage;
import com.trainbooking.entity.Seat;
import com.trainbooking.entity.Ticket;
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
public class BookingMapperImpl implements BookingMapper {

    @Override
    public TicketResponse toTicketResponse(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }

        TicketResponse.TicketResponseBuilder ticketResponse = TicketResponse.builder();

        ticketResponse.seatId( ticketSeatId( ticket ) );
        ticketResponse.seatNumber( ticketSeatSeatNumber( ticket ) );
        ticketResponse.seatType( ticketSeatSeatType( ticket ) );
        ticketResponse.carriageNumber( ticketSeatCarriageCarriageNumber( ticket ) );
        ticketResponse.carriageType( ticketSeatCarriageCarriageType( ticket ) );
        ticketResponse.id( ticket.getId() );
        ticketResponse.passengerIdCard( ticket.getPassengerIdCard() );
        ticketResponse.passengerName( ticket.getPassengerName() );
        ticketResponse.price( ticket.getPrice() );
        ticketResponse.status( ticket.getStatus() );

        return ticketResponse.build();
    }

    @Override
    public BookingResponse toBookingResponse(Booking booking) {
        if ( booking == null ) {
            return null;
        }

        BookingResponse.BookingResponseBuilder bookingResponse = BookingResponse.builder();

        bookingResponse.bookingCode( booking.getBookingCode() );
        bookingResponse.createdAt( booking.getCreatedAt() );
        bookingResponse.customerEmail( booking.getCustomerEmail() );
        bookingResponse.customerName( booking.getCustomerName() );
        bookingResponse.customerPhone( booking.getCustomerPhone() );
        bookingResponse.id( booking.getId() );
        bookingResponse.status( booking.getStatus() );
        bookingResponse.tickets( ticketListToTicketResponseList( booking.getTickets() ) );
        bookingResponse.totalAmount( booking.getTotalAmount() );

        return bookingResponse.build();
    }

    private Long ticketSeatId(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Seat seat = ticket.getSeat();
        if ( seat == null ) {
            return null;
        }
        Long id = seat.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Integer ticketSeatSeatNumber(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Seat seat = ticket.getSeat();
        if ( seat == null ) {
            return null;
        }
        Integer seatNumber = seat.getSeatNumber();
        if ( seatNumber == null ) {
            return null;
        }
        return seatNumber;
    }

    private String ticketSeatSeatType(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Seat seat = ticket.getSeat();
        if ( seat == null ) {
            return null;
        }
        String seatType = seat.getSeatType();
        if ( seatType == null ) {
            return null;
        }
        return seatType;
    }

    private Integer ticketSeatCarriageCarriageNumber(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Seat seat = ticket.getSeat();
        if ( seat == null ) {
            return null;
        }
        Carriage carriage = seat.getCarriage();
        if ( carriage == null ) {
            return null;
        }
        Integer carriageNumber = carriage.getCarriageNumber();
        if ( carriageNumber == null ) {
            return null;
        }
        return carriageNumber;
    }

    private String ticketSeatCarriageCarriageType(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Seat seat = ticket.getSeat();
        if ( seat == null ) {
            return null;
        }
        Carriage carriage = seat.getCarriage();
        if ( carriage == null ) {
            return null;
        }
        String carriageType = carriage.getCarriageType();
        if ( carriageType == null ) {
            return null;
        }
        return carriageType;
    }

    protected List<TicketResponse> ticketListToTicketResponseList(List<Ticket> list) {
        if ( list == null ) {
            return null;
        }

        List<TicketResponse> list1 = new ArrayList<TicketResponse>( list.size() );
        for ( Ticket ticket : list ) {
            list1.add( toTicketResponse( ticket ) );
        }

        return list1;
    }
}
