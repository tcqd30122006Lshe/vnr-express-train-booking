package com.trainbooking.controller;

import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.BookingResponse;
import com.trainbooking.dto.response.PaymentResponse;
import com.trainbooking.entity.Booking;
import com.trainbooking.entity.Ticket;
import com.trainbooking.exception.AppException;
import com.trainbooking.exception.ErrorCode;
import com.trainbooking.mapper.BookingMapper;
import com.trainbooking.payment.vnpay.VnPayService;
import com.trainbooking.repository.BookingRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    VnPayService vnPayService;
    BookingRepository bookingRepository;
    BookingMapper bookingMapper;

    @GetMapping("/vnpay-pay/{bookingCode}")
    public ApiResponse<PaymentResponse> createVnPayPayment(@PathVariable String bookingCode, HttpServletRequest request){
        return ApiResponse.<PaymentResponse>builder()
                .message("Tạo link thanh toán VNPAY QR thành công")
                .result(vnPayService.createVnPayPayment(bookingCode,request))
                .build();
    }

    @GetMapping("/vnpay-callback")
    public void vnPayCallBack(@RequestParam Map<String,String> queryParams, HttpServletResponse response) throws IOException {
        PaymentResponse paymentResponse = vnPayService.processVnPayCallback(queryParams);
        String bookingCode = queryParams.get("vnp_TxnRef");
        boolean isSuccess = "00".equals(queryParams.get("vnp_ResponseCode")) && "OK".equalsIgnoreCase(paymentResponse.getStatus());
        String redirectUrl = "/index.html?paymentStatus=" + (isSuccess ? "success" : "failed") + "&bookingCode=" + (bookingCode != null ? bookingCode : "");
        response.sendRedirect(redirectUrl);
    }

    @org.springframework.transaction.annotation.Transactional
    @RequestMapping(value = "/cash-pay/{bookingCode}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResponse<BookingResponse> payWithCash(@PathVariable String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        booking.setStatus("CONFIRMED");
        if (booking.getTickets() != null) {
            for (Ticket ticket : booking.getTickets()) {
                ticket.setStatus("VALID");
            }
        }
        bookingRepository.save(booking);

        BookingResponse response = bookingMapper.toBookingResponse(booking);
        if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
            Ticket firstTicket = booking.getTickets().get(0);
            if (firstTicket.getTrip() != null) {
                response.setTripId(firstTicket.getTrip().getId());
                response.setTripCode(firstTicket.getTrip().getTripCode());
                if (firstTicket.getTrip().getTrain() != null) {
                    response.setTrainName(firstTicket.getTrip().getTrain().getName());
                }
            }
            if (firstTicket.getStartStation() != null) {
                response.setStartStationName(firstTicket.getStartStation().getName());
            }
            if (firstTicket.getEndStation() != null) {
                response.setEndStationName(firstTicket.getEndStation().getName());
            }
        }

        return ApiResponse.<BookingResponse>builder()
                .message("Thanh toán tiền mặt thành công")
                .result(response)
                .build();
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/simulate-success/{bookingCode}")
    public void simulateSuccess(@PathVariable String bookingCode, HttpServletResponse response) throws IOException {
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElse(null);
        if (booking != null) {
            booking.setStatus("CONFIRMED");
            if (booking.getTickets() != null) {
                for (Ticket ticket : booking.getTickets()) {
                    ticket.setStatus("VALID");
                }
            }
            bookingRepository.save(booking);
            response.sendRedirect("/index.html?paymentStatus=success&bookingCode=" + bookingCode);
        } else {
            response.sendRedirect("/index.html?paymentStatus=failed&bookingCode=" + bookingCode);
        }
    }
}
