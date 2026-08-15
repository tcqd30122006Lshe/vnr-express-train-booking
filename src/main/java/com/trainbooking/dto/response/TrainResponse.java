package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrainResponse {
    Long id;
    String code;
    String name;
    Double speed;
    Double basePricePerKm;
    List<CarriageResponse> carriages;
}

