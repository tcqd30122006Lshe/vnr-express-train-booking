package com.trainbooking.dto.request;

import com.trainbooking.exception.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrVerificationRequest {
    @NotBlank(message = "QR_DATA_NOT_FOUND")
    String qrData;

}
