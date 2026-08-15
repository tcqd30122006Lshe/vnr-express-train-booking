package com.trainbooking.service.impl;

import com.trainbooking.dto.request.SearchTripRequest;
import com.trainbooking.dto.request.TripCreationRequest;
import com.trainbooking.dto.response.CarriageSeatMapResponse;
import com.trainbooking.dto.response.SearchTripResponse;
import com.trainbooking.dto.response.SeatStatusResponse;
import com.trainbooking.dto.response.TripResponse;
import com.trainbooking.entity.*;
import com.trainbooking.exception.AppException;
import com.trainbooking.exception.ErrorCode;
import com.trainbooking.mapper.TripMapper;
import com.trainbooking.repository.*;
import com.trainbooking.service.TripService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripServiceImpl implements TripService {

    TripRepository tripRepository;
    TrainRepository trainRepository;
    RouteRepository routeRepository;
    TripMapper tripMapper;
    CarriageRepository carriageRepository;
    TicketRepository ticketRepository;
    @Override
    @Transactional
    public TripResponse createTrip(TripCreationRequest request) {
        // ========== BƯỚC 1: Kiểm tra trùng mã chuyến ==========
        // Tại sao: Tránh Admin bấm 2 lần tạo ra 2 chuyến trùng mã
        if (tripRepository.existsByTripCode(request.getTripCode())) {
            throw new AppException(ErrorCode.TRIP_EXISTED);
        }
        // ========== BƯỚC 2: Xác minh Train và Route có tồn tại không ==========
        // Tại sao: Admin chỉ gửi ID (con số), phải tìm ra đối tượng thật trong DB
        // Nếu không tìm thấy -> Ném lỗi ngay, không chạy tiếp
        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAIN_NOT_FOUND));

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND));

        // ========== BƯỚC 3: Lấy vận tốc của đoàn tàu ==========
        // Tại sao: Mỗi đoàn tàu có vận tốc khác nhau (SE1: 80km/h, TN1: 40km/h)
        // Dùng để tính thời gian di chuyển giữa các ga ở bước 5
        double trainSpeed = train.getSpeed();

        // ========== BƯỚC 4: Dựng khung Trip ==========
        // Tại sao: Tạo "tờ giấy trắng" ghi thông tin chuyến tàu
        // Chưa set arrivalTime vì phải tính ở bước 5 mới biết
        // Không cần set status vì @Builder.Default đã mặc định = "SCHEDULED"
        Trip trip = Trip.builder().train(train).route(route)
                .tripCode(request.getTripCode())
                .departureTime(request.getDepartureTime())
                .build();


        // ========== BƯỚC 5: Tự động bê lịch ga từ Route sang TripStop ==========
        // Kiểm tra tuyến đường phải có ít nhất 2 ga (ga đầu + ga cuối)
        if (route.getRouteStationList() == null || route.getRouteStationList().size() < 2) {
            throw new AppException(ErrorCode.ROUTE_STATIONS_EMPTY);
        }

        int totalStations = route.getRouteStationList().size();
        List<TripStop> tripStops = new ArrayList<>();

        // lastDepartureTime: Nhớ giờ rời ga trước đó (ban đầu = giờ xuất phát)
        // Tại sao: Giờ đến ga sau PHỤ THUỘC vào giờ rời ga trước (cộng dồn)
        LocalDateTime lastDepartureTime = request.getDepartureTime();


        //lastDistance:Khoảng cách giữa  2 ga
        double lastDistance = 0;
        for (RouteStation rs : route.getRouteStationList()) {
            // Khoảng cách giữa ga này và ga trước đó
            double segmentDistance = rs.getDistanceFromOrigin() - lastDistance;

            long segmentMinutes = (long) ((segmentDistance / trainSpeed) * 60);

            LocalDateTime arrivalTime = null;
            LocalDateTime departureTime = null;

            if (rs.getOrderIndex() == 1) {
                departureTime = request.getDepartureTime();
            } else if (rs.getOrderIndex() == totalStations) {
                arrivalTime = lastDepartureTime.plusMinutes(segmentMinutes);
                trip.setArrivalTime(arrivalTime);
            } else {
                arrivalTime = lastDepartureTime.plusMinutes(segmentMinutes);
                departureTime = arrivalTime.plusMinutes(10);
            }
            if (departureTime != null) {
                lastDepartureTime = departureTime;
            } else {
                lastDepartureTime = arrivalTime;
            }
            lastDistance = rs.getDistanceFromOrigin();

            TripStop tripStop = TripStop.builder().trip(trip)
                    .station(rs.getStation())
                    .orderIndex(rs.getOrderIndex())
                    .arrivalTime(arrivalTime)
                    .departureTime(departureTime)
                    .build();
            tripStops.add(tripStop);
        }
        trip.setTripStops(tripStops);

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional(readOnly = true)  // readOnly = true: Tối ưu hiệu năng cho hàm chỉ đọc
    public List<TripResponse> getAllTrips() {
        // Lấy tất cả Trip từ DB -> Chuyển từng cái thành TripResponse
        return tripRepository.findAll().stream()
                .map(tripMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {
        // Tìm Trip theo ID, không thấy thì ném lỗi
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
        return tripMapper.toResponse(trip);
    }

    @Override
    public List<SearchTripResponse> searchTrips(SearchTripRequest request) {
        List<Trip> trips = tripRepository.searchTrips(
                request.getOriginStationId(),
                request.getDestinationStationId(),
                request.getDepartureDate().atStartOfDay(),
                request.getDepartureDate().atTime(LocalTime.MAX)
        );

        List<SearchTripResponse> responses = new ArrayList<>();
        for (Trip trip : trips) {
            TripStop originStop = null;
            TripStop destStop = null;

            for (TripStop tripStop : trip.getTripStops()) {
                if (tripStop.getStation().getId().equals(request.getOriginStationId())) {
                    originStop = tripStop;
                }
                if (tripStop.getStation().getId().equals(request.getDestinationStationId())) {
                    destStop = tripStop;
                }
            }

            if (originStop == null || destStop == null) {
                continue;
            }

            List<RouteStation> routeStations = trip.getRoute().getRouteStationList();
            Double distanceFromOrigin = null;
            Double distanceFromDest = null;

            for (RouteStation routeStation : routeStations) {
                if (routeStation.getStation().getId().equals(request.getOriginStationId())) {
                    distanceFromOrigin = routeStation.getDistanceFromOrigin();
                }
                if (routeStation.getStation().getId().equals(request.getDestinationStationId())) {
                    distanceFromDest = routeStation.getDistanceFromOrigin();
                }
            }

            double distance = 0.0;
            if (distanceFromDest != null && distanceFromOrigin != null) {
                distance = Math.abs(distanceFromDest - distanceFromOrigin);
            }

            double rate = (trip.getTrain() != null && trip.getTrain().getBasePricePerKm() != null)
                    ? trip.getTrain().getBasePricePerKm()
                    : 1000.0;
            double basePrice = distance * rate;

            SearchTripResponse response = SearchTripResponse.builder()
                    .tripId(trip.getId())
                    .tripCode(trip.getTripCode())
                    .trainCode(trip.getTrain() != null ? trip.getTrain().getCode() : null)
                    .trainName(trip.getTrain() != null ? trip.getTrain().getName() : null)
                    .originStationName(originStop.getStation().getName())
                    .departureTime(originStop.getDepartureTime())
                    .arrivalTime(destStop.getArrivalTime())
                    .distance(distance)
                    .basePrice(basePrice)
                    .build();

            responses.add(response);
        }

        return responses;
    }

    @Override
    public CarriageSeatMapResponse getCarriageSeatMap(Long id, Long carriageId, Long originStationId, Long destStationId) {
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
        Carriage carriage = carriageRepository.findById(carriageId).orElseThrow(() -> new AppException(ErrorCode.CARRIAGE_NOT_FOUND));

        Integer originIndex = trip.getTripStops()
                .stream()
                .filter(ts -> ts.getStation().getId().equals(originStationId))
                .findFirst()
                .map(TripStop::getOrderIndex)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

        Integer destIndex = trip.getTripStops()
                .stream()
                .filter(ts -> ts.getStation().getId().equals(destStationId))
                .findFirst()
                .map(TripStop::getOrderIndex)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));
        if (originIndex >= destIndex) {
            throw new AppException(ErrorCode.INVALID_JOURNEY);
        }
        List<Long> bookedSeatIds = ticketRepository.findBookedSeatIds(
                trip.getId(), carriageId, originIndex, destIndex
        );
        List<SeatStatusResponse> seatStatuses=carriage.getSeats().stream()
                .map(seat ->
                    SeatStatusResponse.builder()
                            .seatId(seat.getId())
                            .seatNumber(seat.getSeatNumber())
                            .seatType(seat.getSeatType())
                            .status(bookedSeatIds.contains(seat.getId()) ? "BOOKED" : "AVAILABLE")
                            .build()
                ).toList();
        return CarriageSeatMapResponse.builder()
                .carriageId(carriageId)
                .carriageNumber(carriage.getCarriageNumber())
                .carriageType(carriage.getCarriageType())
                .totalSeats(carriage.getTotalSeats())
                .seats(seatStatuses)
                .build();
    }

}