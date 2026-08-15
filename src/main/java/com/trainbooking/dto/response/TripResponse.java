package com.trainbooking.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripResponse {
    Long id;
    String tripCode;
    String trainCode;
    String routeName;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
    String status;
    List<TripStopResponse> tripStops;
}
