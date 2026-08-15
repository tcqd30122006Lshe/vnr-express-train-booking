package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class RouteResponse {
    Long id;
    String code;
    String name;
    List<RouteStationResponse> stations;

}
