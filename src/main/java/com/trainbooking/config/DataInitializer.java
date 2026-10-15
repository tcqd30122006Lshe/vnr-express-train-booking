package com.trainbooking.config;

import com.trainbooking.entity.*;
import com.trainbooking.repository.BookingRepository;
import com.trainbooking.repository.RouteRepository;
import com.trainbooking.repository.StationRepository;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.repository.TripRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final TransactionTemplate transactionTemplate;

    @PostConstruct
    public void init() {
        transactionTemplate.executeWithoutResult(status -> {
            doInit();
        });
    }

    private void doInit() {
        log.info(">>> Running DataInitializer for Train Booking System (Inside Transaction)...");

        // 1. Ensure all 9 Stations exist (with alias support for NDI/NDH and DNA/DAN)
        Station han = getOrCreateStation("HAN", "Ga Hà Nội", "Hà Nội");
        Station ply = getOrCreateStation("PLY", "Ga Phủ Lý", "Hà Nam");
        Station ndh = stationRepository.findByCode("NDH")
                .or(() -> stationRepository.findByCode("NDI"))
                .orElseGet(() -> stationRepository.save(Station.builder().code("NDH").name("Ga Nam Định").province("Nam Định").build()));
        Station tho = getOrCreateStation("THO", "Ga Thanh Hóa", "Thanh Hóa");
        Station vii = getOrCreateStation("VII", "Ga Vinh", "Nghệ An");
        Station hue = getOrCreateStation("HUE", "Ga Huế", "Thừa Thiên Huế");
        Station dan = stationRepository.findByCode("DAN")
                .or(() -> stationRepository.findByCode("DNA"))
                .orElseGet(() -> stationRepository.save(Station.builder().code("DAN").name("Ga Đà Nẵng").province("Đà Nẵng").build()));
        Station ntr = getOrCreateStation("NTR", "Ga Nha Trang", "Khánh Hòa");
        Station sgo = getOrCreateStation("SGO", "Ga Sài Gòn", "TP. Hồ Chí Minh");
        log.info(">>> Verified 9 Stations in database.");

        // 2. Ensure Trains exist with Carriages & Seats
        Train se1 = getOrCreateTrain("SE1", "Tàu Bắc Nam Express SE1", 85.0, 1200.0);
        Train se3 = getOrCreateTrain("SE3", "Tàu Bắc Nam Express SE3", 80.0, 1100.0);
        Train se7 = getOrCreateTrain("SE7", "Tàu Nhanh SE7", 75.0, 1000.0);

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
        log.info(">>> Verified Trains with carriages and seats.");

        // 3. Seed Routes (HN-SG and SG-HN)
        Route bacNam = routeRepository.findByCode("HN-SG").orElseGet(() -> {
            Route r = Route.builder().code("HN-SG").name("Tuyến Đường Sắt Bắc Nam (Hà Nội - Sài Gòn)").build();
            return routeRepository.save(r);
        });

        if (bacNam.getRouteStationList() == null || bacNam.getRouteStationList().size() < 9) {
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
            log.info(">>> Configured 9 route stations for HN-SG.");
        }

        Route namBac = routeRepository.findByCode("SG-HN").orElseGet(() -> {
            Route r = Route.builder().code("SG-HN").name("Tuyến Đường Sắt Nam Bắc (Sài Gòn - Hà Nội)").build();
            return routeRepository.save(r);
        });

        if (namBac.getRouteStationList() == null || namBac.getRouteStationList().size() < 9) {
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
            log.info(">>> Configured 9 route stations for SG-HN.");
        }

        // 4. Update any existing trips that miss trip stops
        for (Trip trip : tripRepository.findAll()) {
            if (trip.getTripStops() == null || trip.getTripStops().isEmpty()) {
                if (trip.getRoute() == null) {
                    trip.setRoute(bacNam);
                }
                generateTripStops(trip);
                tripRepository.save(trip);
                log.info(">>> Generated TripStops for existing Trip: {}", trip.getTripCode());
            }
        }

        // 5. Seed Daily Trips for Today and next 14 days (Both directions)
        LocalDate today = LocalDate.now();
        for (int i = 0; i <= 14; i++) {
            LocalDate targetDate = today.plusDays(i);
            String dateSuffix = targetDate.toString().replace("-", "");

            // Tuyến Bắc - Nam: HN-SG
            seedSingleTrip("SE1-" + dateSuffix, se1, bacNam, targetDate.atTime(8, 0));
            seedSingleTrip("SE3-" + dateSuffix, se3, bacNam, targetDate.atTime(11, 30));
            seedSingleTrip("SE7-" + dateSuffix, se7, bacNam, targetDate.atTime(19, 0));

            // Tuyến Nam - Bắc: SG-HN
            seedSingleTrip("SE2-" + dateSuffix, se1, namBac, targetDate.atTime(8, 30));
            seedSingleTrip("SE4-" + dateSuffix, se3, namBac, targetDate.atTime(12, 0));
            seedSingleTrip("SE8-" + dateSuffix, se7, namBac, targetDate.atTime(19, 30));
        }
        log.info(">>> Seeded/Verified Daily Trips for 14 days starting from {} in both directions.", today);

        // 6. Seed Demo Booking for PNR Lookup (BK-93FC2027)
        if (bookingRepository.findByBookingCode("BK-93FC2027").isEmpty()) {
            List<Trip> trips = tripRepository.findAll();
            if (!trips.isEmpty()) {
                Trip sampleTrip = trips.get(0);
                Seat sampleSeat = null;
                if (sampleTrip.getTrain() != null && sampleTrip.getTrain().getCarriages() != null) {
                    for (Carriage c : sampleTrip.getTrain().getCarriages()) {
                        if (c.getSeats() != null && !c.getSeats().isEmpty()) {
                            sampleSeat = c.getSeats().get(0);
                            break;
                        }
                    }
                }

                if (sampleSeat != null) {
                    Booking sampleBooking = Booking.builder()
                            .bookingCode("BK-93FC2027")
                            .customerName("Nguyễn Văn An")
                            .customerPhone("0987654321")
                            .customerEmail("nguyenvanan@vnr.vn")
                            .totalAmount(750000.0)
                            .status("CONFIRMED")
                            .createdAt(LocalDateTime.now())
                            .build();

                    Ticket sampleTicket = Ticket.builder()
                            .booking(sampleBooking)
                            .trip(sampleTrip)
                            .seat(sampleSeat)
                            .startStation(han)
                            .endStation(dan)
                            .startOrderIndex(1)
                            .endOrderIndex(7)
                            .passengerName("Nguyễn Văn An")
                            .passengerIdCard("001200001234")
                            .price(750000.0)
                            .status("VALID")
                            .build();

                    sampleBooking.setTickets(new ArrayList<>(List.of(sampleTicket)));
                    bookingRepository.save(sampleBooking);
                    log.info(">>> Seeded demo booking BK-93FC2027 for PNR Lookup.");
                }
            }
        }
    }

    private void seedSingleTrip(String code, Train train, Route route, LocalDateTime departureTime) {
        if (!tripRepository.existsByTripCode(code)) {
            Trip trip = Trip.builder()
                    .tripCode(code)
                    .train(train)
                    .route(route)
                    .departureTime(departureTime)
                    .arrivalTime(departureTime.plusHours(30))
                    .status("SCHEDULED")
                    .build();
            generateTripStops(trip);
            tripRepository.save(trip);
        }
    }

    private Station getOrCreateStation(String code, String name, String province) {
        return stationRepository.findByCode(code).orElseGet(() ->
                stationRepository.save(Station.builder().code(code).name(name).province(province).build())
        );
    }

    private Train getOrCreateTrain(String code, String name, Double speed, Double basePricePerKm) {
        return trainRepository.findByCode(code).orElseGet(() -> {
            Train t = Train.builder().code(code).name(name).speed(speed).basePricePerKm(basePricePerKm).build();
            t.setCarriages(createDefaultCarriages(t));
            return trainRepository.save(t);
        });
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
