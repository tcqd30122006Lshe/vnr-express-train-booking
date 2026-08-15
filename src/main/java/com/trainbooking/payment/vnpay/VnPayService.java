package com.trainbooking.payment.vnpay;

import com.trainbooking.dto.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VnPayService {
    // 1. Hàm tạo đường link URL VNPAY QR Code cho đơn hàng
    PaymentResponse createVnPayPayment(String bookingCode, HttpServletRequest request);
    // 2. Hàm xử lý kết quả Callback / IPN trả về từ VNPAY để chốt đơn PAID
    PaymentResponse processVnPayCallback(Map<String, String> queryParams);
}
