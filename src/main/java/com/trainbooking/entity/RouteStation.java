package com.trainbooking.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "route_stations",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"route_id","station_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteStation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id",nullable = false)
    Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id",nullable = false)
    Station station;


    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
    @Column(name = "distance_from_origin", nullable = false)
    private Double distanceFromOrigin;


}
