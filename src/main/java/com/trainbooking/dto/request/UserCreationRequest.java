package com.trainbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {

    @NotBlank(message = "USERNAME_INVALID")
    @Size(min = 4, max = 50, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "PASSWORD_INVALID")
    @Size(min = 6, message = "PASSWORD_INVALID")
    String password;

    @NotBlank(message = "CUSTOMER_NAME_BLANK")
    String fullName;

    @NotBlank(message = "CUSTOMER_EMAIL_INVALID")
    @Email(message = "CUSTOMER_EMAIL_INVALID")
    String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "CUSTOMER_PHONE_INVALID")
    String phone;

    @Size(max = 20, message = "PASSENGER_IDCARD_BLANK")
    String idCard;

    @Past(message = "INVALID_KEY")
    LocalDate dob;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "INVALID_KEY")
    String gender;

    @Pattern(regexp = "^(ADULT|STUDENT|SENIOR|CHILD)$", message = "INVALID_KEY")
    String passengerType;

    @Size(max = 255, message = "INVALID_KEY")
    String address;

    @Pattern(regexp = "^[0-9]{10,13}$", message = "INVALID_KEY")
    String taxCode;

    @Size(max = 255, message = "INVALID_KEY")
    String companyName;
}