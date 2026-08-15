package com.trainbooking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteStationRequest {

    @NotNull(message = "STATION_ID_NULL")
    Long stationId;

    @NotNull(message = "ORDER_INDEX_NULL")
    Integer orderIndex;

    @NotNull(message = "DISTANCE_INVALID")
    @PositiveOrZero(message = "DISTANCE_INVALID")
    Double distanceFromOrigin;
}
