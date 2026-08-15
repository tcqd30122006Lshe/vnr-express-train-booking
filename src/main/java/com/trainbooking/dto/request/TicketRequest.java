package com.trainbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketRequest {

    @NotNull(message = "SEAT_NOT_FOUND")
    Long seatId;

    @NotBlank(message = "PASSENGER_NAME_BLANK")
    String passengerName;

    @NotBlank(message = "PASSENGER_IDCARD_BLANK")
    String passengerIdCard;
}
