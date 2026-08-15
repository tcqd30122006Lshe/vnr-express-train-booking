package com.trainbooking.service.impl;

import com.trainbooking.dto.response.RevenueReportResponse;
import com.trainbooking.dto.response.RouteAnalyticsResponse;
import com.trainbooking.entity.Booking;
import com.trainbooking.entity.Route;
import com.trainbooking.entity.Ticket;
import com.trainbooking.repository.BookingRepository;
import com.trainbooking.repository.RouteRepository;
import com.trainbooking.repository.TicketRepository;
import com.trainbooking.service.AnalyticsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    BookingRepository bookingRepository;
    TicketRepository ticketRepository;
    RouteRepository routeRepository;

    @Override
    public RevenueReportResponse getRevenueReport(String startDate, String endDate) {
        List<Booking> bookings = bookingRepository.findAll();

        double totalRevenue = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Booking::getTotalAmount)
                .sum();

        long totalBookings = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .count();

        long totalTicketsSold = ticketRepository.findAll().stream()
                .filter(t -> !"CANCELLED".equalsIgnoreCase(t.getStatus()))
                .count();

        return RevenueReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalBookings(totalBookings)
                .totalTicketsSold(totalTicketsSold)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    public List<RouteAnalyticsResponse> getTicketsByRouteReport() {
        List<Route> routes = routeRepository.findAll();
        List<Ticket> allTickets = ticketRepository.findAll();
        List<RouteAnalyticsResponse> report = new ArrayList<>();

        for (Route route : routes) {
            List<Ticket> routeTickets = allTickets.stream()
                    .filter(t -> t.getTrip() != null && t.getTrip().getRoute() != null
                            && t.getTrip().getRoute().getId().equals(route.getId())
                            && !"CANCELLED".equalsIgnoreCase(t.getStatus()))
                    .toList();

            long ticketsSold = routeTickets.size();
            double revenue = routeTickets.stream().mapToDouble(Ticket::getPrice).sum();

            report.add(RouteAnalyticsResponse.builder()
                    .routeId(route.getId())
                    .routeCode(route.getCode())
                    .routeName(route.getName())
                    .ticketsSold(ticketsSold)
                    .revenue(revenue)
                    .build());
        }

        return report;
    }
}
