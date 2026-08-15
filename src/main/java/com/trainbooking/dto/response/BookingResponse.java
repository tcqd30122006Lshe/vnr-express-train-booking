package com.trainbooking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    Long id;
    String bookingCode; // Ví dụ: "BK-8F9A12"

    // Thông tin người đặt
    String customerName;
    String customerPhone;
    String customerEmail;

    // Thông tin chuyến tàu & chặng đi
    Long tripId;
    String tripCode;
    String trainName;
    String startStationName;
    String endStationName;

    // Tổng tiền đơn hàng & Trạng thái
    Double totalAmount;
    String status;
    LocalDateTime createdAt;

    // Danh sách chi tiết từng chiếc vé trong đơn hàng
    List<TicketResponse> tickets;
}