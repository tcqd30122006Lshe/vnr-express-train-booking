package com.trainbooking.service;

import com.nimbusds.jose.JOSEException;
import com.trainbooking.dto.request.AuthenticationRequest;
import com.trainbooking.dto.request.IntrospectRequest;
import com.trainbooking.dto.request.LogoutRequest;
import com.trainbooking.dto.request.UserCreationRequest;
import com.trainbooking.dto.response.AuthenticationResponse;
import com.trainbooking.dto.response.IntrospectResponse;
import com.trainbooking.dto.response.UserResponse;

import java.text.ParseException;

public interface AuthenticationService {

    UserResponse register(UserCreationRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    IntrospectResponse introspect(IntrospectRequest request);

    void logout(LogoutRequest request) throws ParseException, JOSEException;
}
