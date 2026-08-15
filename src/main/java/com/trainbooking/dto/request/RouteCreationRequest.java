package com.trainbooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteCreationRequest {

    @NotBlank(message = "ROUTE_CODE_BLANK")
    @Size(max = 20, message = "ROUTE_CODE_BLANK")
    String code;

    @NotBlank(message = "ROUTE_NAME_BLANK")
    @Size(max = 100, message = "ROUTE_NAME_BLANK")
    String name;

    @NotEmpty(message = "ROUTE_STATIONS_EMPTY")
    @Valid
    List<RouteStationRequest> stations;
}
