package com.trainbooking.component;


import com.trainbooking.entity.Booking;
import com.trainbooking.entity.Ticket;
import com.trainbooking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationTask {
    private final BookingRepository bookingRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredPendingBooks(){
        LocalDateTime threshold= LocalDateTime.now().minusMinutes(15);
        List<Booking> expiredBookings =bookingRepository.findByStatusAndCreatedAtBefore("PENDING",threshold);
        List<Booking> finishExpiredBookings=expiredBookings.stream().map(booking -> {
            booking.setStatus("EXPIRED");
            booking.getTickets().forEach(ticket -> ticket.setStatus("CANCELLED"));
            return booking;
        }).toList();
        bookingRepository.saveAll(finishExpiredBookings);
    }


}
