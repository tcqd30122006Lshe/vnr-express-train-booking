package com.trainbooking.service.impl;

import com.trainbooking.dto.request.StationRequest;
import com.trainbooking.dto.response.StationResponse;
import com.trainbooking.entity.Station;
import com.trainbooking.exception.AppException;
import com.trainbooking.exception.ErrorCode;
import com.trainbooking.mapper.StationMapper;
import com.trainbooking.repository.StationRepository;
import com.trainbooking.service.StationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StationServiceImpl implements StationService {

    StationRepository stationRepository;
    StationMapper stationMapper;

    @Override
    public StationResponse createStation(StationRequest request) {
        // 1. Kiểm tra mã ga trùng -> Ném AppException với ErrorCode chuẩn
        if (stationRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.STATION_EXISTED);
        }

        // 2. Chuyển DTO Request -> Entity dùng MapStruct
        Station station = stationMapper.toEntity(request);

        // 3. Lưu DB
        Station savedStation = stationRepository.save(station);

        // 4. Chuyển Entity -> DTO Response dùng MapStruct
        return stationMapper.toResponse(savedStation);
    }

    @Override
    public List<StationResponse> getAllStations() {
        return stationRepository.findAll()
                .stream()
                .map(stationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StationResponse getStationById(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        return stationMapper.toResponse(station);
    }
}
