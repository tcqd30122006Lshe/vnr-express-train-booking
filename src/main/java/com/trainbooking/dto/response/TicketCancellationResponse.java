package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketCancellationResponse {
    Long ticketId;

    Double originPrice;

    Double cancellationPrice;

    Double refundAmount;

    String status;

    String message;


}
