package com.trainbooking.repository;

import com.trainbooking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByTripId(Long tripId);

    @Query("SELECT COUNT(t) > 0 FROM Ticket t " +
           "WHERE t.trip.id = :tripId " +
           "AND t.seat.id = :seatId " +
           "AND t.status <> 'CANCELLED' " +
           "AND t.startOrderIndex < :endIndex " +
           "AND t.endOrderIndex > :startIndex")
    boolean existsOverlappingTicket(
            @Param("tripId") Long tripId,
            @Param("seatId") Long seatId,
            @Param("startIndex") Integer startIndex,
            @Param("endIndex") Integer endIndex
    );
    @Query("SELECT DISTINCT t.seat.id FROM Ticket t " +
            "WHERE t.trip.id = :tripId " +
            "AND t.seat.carriage.id = :carriageId " +
            "AND t.status <> 'CANCELLED' " +
            "AND t.startOrderIndex < :endIndex " +
            "AND t.endOrderIndex > :startIndex")
    List<Long> findBookedSeatIds(
            @Param("tripId") Long tripId,
            @Param("carriageId") Long carriageId,
            @Param("startIndex") Integer startIndex,
            @Param("endIndex") Integer endIndex
    );

    @Query("SELECT t FROM Ticket t where t.booking.bookingCode = :bookingCode")
    List<Ticket> findByBookingCode(@Param("bookingCode") String bookingCode);

}
