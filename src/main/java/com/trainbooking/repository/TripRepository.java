package com.trainbooking.repository;

import com.trainbooking.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    boolean existsByTripCode(String tripCode);

    Optional<Trip> findByTripCode(String tripCode);

    @Query("""
                    SELECT DISTINCT t 
                    FROM Trip t
                    JOIN t.tripStops tsOrigin
                    JOIN t.tripStops tsDest
                    WHERE tsOrigin.station.id= :originId
                    AND tsDest.station.id= :destId
                    AND tsOrigin.orderIndex< tsDest.orderIndex
                    AND tsOrigin.departureTime >= :startOfDay
                    AND tsOrigin.departureTime <= :endOfDay"""
    )
    List<Trip> searchTrips(
            @Param("originId") Long originId,

    @Param("destId") Long destId,
    @Param("startOfDay")
    LocalDateTime startOfDay,
    @Param("endOfDay")
    LocalDateTime endOfDay
    );
}


