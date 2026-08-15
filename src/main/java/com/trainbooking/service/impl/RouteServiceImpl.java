package com.trainbooking.service.impl;

import com.trainbooking.dto.request.RouteCreationRequest;
import com.trainbooking.dto.response.RouteResponse;
import com.trainbooking.dto.response.RouteStationResponse;
import com.trainbooking.entity.Route;
import com.trainbooking.entity.RouteStation;
import com.trainbooking.entity.Station;
import com.trainbooking.exception.AppException;
import com.trainbooking.exception.ErrorCode;
import com.trainbooking.repository.RouteRepository;
import com.trainbooking.repository.StationRepository;
import com.trainbooking.service.RouteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RouteServiceImpl implements RouteService {

    RouteRepository routeRepository;
    StationRepository stationRepository;

    @Override
    @Transactional
    public RouteResponse createRoute(RouteCreationRequest request) {
        // 1. Kiểm tra trùng mã tuyến
        if (routeRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.ROUTE_EXISTED);
        }

        // 2. Khởi tạo đối tượng Route
        Route route = Route.builder()
                .code(request.getCode())
                .name(request.getName())
                .build();

        // 3. DÙNG JAVA STREAM: Chuyển đổi danh sách RouteStationRequest -> List<RouteStation>
        List<RouteStation> routeStations = request.getStations().stream()
                .map(item -> {
                    // Tìm Station trong DB theo ID
                    Station station = stationRepository.findById(item.getStationId())
                            .orElseThrow(() -> new AppException(ErrorCode.STATION_NOT_FOUND));

                    // Tạo đối tượng RouteStation liên kết
                    return RouteStation.builder()
                            .route(route)
                            .station(station)
                            .orderIndex(item.getOrderIndex())
                            .distanceFromOrigin(item.getDistanceFromOrigin())
                            .build();
                })
                .toList();

        // Gán danh sách trạm dừng vào Route
        route.setRouteStationList(routeStations);

        // 4. Lưu vào Database (Hibernate tự động lưu cả Route lẫn routeStations nhờ CascadeType.ALL)
        Route savedRoute = routeRepository.save(route);

        // 5. DÙNG JAVA STREAM: Biến đổi Entity savedRoute -> RouteResponse trả về cho Client
        return mapToResponse(savedRoute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND));
        return mapToResponse(route);
    }

    // Helper method: Dùng Java Stream chuyển đổi Route Entity -> RouteResponse DTO
    private RouteResponse mapToResponse(Route route) {
        List<RouteStationResponse> stationResponses = route.getRouteStationList().stream()
                .map(rs -> RouteStationResponse.builder()
                        .stationId(rs.getStation().getId())
                        .stationCode(rs.getStation().getCode())
                        .stationName(rs.getStation().getName())
                        .orderIndex(rs.getOrderIndex())
                        .distanceFromOrigin(rs.getDistanceFromOrigin())
                        .build())
                .toList();

        return RouteResponse.builder()
                .id(route.getId())
                .code(route.getCode())
                .name(route.getName())
                .stations(stationResponses)
                .build();
    }
}
