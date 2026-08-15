package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueReportResponse {
    Double totalRevenue;
    Long totalTicketsSold;
    Long totalBookings;
    String startDate;
    String endDate;
}
