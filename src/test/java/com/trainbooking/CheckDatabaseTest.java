package com.trainbooking;

import com.trainbooking.entity.*;
import com.trainbooking.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class CheckDatabaseTest {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void printDatabaseState() {
        System.out.println("========== DB STATE INSPECTION ==========");
        List<Station> stations = stationRepository.findAll();
        System.out.println(">>> STATIONS (" + stations.size() + "):");
        for (Station s : stations) {
            System.out.println("  ID=" + s.getId() + ", Code=" + s.getCode() + ", Name=" + s.getName() + ", Province=" + s.getProvince());
        }

        List<Train> trains = trainRepository.findAll();
        System.out.println(">>> TRAINS (" + trains.size() + "):");
        for (Train t : trains) {
            int carriages = (t.getCarriages() != null) ? t.getCarriages().size() : 0;
            System.out.println("  ID=" + t.getId() + ", Code=" + t.getCode() + ", Name=" + t.getName() + ", Speed=" + t.getSpeed() + ", Carriages=" + carriages);
        }

        List<Route> routes = routeRepository.findAll();
        System.out.println(">>> ROUTES (" + routes.size() + "):");
        for (Route r : routes) {
            int rStations = (r.getRouteStationList() != null) ? r.getRouteStationList().size() : 0;
            System.out.println("  ID=" + r.getId() + ", Code=" + r.getCode() + ", Name=" + r.getName() + ", StationsCount=" + rStations);
            if (r.getRouteStationList() != null) {
                for (RouteStation rs : r.getRouteStationList()) {
                    System.out.println("    Order=" + rs.getOrderIndex() + ", Station=" + (rs.getStation() != null ? rs.getStation().getCode() : "null") + ", Dist=" + rs.getDistanceFromOrigin());
                }
            }
        }

        List<Trip> trips = tripRepository.findAll();
        System.out.println(">>> TRIPS (" + trips.size() + "):");
        for (Trip tr : trips) {
            int stops = (tr.getTripStops() != null) ? tr.getTripStops().size() : 0;
            System.out.println("  ID=" + tr.getId() + ", Code=" + tr.getTripCode() + ", Route=" + (tr.getRoute() != null ? tr.getRoute().getCode() : "null") + ", Dep=" + tr.getDepartureTime() + ", Stops=" + stops);
        }

        long userCount = userRepository.count();
        System.out.println(">>> USERS COUNT: " + userCount);
        System.out.println("=========================================");
    }

    @Test
    public void testSearchTripsToday() {
        System.out.println("========== TESTING TRIP SEARCH ==========");
        Station hanoi = stationRepository.findByCode("HAN").orElseThrow();
        Station saigon = stationRepository.findByCode("SGO").orElseThrow();

        java.time.LocalDate today = java.time.LocalDate.now();
        List<Trip> results = tripRepository.searchTrips(
                hanoi.getId(),
                saigon.getId(),
                today.atStartOfDay(),
                today.atTime(java.time.LocalTime.MAX)
        );

        System.out.println(">>> SEARCH HAN -> SGO on " + today + ": Found " + results.size() + " trips!");
        for (Trip t : results) {
            System.out.println("    Trip: " + t.getTripCode() + ", Train: " + t.getTrain().getName() + ", Dep: " + t.getDepartureTime());
        }
        org.junit.jupiter.api.Assertions.assertFalse(results.isEmpty(), "Should find trips between Hanoi and Saigon today!");
    }
}
