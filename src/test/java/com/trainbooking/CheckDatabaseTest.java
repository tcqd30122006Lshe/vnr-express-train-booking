package com.trainbooking;

import com.trainbooking.entity.*;
import com.trainbooking.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CheckDatabaseTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private com.trainbooking.service.BookingService bookingService;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    public void testCreateBookingSimulation() {
        System.out.println("========== TESTING BOOKING CREATION ==========");
        Station hanoi = stationRepository.findByCode("HAN").orElseThrow();
        Station namdinh = stationRepository.findByCode("NDH").or(() -> stationRepository.findByCode("NDI")).orElseThrow();

        java.time.LocalDate today = java.time.LocalDate.now();
        List<Trip> results = tripRepository.searchTrips(
                hanoi.getId(),
                namdinh.getId(),
                today.atStartOfDay(),
                today.atTime(java.time.LocalTime.MAX)
        );
        org.junit.jupiter.api.Assertions.assertFalse(results.isEmpty(), "Trips must exist");
        Trip trip = results.get(0);

        List<Seat> seats = seatRepository.findAll();
        System.out.println(">>> Total seats in DB: " + seats.size());
        Seat testSeat = seats.get(0);

        com.trainbooking.dto.request.BookingRequest req = com.trainbooking.dto.request.BookingRequest.builder()
                .customerName("Quản Trị Viên VNR")
                .customerPhone("0987654321")
                .customerEmail("admin@gmail.com")
                .tripId(trip.getId())
                .startStationId(hanoi.getId())
                .endStationId(namdinh.getId())
                .tickets(List.of(
                        com.trainbooking.dto.request.TicketRequest.builder()
                                .seatId(testSeat.getId())
                                .passengerName("Quản Trị Viên VNR")
                                .passengerIdCard("066206015610")
                                .build()
                ))
                .build();

        try {
            com.trainbooking.dto.response.BookingResponse response = bookingService.createBooking(req);
            System.out.println(">>> BOOKING SUCCESS! Code=" + response.getBookingCode() + ", Total=" + response.getTotalAmount());
        } catch (Exception e) {
            System.err.println(">>> BOOKING FAILED WITH EXCEPTION:");
            e.printStackTrace();
            throw e;
        }
    }

    @Autowired
    private com.trainbooking.service.AuthenticationService authenticationService;

    @Test
    public void testBookingCreationViaMockMvc() throws Exception {
        com.trainbooking.dto.response.AuthenticationResponse auth = authenticationService.authenticate(
                com.trainbooking.dto.request.AuthenticationRequest.builder()
                        .username("testuser_payment")
                        .password("password123")
                        .build()
        );
        String token = auth.getToken();

        String json = """
                {
                    "customerName": "Quản Trị Viên VNR",
                    "customerPhone": "0987654321",
                    "customerEmail": "admin@gmail.com",
                    "tripId": 2,
                    "startStationId": 1,
                    "endStationId": 3,
                    "tickets": [
                        {
                            "seatId": 13,
                            "passengerName": "Quản Trị Viên VNR",
                            "passengerIdCard": "066206015610"
                        }
                    ]
                }
                """;
        mockMvc.perform(post("/api/v1/bookings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value(1000));
    }
}
