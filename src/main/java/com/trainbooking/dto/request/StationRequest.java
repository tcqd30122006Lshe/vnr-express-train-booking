package com.trainbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationRequest {

    @NotBlank(message = "STATION_CODE_BLANK")
    @Size(max = 20, message = "STATION_CODE_INVALID")
    String code;

    @NotBlank(message = "STATION_NAME_BLANK")
    @Size(max = 100, message = "STATION_NAME_INVALID")
    String name;

    @NotBlank(message = "STATION_PROVINCE_BLANK")
    @Size(max = 100, message = "STATION_PROVINCE_BLANK")
    String province;
}