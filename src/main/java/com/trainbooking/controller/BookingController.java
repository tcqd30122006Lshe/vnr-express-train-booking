package com.trainbooking.controller;


import com.trainbooking.dto.request.BookingRequest;
import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.BookingResponse;
import com.trainbooking.entity.Booking;
import com.trainbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    @Autowired
    BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingResponse> createBooking(@RequestBody @Valid BookingRequest request){
        return ApiResponse.<BookingResponse>builder().message("Bạn đã đặt vé thành công")
                .result(bookingService.createBooking(request)).build();
    }

    @GetMapping
    public ApiResponse<List<BookingResponse>> getAllBooking(){
        return ApiResponse.<List<BookingResponse>>builder().message("Tìm Kiếm")
                .result(bookingService.getAllBookings()).build();
    }
    @GetMapping("/{bookingCode}")
    public ApiResponse<BookingResponse> getBookingByCode(@PathVariable String bookingCode){
        return ApiResponse.<BookingResponse>builder().message("Tìm kiếm theo mã đặt")
                .result(bookingService.getBookingByCode(bookingCode)).build();
    }

    @DeleteMapping("/{bookingCode}")
    public ApiResponse<Void> cancelBooking(@PathVariable String bookingCode){
            bookingService.cancelBooking(bookingCode);
            return ApiResponse.<Void>builder().message("Đã Cancel").build();
    }



}
