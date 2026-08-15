package com.trainbooking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_station_id", nullable = false)
    Station startStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_station_id", nullable = false)
    Station endStation;

    @Column(name = "start_order_index", nullable = false)
    Integer startOrderIndex;

    @Column(name = "end_order_index", nullable = false)
    Integer endOrderIndex;

    @Column(name = "passenger_name", nullable = false)
    String passengerName;

    @Column(name = "passenger_id_card")
    String passengerIdCard;

    @Column(name = "price", nullable = false)
    Double price;

    @Column(name = "status", nullable = false)
    @Builder.Default
    String status = "VALID";
}
