package com.trainbooking.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchTripRequest {

    @NotNull(message = "STATION_ID_NULL")
    Long originStationId;

    @NotNull(message = "STATION_ID_NULL")
    Long destinationStationId;

    @NotNull(message = "DEPARTURE_TIME_NULL")
    @DateTimeFormat(iso =DateTimeFormat.ISO.DATE)
    LocalDate departureDate;
}
