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
    public void vnPayCallBack(@RequestParam Map<String,String> queryParams, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        PaymentResponse paymentResponse = vnPayService.processVnPayCallback(queryParams);
        String bookingCode = queryParams.get("vnp_TxnRef");
        boolean isSuccess = "00".equals(queryParams.get("vnp_ResponseCode")) && "OK".equalsIgnoreCase(paymentResponse.getStatus());
        String redirectUrl = "/index.html?paymentStatus=" + (isSuccess ? "success" : "failed") + "&bookingCode=" + (bookingCode != null ? bookingCode : "");
        response.sendRedirect(redirectUrl);
    }
}
