package com.trainbooking.controller;

import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.TicketCancellationResponse;
import com.trainbooking.dto.response.TicketResponse;
import com.trainbooking.service.TicketService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketController {

    TicketService ticketService;

    @GetMapping("/{ticketId}")
    public ApiResponse<TicketResponse> getTicketById(@PathVariable Long ticketId) {
        return ApiResponse.<TicketResponse>builder()
                .message("Lấy thông tin Vé Điện Tử thành công")
                .result(ticketService.getTicketById(ticketId))
                .build();
    }

    @GetMapping("/booking/{bookingCode}")
    public ApiResponse<List<TicketResponse>> getTicketsByBookingCode(@PathVariable String bookingCode) {
        return ApiResponse.<List<TicketResponse>>builder()
                .message("Lấy danh sách Vé Điện Tử theo mã đặt vé thành công")
                .result(ticketService.getTicketsByBookingCode(bookingCode))
                .build();
    }

    @PostMapping("/verify-qr")
    public ApiResponse<String> verifyTicketQr(@RequestParam String qrData) {
        ticketService.verifyTicketQr(qrData);
        return ApiResponse.<String>builder()
                .message("Xác thực vé hợp lệ thành công!")
                .result("Vé hợp lệ, đã đổi trạng thái sang USED.")
                .build();
    }
    @PostMapping("/{ticketId}/cancel")
    public ApiResponse<TicketCancellationResponse> cancel(@PathVariable Long ticketId){
        return ApiResponse.<TicketCancellationResponse>builder()
                .message("Hủy vé thành công")
                .result(ticketService.cancelTicket(ticketId))
                .build();
    }

    }

