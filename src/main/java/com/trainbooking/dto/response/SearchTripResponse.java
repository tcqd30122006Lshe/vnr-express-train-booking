package com.trainbooking.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class SearchTripResponse {

    Long tripId;

    String tripCode;

    String trainName;

    String trainCode;

    String originStationName;
    LocalDateTime departureTime;

    LocalDateTime arrivalTime;

    Double distance;

    Double basePrice;


}
