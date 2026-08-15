package com.trainbooking.service;

import com.trainbooking.dto.request.TrainCreationRequest;
import com.trainbooking.dto.response.TrainResponse;

import java.util.List;

public interface TrainService {
    TrainResponse createTrain(TrainCreationRequest request);
    List<TrainResponse> getAllTrains();
    TrainResponse getTrainById(Long id);
}
