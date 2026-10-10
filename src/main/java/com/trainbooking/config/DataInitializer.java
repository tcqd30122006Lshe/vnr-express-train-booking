package com.trainbooking.config;

import com.trainbooking.entity.*;
import com.trainbooking.repository.RouteRepository;
import com.trainbooking.repository.StationRepository;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.repository.TripRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

        // 2. Seed Trains with Carriages & Seats if empty
        if (trainRepository.count() == 0) {
            Train se1 = Train.builder().code("SE1").name("Tàu Bắc Nam Express SE1").speed(85.0).basePricePerKm(1200.0).build();
            se1.setCarriages(createDefaultCarriages(se1));

            Train se3 = Train.builder().code("SE3").name("Tàu Bắc Nam Express SE3").speed(80.0).basePricePerKm(1100.0).build();
            se3.setCarriages(createDefaultCarriages(se3));

            Train se7 = Train.builder().code("SE7").name("Tàu Nhanh SE7").speed(75.0).basePricePerKm(1000.0).build();
            se7.setCarriages(createDefaultCarriages(se7));

            trainRepository.saveAll(List.of(se1, se3, se7));
            log.info(">>> Seeded 3 initial Trains (SE1, SE3, SE7) with carriages and seats.");
        }

        // 3. Ensure all existing trains have speed, base price, carriages, and seats
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
            if (train.getCarriages() == null || train.getCarriages().isEmpty()) {
                train.setCarriages(createDefaultCarriages(train));
                updated = true;
            }
            if (updated) {
                trainRepository.save(train);
            }
        }

        // 4. Seed Routes (HN-SG and SG-HN) with 9 RouteStations
        List<Station> stations = stationRepository.findAll();
        Map<String, Station> stationMap = stations.stream()
                .collect(Collectors.toMap(Station::getCode, s -> s));

        Station han = stationMap.get("HAN");
        Station ply = stationMap.get("PLY");
        Station ndh = stationMap.get("NDH");
        Station tho = stationMap.get("THO");
        Station vii = stationMap.get("VII");
        Station hue = stationMap.get("HUE");
        Station dan = stationMap.get("DAN");
        Station ntr = stationMap.get("NTR");
        Station sgo = stationMap.get("SGO");

        if (han != null && sgo != null) {
            // Tuyến Bắc - Nam (Hà Nội -> Sài Gòn)
            Route bacNam = routeRepository.findByCode("HN-SG").orElseGet(() -> {
                Route r = Route.builder().code("HN-SG").name("Tuyến Đường Sắt Bắc Nam (Hà Nội - Sài Gòn)").build();
                return routeRepository.save(r);
            });

            if (bacNam.getRouteStationList() == null || bacNam.getRouteStationList().isEmpty()) {
                List<RouteStation> bacNamStations = new ArrayList<>(List.of(
                        RouteStation.builder().route(bacNam).station(han).orderIndex(1).distanceFromOrigin(0.0).build(),
                        RouteStation.builder().route(bacNam).station(ply).orderIndex(2).distanceFromOrigin(56.0).build(),
                        RouteStation.builder().route(bacNam).station(ndh).orderIndex(3).distanceFromOrigin(87.0).build(),
                        RouteStation.builder().route(bacNam).station(tho).orderIndex(4).distanceFromOrigin(175.0).build(),
                        RouteStation.builder().route(bacNam).station(vii).orderIndex(5).distanceFromOrigin(319.0).build(),
                        RouteStation.builder().route(bacNam).station(hue).orderIndex(6).distanceFromOrigin(688.0).build(),
                        RouteStation.builder().route(bacNam).station(dan).orderIndex(7).distanceFromOrigin(791.0).build(),
                        RouteStation.builder().route(bacNam).station(ntr).orderIndex(8).distanceFromOrigin(1315.0).build(),
                        RouteStation.builder().route(bacNam).station(sgo).orderIndex(9).distanceFromOrigin(1726.0).build()
                ));
                bacNam.setRouteStationList(bacNamStations);
                routeRepository.save(bacNam);
                log.info(">>> Seeded 9 route stations for HN-SG.");
            }

            // Tuyến Nam - Bắc (Sài Gòn -> Hà Nội)
            Route namBac = routeRepository.findByCode("SG-HN").orElseGet(() -> {
                Route r = Route.builder().code("SG-HN").name("Tuyến Đường Sắt Nam Bắc (Sài Gòn - Hà Nội)").build();
                return routeRepository.save(r);
            });

            if (namBac.getRouteStationList() == null || namBac.getRouteStationList().isEmpty()) {
                List<RouteStation> namBacStations = new ArrayList<>(List.of(
                        RouteStation.builder().route(namBac).station(sgo).orderIndex(1).distanceFromOrigin(0.0).build(),
                        RouteStation.builder().route(namBac).station(ntr).orderIndex(2).distanceFromOrigin(411.0).build(),
                        RouteStation.builder().route(namBac).station(dan).orderIndex(3).distanceFromOrigin(935.0).build(),
                        RouteStation.builder().route(namBac).station(hue).orderIndex(4).distanceFromOrigin(1038.0).build(),
                        RouteStation.builder().route(namBac).station(vii).orderIndex(5).distanceFromOrigin(1407.0).build(),
                        RouteStation.builder().route(namBac).station(tho).orderIndex(6).distanceFromOrigin(1551.0).build(),
                        RouteStation.builder().route(namBac).station(ndh).orderIndex(7).distanceFromOrigin(1639.0).build(),
                        RouteStation.builder().route(namBac).station(ply).orderIndex(8).distanceFromOrigin(1670.0).build(),
                        RouteStation.builder().route(namBac).station(han).orderIndex(9).distanceFromOrigin(1726.0).build()
                ));
                namBac.setRouteStationList(namBacStations);
                routeRepository.save(namBac);
                log.info(">>> Seeded 9 route stations for SG-HN.");
            }
        }

        // 5. Seed Trips if empty or missing stops
        List<Train> allTrains = trainRepository.findAll();
        List<Route> allRoutes = routeRepository.findAll();

        if (tripRepository.count() == 0 && !allTrains.isEmpty() && !allRoutes.isEmpty()) {
            Train se1 = allTrains.get(0);
            Train se3 = allTrains.size() > 1 ? allTrains.get(1) : se1;
            Train se7 = allTrains.size() > 2 ? allTrains.get(2) : se1;

            Route routeHnSg = routeRepository.findByCode("HN-SG").orElse(allRoutes.get(0));
            Route routeSgHn = routeRepository.findByCode("SG-HN").orElse(routeHnSg);

            LocalDateTime now = LocalDateTime.now();

            Trip trip1 = Trip.builder()
                    .tripCode("TRIP-SE1-01")
                    .train(se1)
                    .route(routeHnSg)
                    .departureTime(now.plusHours(2))
                    .arrivalTime(now.plusHours(34))
                    .status("SCHEDULED")
                    .build();
            generateTripStops(trip1);

            Trip trip2 = Trip.builder()
                    .tripCode("TRIP-SE3-02")
                    .train(se3)
                    .route(routeHnSg)
                    .departureTime(now.plusHours(12))
                    .arrivalTime(now.plusHours(44))
                    .status("SCHEDULED")
                    .build();
            generateTripStops(trip2);

            Trip trip3 = Trip.builder()
                    .tripCode("TRIP-SE7-03")
                    .train(se7)
                    .route(routeSgHn)
                    .departureTime(now.plusHours(6))
                    .arrivalTime(now.plusHours(38))
                    .status("SCHEDULED")
                    .build();
            generateTripStops(trip3);

            tripRepository.saveAll(List.of(trip1, trip2, trip3));
            log.info(">>> Seeded 3 initial Trips with full TripStops (TRIP-SE1-01, TRIP-SE3-02, TRIP-SE7-03).");
        }

        // 6. Ensure existing trips have trip stops
        for (Trip trip : tripRepository.findAll()) {
            if (trip.getTripStops() == null || trip.getTripStops().isEmpty()) {
                generateTripStops(trip);
                tripRepository.save(trip);
                log.info(">>> Generated TripStops for existing Trip: {}", trip.getTripCode());
            }
        }

        // 7. Ensure Daily Trips for Today and next 14 days
        if (!allRoutes.isEmpty() && !allTrains.isEmpty()) {
            Route mainRoute = allRoutes.get(0);
            Train t1 = allTrains.get(0);
            Train t2 = allTrains.size() > 1 ? allTrains.get(1) : t1;

            java.time.LocalDate today = java.time.LocalDate.now();
            for (int i = 0; i <= 14; i++) {
                java.time.LocalDate targetDate = today.plusDays(i);
                String dateSuffix = targetDate.toString().replace("-", "");

                // Chuyến sáng: 08:00
                String code1 = "SE1-" + dateSuffix;
                if (!tripRepository.existsByTripCode(code1)) {
                    Trip tripMorning = Trip.builder()
                            .tripCode(code1)
                            .train(t1)
                            .route(mainRoute)
                            .departureTime(targetDate.atTime(8, 0))
                            .arrivalTime(targetDate.atTime(20, 30))
                            .status("SCHEDULED")
                            .build();
                    generateTripStops(tripMorning);
                    tripRepository.save(tripMorning);
                }

                // Chuyến trưa: 11:30
                String code2 = "SE3-" + dateSuffix;
                if (!tripRepository.existsByTripCode(code2)) {
                    Trip tripNoon = Trip.builder()
                            .tripCode(code2)
                            .train(t2)
                            .route(mainRoute)
                            .departureTime(targetDate.atTime(11, 30))
                            .arrivalTime(targetDate.atTime(23, 30))
                            .status("SCHEDULED")
                            .build();
                    generateTripStops(tripNoon);
                    tripRepository.save(tripNoon);
                }

                // Chuyến tối: 19:00
                String code3 = "SE7-" + dateSuffix;
                if (!tripRepository.existsByTripCode(code3)) {
                    Trip tripEvening = Trip.builder()
                            .tripCode(code3)
                            .train(t1)
                            .route(mainRoute)
                            .departureTime(targetDate.atTime(19, 0))
                            .arrivalTime(targetDate.plusDays(1).atTime(7, 30))
                            .status("SCHEDULED")
                            .build();
                    generateTripStops(tripEvening);
                    tripRepository.save(tripEvening);
                }
            }
            log.info(">>> Seeded/Verified Daily Trips for 14 days starting from {}", today);
        }
    }

    private List<Carriage> createDefaultCarriages(Train train) {
        List<Carriage> carriages = new ArrayList<>();

        // Toa 1: Ghế ngồi mềm (28 ghế)
        Carriage c1 = Carriage.builder().train(train).carriageNumber(1).carriageType("SOFT_SEAT").totalSeats(28).build();
        List<Seat> s1 = new ArrayList<>();
        for (int i = 1; i <= 28; i++) {
            int mod = i % 4;
            String type = (mod == 1 || mod == 0) ? "WINDOW" : "AISLE";
            s1.add(Seat.builder().carriage(c1).seatNumber(i).seatType(type).build());
        }
        c1.setSeats(s1);
        carriages.add(c1);

        // Toa 2: Giường nằm khoang 4 VIP (28 ghế)
        Carriage c2 = Carriage.builder().train(train).carriageNumber(2).carriageType("SLEEPER_4").totalSeats(28).build();
        List<Seat> s2 = new ArrayList<>();
        for (int i = 1; i <= 28; i++) {
            String type = (i % 2 != 0) ? "LOWER_BED" : "UPPER_BED";
            s2.add(Seat.builder().carriage(c2).seatNumber(i).seatType(type).build());
        }
        c2.setSeats(s2);
        carriages.add(c2);

        // Toa 3: Giường nằm khoang 6 (30 ghế)
        Carriage c3 = Carriage.builder().train(train).carriageNumber(3).carriageType("SLEEPER_6").totalSeats(30).build();
        List<Seat> s3 = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            int mod = i % 3;
            String type = (mod == 1) ? "LOWER_BED" : (mod == 2) ? "MIDDLE_BED" : "UPPER_BED";
            s3.add(Seat.builder().carriage(c3).seatNumber(i).seatType(type).build());
        }
        c3.setSeats(s3);
        carriages.add(c3);

        return carriages;
    }

    private void generateTripStops(Trip trip) {
        Route route = trip.getRoute();
        if (route == null || route.getRouteStationList() == null || route.getRouteStationList().isEmpty()) {
            return;
        }

        double speed = (trip.getTrain() != null && trip.getTrain().getSpeed() != null) ? trip.getTrain().getSpeed() : 80.0;
        int total = route.getRouteStationList().size();
        List<TripStop> stops = new ArrayList<>();
        LocalDateTime lastDep = trip.getDepartureTime() != null ? trip.getDepartureTime() : LocalDateTime.now();
        double lastDist = 0;

        for (RouteStation rs : route.getRouteStationList()) {
            double segDist = rs.getDistanceFromOrigin() - lastDist;
            long segMin = (long) ((segDist / speed) * 60);

            LocalDateTime arr = null;
            LocalDateTime dep = null;

            if (rs.getOrderIndex() == 1) {
                dep = trip.getDepartureTime();
            } else if (rs.getOrderIndex() == total) {
                arr = lastDep.plusMinutes(segMin);
                trip.setArrivalTime(arr);
            } else {
                arr = lastDep.plusMinutes(segMin);
                dep = arr.plusMinutes(10);
            }

            lastDep = (dep != null) ? dep : arr;
            lastDist = rs.getDistanceFromOrigin();

            stops.add(TripStop.builder()
                    .trip(trip)
                    .station(rs.getStation())
                    .orderIndex(rs.getOrderIndex())
                    .arrivalTime(arr)
                    .departureTime(dep)
                    .build());
        }
        trip.setTripStops(stops);
    }
}
