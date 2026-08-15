package com.trainbooking.service.impl;

import com.trainbooking.dto.request.BookingRequest;
import com.trainbooking.dto.request.TicketRequest;
import com.trainbooking.dto.response.BookingResponse;
import com.trainbooking.entity.*;
import com.trainbooking.exception.AppException;
import com.trainbooking.exception.ErrorCode;
import com.trainbooking.mapper.BookingMapper;
import com.trainbooking.repository.BookingRepository;
import com.trainbooking.repository.SeatRepository;
import com.trainbooking.repository.TicketRepository;
import com.trainbooking.repository.TripRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements com.trainbooking.service.BookingService {

    BookingRepository bookingRepository;
    TripRepository tripRepository;
    SeatRepository seatRepository;
    TicketRepository ticketRepository;
    BookingMapper bookingMapper;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public BookingResponse createBooking(BookingRequest request) {

        String bookingCode = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        TripStop originStop = null;
        TripStop destStop = null;

        for (TripStop stop : trip.getTripStops()) {
            if (stop.getStation().getId().equals(request.getStartStationId())) {
                originStop = stop;
            }
            if (stop.getStation().getId().equals(request.getEndStationId())) {
                destStop = stop;
            }
        }

        if (originStop == null || destStop == null || originStop.getOrderIndex() >= destStop.getOrderIndex()) {
            throw new AppException(ErrorCode.INVALID_JOURNEY);
        }

        List<RouteStation> routeStations = trip.getRoute().getRouteStationList();
        Double startDistance = null;
        Double endDistance = null;

        for (RouteStation rs : routeStations) {
            if (rs.getStation().getId().equals(request.getStartStationId())) {
                startDistance = rs.getDistanceFromOrigin();
            }
            if (rs.getStation().getId().equals(request.getEndStationId())) {
                endDistance = rs.getDistanceFromOrigin();
            }
        }

        if (startDistance == null || endDistance == null) {
            throw new AppException(ErrorCode.INVALID_JOURNEY);
        }

        double distance = Math.abs(endDistance - startDistance);
        double ticketPrice = distance * (trip.getTrain().getBasePricePerKm() != null ? trip.getTrain().getBasePricePerKm() : 1000.0);

        List<Ticket> tickets = new ArrayList<>();
        for (TicketRequest tk : request.getTickets()) {
            Seat seat = seatRepository.findById(tk.getSeatId())
                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

            boolean isOverlapping = ticketRepository.existsOverlappingTicket(
                    trip.getId(), seat.getId(), originStop.getOrderIndex(), destStop.getOrderIndex()
            );

            if (isOverlapping) {
                throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
            }

            Ticket ticket = Ticket.builder()
                    .trip(trip)
                    .seat(seat)
                    .startStation(originStop.getStation())
                    .endStation(destStop.getStation())
                    .startOrderIndex(originStop.getOrderIndex())
                    .endOrderIndex(destStop.getOrderIndex())
                    .passengerName(tk.getPassengerName())
                    .passengerIdCard(tk.getPassengerIdCard())
                    .price(ticketPrice)
                    .status("PENDING")
                    .build();

            tickets.add(ticket);
        }

        Double totalAmount = tickets.size() * ticketPrice;
        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .totalAmount(totalAmount)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .tickets(tickets)
                .build();

        for (Ticket tk : tickets) {
            tk.setBooking(booking);
        }
        Booking savedBooking = bookingRepository.save(booking);
        BookingResponse response = bookingMapper.toBookingResponse(savedBooking);

        response.setTripId(trip.getId());
        response.setTripCode(trip.getTripCode());
        response.setTrainName(trip.getTrain().getName());
        response.setStartStationName(originStop.getStation().getName());
        response.setEndStationName(destStop.getStation().getName());

        return response;
    }
    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByCode(String bookingCode){
        Booking booking= bookingRepository.findByBookingCode(bookingCode).orElseThrow(()->new AppException(ErrorCode.BOOKING_NOT_FOUND));
        BookingResponse response=bookingMapper.toBookingResponse(booking);
        if(booking.getTickets()!=null&& !booking.getTickets().isEmpty()){
            Ticket firstTicket=booking.getTickets().get(0);
            response.setTripId(firstTicket.getTrip().getId());
            response.setTripCode(firstTicket.getTrip().getTripCode());
            response.setTrainName(firstTicket.getTrip().getTrain().getName());
            response.setStartStationName(firstTicket.getStartStation().getName());
            response.setEndStationName(firstTicket.getEndStation().getName());
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(booking -> {
                    BookingResponse response=bookingMapper.toBookingResponse(booking);
                    if(booking.getTickets()!=null&& !booking.getTickets().isEmpty()){
                        Ticket firstTicket=booking.getTickets().get(0);
                        response.setTripId(firstTicket.getTrip().getId());
                        response.setTripCode(firstTicket.getTrip().getTripCode());
                        response.setTrainName(firstTicket.getTrip().getTrain().getName());
                        response.setStartStationName(firstTicket.getStartStation().getName());
                        response.setEndStationName(firstTicket.getEndStation().getName());
                    }
                    return response;

                }).toList();
    }

    @Override
    public void cancelBooking(String bookingCode) {
        Booking booking=bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(()->new AppException(ErrorCode.BOOKING_NOT_FOUND));
        booking.setStatus("CANCELLED");
        if(booking.getTickets()!=null){
            for(Ticket ticket: booking.getTickets()){
                ticket.setStatus("CANCELLED");
            }
        }
        bookingRepository.save(booking);
    }
}
