package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class RouteStationResponse {
    Long stationId;
    String stationCode;
    String stationName;
    Integer orderIndex;
    Double distanceFromOrigin;

}
