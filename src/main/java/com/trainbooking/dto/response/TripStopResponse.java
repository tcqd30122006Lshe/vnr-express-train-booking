package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripStopResponse {

    Long stationId;
    String stationCode;
    String stationName;
    Integer orderIndex;
    LocalDateTime arrivalTime;
    LocalDateTime departureTime;

}
