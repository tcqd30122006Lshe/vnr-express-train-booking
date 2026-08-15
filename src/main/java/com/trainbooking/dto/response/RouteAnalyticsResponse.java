package com.trainbooking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteAnalyticsResponse {
    Long routeId;
    String routeCode;
    String routeName;
    Long ticketsSold;
    Double revenue;
}
