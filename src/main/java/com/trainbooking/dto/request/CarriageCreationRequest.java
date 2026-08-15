package com.trainbooking.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarriageCreationRequest {

    @NotNull(message = "CARRIAGE_NUMBER_INVALID")
    @Min(value = 1, message = "CARRIAGE_NUMBER_INVALID")
    Integer carriageNumber;


    @NotBlank(message = "CARRIAGE_TYPE_BLANK")
    String carriageType;

    @NotNull(message = "TOTAL_SEATS_INVALID")
    @Min(value = 1, message = "TOTAL_SEATS_INVALID")
    Integer totalSeats;

}
