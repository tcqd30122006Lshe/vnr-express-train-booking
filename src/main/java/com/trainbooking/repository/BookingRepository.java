package com.trainbooking.repository;

import com.trainbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByStatusAndCreatedAtBefore(String status, LocalDateTime threshold);
}
