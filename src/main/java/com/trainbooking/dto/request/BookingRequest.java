package com.trainbooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {

    @NotBlank(message = "CUSTOMER_NAME_BLANK")
    String customerName;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "CUSTOMER_PHONE_INVALID"
    )
    String customerPhone;

    @NotBlank(message = "CUSTOMER_EMAIL_INVALID")
    @Email(message = "CUSTOMER_EMAIL_INVALID")
    String customerEmail;

    @NotNull(message = "TRIP_ID_NULL")
    Long tripId;

    @NotNull(message = "STATION_ID_NULL")
    Long startStationId;

    @NotNull(message = "STATION_ID_NULL")
    Long endStationId;

    @NotEmpty(message = "TICKET_LIST_EMPTY")
    @Valid
    @Builder.Default
    List<TicketRequest> tickets = new ArrayList<>();
}
