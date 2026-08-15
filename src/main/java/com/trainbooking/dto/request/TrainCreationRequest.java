package com.trainbooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class TrainCreationRequest {

    @NotBlank(message = "TRAIN_CODE_BLANK")
    @Size(max = 20, message = "TRAIN_CODE_BLANK")
    String code;

    @NotBlank(message = "TRAIN_NAME_BLANK")
    @Size(max = 100, message = "TRAIN_NAME_BLANK")
    String name;

    @NotNull(message = "TRAIN_SPEED_INVALID")
    @Min(value = 1, message = "TRAIN_SPEED_INVALID")
    Double speed;

    Double basePricePerKm;

    @NotEmpty(message = "CARRIAGE_LIST_EMPTY")
    @Valid
    List<CarriageCreationRequest> carriages;
}

