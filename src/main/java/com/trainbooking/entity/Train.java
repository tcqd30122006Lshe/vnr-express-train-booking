package com.trainbooking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "train_code", unique = true, nullable = false)
    String code;

    @Column(name = "train_name", nullable = false)
    String name;

    @Builder.Default
    @Column(name = "speed", nullable = false)
    Double speed = 60.0;

    @Builder.Default
    @Column(name = "base_price_per_km", nullable = false)
    Double basePricePerKm = 1000.0;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<Carriage> carriages = new ArrayList<>();
}
