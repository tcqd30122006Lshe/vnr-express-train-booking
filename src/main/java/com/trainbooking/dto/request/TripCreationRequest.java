package com.trainbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripCreationRequest {


    @NotNull(message = "TRAIN_ID_NULL")
    Long trainId;
    @NotNull(message = "ROUTE_ID_NULL")
    Long routeId;

    @NotBlank(message = "TRIP_CODE_BLANK")
    String tripCode;

    @NotNull(message = "DEPARTURE_TIME_NULL")
    LocalDateTime departureTime;

}