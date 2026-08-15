package com.trainbooking.service;

import com.trainbooking.dto.request.BookingRequest;
import com.trainbooking.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    BookingResponse getBookingByCode(String bookingCode);
    List<BookingResponse> getAllBookings();
    void cancelBooking(String bookingCode);
}