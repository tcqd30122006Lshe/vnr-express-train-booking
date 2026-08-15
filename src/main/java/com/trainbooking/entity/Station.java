package com.trainbooking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
     String code;

    @Column(name = "name", nullable = false, length = 100)
     String name;

    @Column(name = "province", nullable = false, length = 100)
     String province;

    @OneToMany(mappedBy = "station",cascade = CascadeType.ALL,orphanRemoval = true)
    List<RouteStation> routeStationList;
}
