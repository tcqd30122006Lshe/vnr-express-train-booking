package com.trainbooking.controller;

import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.PaymentResponse;
import com.trainbooking.payment.vnpay.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    VnPayService vnPayService;

    @GetMapping("/vnpay-pay/{bookingCode}")
    public ApiResponse<PaymentResponse> createVnPayPayment(@PathVariable String bookingCode, HttpServletRequest request){
        return ApiResponse.<PaymentResponse>builder()
                .message("Tạo link thanh toán VNPAY QR thành công")
                .result(vnPayService.createVnPayPayment(bookingCode,request))
                .build();
    }

    @GetMapping("/vnpay-callback")
    public ApiResponse<PaymentResponse> vnPayCallBack(@RequestParam Map<String,String> queryParams){
        return  ApiResponse.<PaymentResponse>builder()
                .message("Xử lý phản hồi thành công")
                .result(vnPayService.processVnPayCallback(queryParams))
                .build();
    }
}
