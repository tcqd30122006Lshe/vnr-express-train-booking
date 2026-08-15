package com.trainbooking.dto.response;



import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    String username;

    String fullName;

    String email;

    String phone;

    String idCard;

    LocalDate dob;

    String gender;

    String passengerType;

    String address;

    String taxCode;

    String companyName;
}
