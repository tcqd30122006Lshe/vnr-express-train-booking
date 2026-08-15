package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarriageSeatMapResponse {

    Long carriageId;
    Integer carriageNumber;
    String carriageType;
    Integer totalSeats;
    List<SeatStatusResponse> seats;
}
