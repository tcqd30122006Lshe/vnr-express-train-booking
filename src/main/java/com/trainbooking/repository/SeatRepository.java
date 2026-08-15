package com.trainbooking.repository;

import com.trainbooking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat,Long> {

    boolean existsByCarriageIdAndSeatNumber(Long CarriageId, Integer seatNumber);


}
