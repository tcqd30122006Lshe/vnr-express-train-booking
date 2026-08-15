package com.trainbooking.config;

import com.trainbooking.entity.Route;
import com.trainbooking.entity.Station;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.Trip;
import com.trainbooking.repository.RouteRepository;
import com.trainbooking.repository.StationRepository;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.repository.TripRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;

    @PostConstruct
    public void init() {
        log.info(">>> Running DataInitializer for Train Booking System...");

        // 1. Seed Stations if empty
        if (stationRepository.count() == 0) {
            Station hanoi = Station.builder().code("HAN").name("Ga Hà Nội").province("Hà Nội").build();
            Station phuly = Station.builder().code("PLY").name("Ga Phủ Lý").province("Hà Nam").build();
            Station namdinh = Station.builder().code("NDH").name("Ga Nam Định").province("Nam Định").build();
            Station thanhhoa = Station.builder().code("THO").name("Ga Thanh Hóa").province("Thanh Hóa").build();
            Station vinh = Station.builder().code("VII").name("Ga Vinh").province("Nghệ An").build();
            Station hue = Station.builder().code("HUE").name("Ga Huế").province("Thừa Thiên Huế").build();
            Station danang = Station.builder().code("DAN").name("Ga Đà Nẵng").province("Đà Nẵng").build();
            Station nhatrang = Station.builder().code("NTR").name("Ga Nha Trang").province("Khánh Hòa").build();
            Station saigon = Station.builder().code("SGO").name("Ga Sài Gòn").province("TP. Hồ Chí Minh").build();

            stationRepository.saveAll(List.of(hanoi, phuly, namdinh, thanhhoa, vinh, hue, danang, nhatrang, saigon));
            log.info(">>> Seeded 9 initial Train Stations (HAN, DAN, SGO...).");
        }

        // 2. Seed Trains if empty
        if (trainRepository.count() == 0) {
            Train se1 = Train.builder().code("SE1").name("Tàu Bắc Nam Express SE1").speed(85.0).basePricePerKm(1200.0).build();
            Train se3 = Train.builder().code("SE3").name("Tàu Bắc Nam Express SE3").speed(80.0).basePricePerKm(1100.0).build();
            Train se7 = Train.builder().code("SE7").name("Tàu Nhanh SE7").speed(75.0).basePricePerKm(1000.0).build();

            trainRepository.saveAll(List.of(se1, se3, se7));
            log.info(">>> Seeded 3 initial Trains (SE1, SE3, SE7).");
        }

        // 3. Seed Route if empty
        if (routeRepository.count() == 0) {
            Route bacNam = Route.builder().code("HN-SG").name("Tuyến Đường Sắt Bắc Nam (Hà Nội - Sài Gòn)").build();
            routeRepository.save(bacNam);
            log.info(">>> Seeded 1 initial Route (Bắc - Nam).");
        }

        // 4. Seed Trips if empty
        if (tripRepository.count() == 0 && trainRepository.count() > 0 && routeRepository.count() > 0) {
            Train se1 = trainRepository.findAll().get(0);
            Route route = routeRepository.findAll().get(0);

            LocalDateTime now = LocalDateTime.now();

            Trip trip1 = Trip.builder()
                    .tripCode("TRIP-SE1-01")
                    .train(se1)
                    .route(route)
                    .departureTime(now.plusHours(2))
                    .arrivalTime(now.plusHours(34))
                    .status("SCHEDULED")
                    .build();

            Trip trip2 = Trip.builder()
                    .tripCode("TRIP-SE3-02")
                    .train(se1)
                    .route(route)
                    .departureTime(now.plusHours(12))
                    .arrivalTime(now.plusHours(44))
                    .status("SCHEDULED")
                    .build();

            tripRepository.saveAll(List.of(trip1, trip2));
            log.info(">>> Seeded 2 initial Trips (TRIP-SE1-01, TRIP-SE3-02).");
        }

        // 5. Ensure existing trains have speed & base price set
        for (Train train : trainRepository.findAll()) {
            boolean updated = false;
            if (train.getSpeed() == null || train.getSpeed() <= 0) {
                train.setSpeed(80.0);
                updated = true;
            }
            if (train.getBasePricePerKm() == null || train.getBasePricePerKm() <= 0) {
                train.setBasePricePerKm(1000.0);
                updated = true;
            }
            if (updated) {
                trainRepository.save(train);
            }
        }
    }
}
