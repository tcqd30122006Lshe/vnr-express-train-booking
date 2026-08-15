package com.trainbooking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    Train train;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    Route route;

    @Column(name = "trip_code", unique = true, nullable = false)
    String tripCode;

    @Column(name = "departure_time", nullable = false)
    LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    LocalDateTime arrivalTime;

    @Column(name = "status", nullable = false)
    @Builder.Default
    String status = "SCHEDULED";

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<TripStop> tripStops = new ArrayList<>();
}
