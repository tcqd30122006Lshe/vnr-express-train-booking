package com.trainbooking.service;

import com.trainbooking.dto.request.SearchTripRequest;
import com.trainbooking.dto.request.TripCreationRequest;
import com.trainbooking.dto.response.CarriageSeatMapResponse;
import com.trainbooking.dto.response.SearchTripResponse;
import com.trainbooking.dto.response.TripResponse;

import java.util.List;

public interface TripService {
    TripResponse createTrip(TripCreationRequest request);
    List<TripResponse> getAllTrips();
    TripResponse getTripById(Long id);
    List<SearchTripResponse> searchTrips(SearchTripRequest request);
    CarriageSeatMapResponse getCarriageSeatMap(Long id, Long carriageId, Long originStationId, Long destStationId);
}