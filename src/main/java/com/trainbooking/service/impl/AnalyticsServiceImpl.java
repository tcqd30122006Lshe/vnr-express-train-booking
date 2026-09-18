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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(String startDate, String endDate) {
        java.time.LocalDateTime start = java.time.LocalDate.parse(startDate).atStartOfDay();
        java.time.LocalDateTime end = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);

        Double totalRevenue = bookingRepository.getTotalRevenue("CONFIRMED", start, end);
        if (totalRevenue == null) totalRevenue = 0.0;

        Long totalBookings = bookingRepository.countTotalBookings("CONFIRMED", start, end);
        if (totalBookings == null) totalBookings = 0L;

        Long totalTicketsSold = ticketRepository.countNonCancelledTicketsByDateRange(start, end);
        if (totalTicketsSold == null) totalTicketsSold = 0L;

        return RevenueReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalBookings(totalBookings)
                .totalTicketsSold(totalTicketsSold)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<RouteAnalyticsResponse> getTicketsByRouteReport() {
        List<Route> routes = routeRepository.findAll();
        List<RouteAnalyticsResponse> report = new ArrayList<>();

        for (Route route : routes) {
            Long ticketsSold = ticketRepository.countNonCancelledTicketsByRoute(route.getId());
            if (ticketsSold == null) ticketsSold = 0L;

            Double revenue = ticketRepository.sumRevenueByRoute(route.getId());
            if (revenue == null) revenue = 0.0;

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
