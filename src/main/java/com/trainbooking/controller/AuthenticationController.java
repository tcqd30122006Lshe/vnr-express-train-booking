package com.trainbooking.controller;

import com.nimbusds.jose.JOSEException;
import com.trainbooking.dto.request.AuthenticationRequest;
import com.trainbooking.dto.request.IntrospectRequest;
import com.trainbooking.dto.request.LogoutRequest;
import com.trainbooking.dto.request.UserCreationRequest;
import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.AuthenticationResponse;
import com.trainbooking.dto.response.IntrospectResponse;
import com.trainbooking.dto.response.UserResponse;
import com.trainbooking.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder().result(authenticationService.register(request)).build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder().result(authenticationService.authenticate(request)).build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return ApiResponse.<IntrospectResponse>builder().result(authenticationService.introspect(request)).build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().message("Xóa thành công").build();
    }
}
