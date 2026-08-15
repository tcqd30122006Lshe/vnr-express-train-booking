package com.trainbooking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carriages", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"train_id", "carriage_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Carriage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    Train train;

    @Column(name = "carriage_number", nullable = false)
    Integer carriageNumber;

    @Column(name = "carriage_type", nullable = false, length = 50)
    String carriageType;

    @Column(name = "total_seats", nullable = false)
    Integer totalSeats;

    @OneToMany(mappedBy = "carriage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<Seat> seats = new ArrayList<>();
}
