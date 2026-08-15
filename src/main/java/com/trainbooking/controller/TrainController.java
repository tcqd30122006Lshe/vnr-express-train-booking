package com.trainbooking.controller;

import com.trainbooking.dto.request.TrainCreationRequest;
import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.TrainResponse;
import com.trainbooking.service.TrainService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trains")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrainController {

    TrainService trainService;

    @PostMapping
    public ApiResponse<TrainResponse> createTrain(@RequestBody @Valid TrainCreationRequest request) {
        return ApiResponse.<TrainResponse>builder()
                .code(1000)
                .message("Tạo đoàn tàu mới thành công")
                .result(trainService.createTrain(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<TrainResponse>> getAllTrains() {
        return ApiResponse.<List<TrainResponse>>builder()
                .code(1000)
                .result(trainService.getAllTrains())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TrainResponse> getTrainById(@PathVariable Long id) {
        return ApiResponse.<TrainResponse>builder()
                .code(1000)
                .result(trainService.getTrainById(id))
                .build();
    }
}
