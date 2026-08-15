package com.trainbooking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "booking_code", unique = true, nullable = false)
    String bookingCode;

    @Column(name = "customer_name", nullable = false)
    String customerName;

    @Column(name = "customer_phone", nullable = false)
    String customerPhone;

    @Column(name = "customer_email")
    String customerEmail;

    @Column(name = "total_amount", nullable = false)
    Double totalAmount;

    @Column(name = "status", nullable = false)
    @Builder.Default
    String status = "CONFIRMED";

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<Ticket> tickets = new ArrayList<>();
}
