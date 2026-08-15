package com.trainbooking.controller;

import com.trainbooking.dto.request.StationRequest;
import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.StationResponse;
import com.trainbooking.service.StationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StationController {

    StationService stationService;

    @PostMapping
    public ApiResponse<StationResponse> createStation(@RequestBody @Valid StationRequest request) {
        return ApiResponse.<StationResponse>builder()
                .code(1000)
                .message("Tạo ga tàu mới thành công")
                .result(stationService.createStation(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<StationResponse>> getAllStations() {
        return ApiResponse.<List<StationResponse>>builder()
                .code(1000)
                .result(stationService.getAllStations())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StationResponse> getStationById(@PathVariable Long id) {
        return ApiResponse.<StationResponse>builder()
                .code(1000)
                .result(stationService.getStationById(id))
                .build();
    }
}