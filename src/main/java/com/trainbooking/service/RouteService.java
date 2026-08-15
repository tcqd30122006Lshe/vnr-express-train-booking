package com.trainbooking.service;

import com.trainbooking.dto.request.RouteCreationRequest;
import com.trainbooking.dto.response.RouteResponse;

import java.util.List;

public interface RouteService {

    RouteResponse createRoute(RouteCreationRequest request);

    List<RouteResponse> getAllRoutes();

    RouteResponse getRouteById(Long id);
}