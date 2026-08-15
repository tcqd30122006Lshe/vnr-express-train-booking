package com.trainbooking.service.impl;

import com.trainbooking.dto.request.CarriageCreationRequest;
import com.trainbooking.dto.request.TrainCreationRequest;
import com.trainbooking.dto.response.TrainResponse;
import com.trainbooking.entity.Carriage;
import com.trainbooking.entity.Seat;
import com.trainbooking.entity.Train;
import com.trainbooking.exception.AppException;
import com.trainbooking.exception.ErrorCode;
import com.trainbooking.mapper.TrainMapper;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.service.TrainService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrainServiceImpl implements TrainService {

    TrainRepository trainRepository;
    TrainMapper trainMapper;

    @Override
    @Transactional
    public TrainResponse createTrain(TrainCreationRequest request) {
        if (trainRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.TRAIN_EXISTED);
        }

        Train train = Train.builder()
                .code(request.getCode())
                .name(request.getName())
                .speed(request.getSpeed())
                .basePricePerKm(request.getBasePricePerKm() != null ? request.getBasePricePerKm() : 1000.0)
                .build();

        List<Carriage> carriageList = new ArrayList<>();
        for (CarriageCreationRequest carriageCreationRequest : request.getCarriages()) {
            Carriage carriage = Carriage.builder()
                    .train(train)
                    .carriageNumber(carriageCreationRequest.getCarriageNumber())
                    .carriageType(carriageCreationRequest.getCarriageType())
                    .totalSeats(carriageCreationRequest.getTotalSeats())
                    .build();

            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= carriageCreationRequest.getTotalSeats(); i++) {
                Seat seat = Seat.builder()
                        .carriage(carriage)
                        .seatNumber(i)
                        .seatType(determineSeatType(carriageCreationRequest.getCarriageType(), i))
                        .build();
                seats.add(seat);
            }
            carriage.setSeats(seats);
            carriageList.add(carriage);
        }
        train.setCarriages(carriageList);

        Train savedTrain = trainRepository.save(train);

        return trainMapper.toResponse(savedTrain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainResponse> getAllTrains() {
        return trainRepository.findAll().stream()
                .map(trainMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainResponse getTrainById(Long id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAIN_NOT_FOUND));
        return trainMapper.toResponse(train);
    }

    String determineSeatType(String carriageType, int seatNumber) {
        if ("SLEEPER_4".equalsIgnoreCase(carriageType) || "SLEEPER_6".equalsIgnoreCase(carriageType)) {
            return (seatNumber % 2 != 0) ? "LOWER_BED" : "UPPER_BED";
        }
        return (seatNumber % 2 != 0) ? "WINDOW" : "AISLE";
    }
}
