package com.trainbooking.controller;

import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.dto.response.RevenueReportResponse;
import com.trainbooking.dto.response.RouteAnalyticsResponse;
import com.trainbooking.service.AnalyticsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyticsController {

    AnalyticsService analyticsService;

    @GetMapping("/revenue")
    public ApiResponse<RevenueReportResponse> getRevenueReport(
            @RequestParam(required = false, defaultValue = "2026-01-01") String startDate,
            @RequestParam(required = false, defaultValue = "2026-12-31") String endDate) {
        return ApiResponse.<RevenueReportResponse>builder()
                .message("Lấy báo cáo doanh thu thành công")
                .result(analyticsService.getRevenueReport(startDate, endDate))
                .build();
    }

    @GetMapping("/tickets-by-route")
    public ApiResponse<List<RouteAnalyticsResponse>> getTicketsByRouteReport() {
        return ApiResponse.<List<RouteAnalyticsResponse>>builder()
                .message("Lấy báo cáo doanh thu theo tuyến đường thành công")
                .result(analyticsService.getTicketsByRouteReport())
                .build();
    }
}
