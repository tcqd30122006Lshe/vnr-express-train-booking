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

    @org.springframework.data.jpa.repository.Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = :status AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    Double getTotalRevenue(@org.springframework.data.repository.query.Param("status") String status, 
                           @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate, 
                           @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    Long countTotalBookings(@org.springframework.data.repository.query.Param("status") String status, 
                            @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate, 
                            @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate);
}
