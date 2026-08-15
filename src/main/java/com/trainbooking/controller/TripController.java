package com.trainbooking.controller;

import com.trainbooking.dto.request.SearchTripRequest;
import com.trainbooking.dto.request.TripCreationRequest;
import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.CarriageSeatMapResponse;
import com.trainbooking.dto.response.SearchTripResponse;
import com.trainbooking.dto.response.TripResponse;
import com.trainbooking.service.TripService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripController {

    TripService tripService;

    @PostMapping
    public ApiResponse<TripResponse> createTrip(@RequestBody @Valid TripCreationRequest request) {
        return ApiResponse.<TripResponse>builder()
                .code(1000)
                .message("Tạo chuyến tàu mới thành công")
                .result(tripService.createTrip(request))
                .build();
    }

    @PostMapping("/search")
    public ApiResponse<List<com.trainbooking.dto.response.SearchTripResponse>> searchTrips(
            @RequestBody @Valid com.trainbooking.dto.request.SearchTripRequest request) {
        return ApiResponse.<List<com.trainbooking.dto.response.SearchTripResponse>>builder()
                .code(1000)
                .message("Tìm kiếm chuyến tàu thành công")
                .result(tripService.searchTrips(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<TripResponse>> getAllTrips() {
        return ApiResponse.<List<TripResponse>>builder()
                .code(1000)
                .result(tripService.getAllTrips())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TripResponse> getTripById(@PathVariable Long id) {
        return ApiResponse.<TripResponse>builder()
                .code(1000)
                .result(tripService.getTripById(id))
                .build();
    }
    @GetMapping("/{tripId}/carriages/{carriageId}/seat-map")
    public ApiResponse<CarriageSeatMapResponse> getCarriageSeatMap(@PathVariable Long tripId,@PathVariable Long carriageId,@RequestParam Long originStationId,@RequestParam Long destStationId) {
        return ApiResponse.<CarriageSeatMapResponse>builder()
                .code(1000)
                .message("Lấy sơ đồ ghế thành công")
                .result(tripService.getCarriageSeatMap(tripId, carriageId, originStationId, destStationId))
                .build();

    }
}


