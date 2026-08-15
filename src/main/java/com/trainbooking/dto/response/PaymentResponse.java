package com.trainbooking.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    String status;      // "OK" hoặc "FAILED"
    String message;     // Thông báo chi tiết
    String paymentUrl;  // Đường link URL VNPAY / MOMO chứa mã băm để quét QR
}
