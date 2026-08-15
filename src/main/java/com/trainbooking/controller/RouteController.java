package com.trainbooking.controller;

import com.trainbooking.dto.request.RouteCreationRequest;
import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.RouteResponse;
import com.trainbooking.service.RouteService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RouteController {

    RouteService routeService;

    @PostMapping
    public ApiResponse<RouteResponse> createRoute(@RequestBody @Valid RouteCreationRequest request) {
        return ApiResponse.<RouteResponse>builder()
                .code(1000)
                .message("Tạo tuyến đường mới thành công")
                .result(routeService.createRoute(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<RouteResponse>> getAllRoutes() {
        return ApiResponse.<List<RouteResponse>>builder()
                .code(1000)
                .result(routeService.getAllRoutes())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RouteResponse> getRouteById(@PathVariable Long id) {
        return ApiResponse.<RouteResponse>builder()
                .code(1000)
                .result(routeService.getRouteById(id))
                .build();
    }
}
