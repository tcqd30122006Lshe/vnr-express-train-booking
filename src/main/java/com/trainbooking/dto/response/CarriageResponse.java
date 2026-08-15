package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarriageResponse {
    Long id;
    Integer carriageNumber;
    String carriageType;
    Integer totalSeats;
    List<SeatResponse> seats;
}
