package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatStatusResponse {

    Long seatId;
    Integer seatNumber;
    String seatType;
    String status; // AVAILABLE hoặc BOOKED
}
