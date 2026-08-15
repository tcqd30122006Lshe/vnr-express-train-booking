package com.trainbooking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_stops", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"trip_id", "station_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    Station station;

    @Column(name = "order_index", nullable = false)
    Integer orderIndex;

    @Column(name = "arrival_time")
    LocalDateTime arrivalTime;

    @Column(name = "departure_time")
    LocalDateTime departureTime;
}
