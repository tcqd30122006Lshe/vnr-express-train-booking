package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketResponse {
    Long id;
    String passengerName;
    String passengerIdCard;
    Long seatId;
    Integer seatNumber;
    String seatType;
    Integer carriageNumber;
    String carriageType;
    Double price;
    String status;
    String qrData;
    String qrCodeBase64;
}
