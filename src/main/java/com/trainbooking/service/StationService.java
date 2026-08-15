package com.trainbooking.service;

import com.trainbooking.dto.request.StationRequest;
import com.trainbooking.dto.response.StationResponse;

import java.util.List;

public interface StationService {

    StationResponse createStation(StationRequest request);

    List<StationResponse> getAllStations();

    StationResponse getStationById(Long id);
}