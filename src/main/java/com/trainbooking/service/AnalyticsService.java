package com.trainbooking.service;

import com.trainbooking.dto.response.RevenueReportResponse;
import com.trainbooking.dto.response.RouteAnalyticsResponse;

import java.util.List;

public interface AnalyticsService {
    RevenueReportResponse getRevenueReport(String startDate, String endDate);
    List<RouteAnalyticsResponse> getTicketsByRouteReport();
}
