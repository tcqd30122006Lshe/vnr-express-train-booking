package com.trainbooking.service;

import com.trainbooking.entity.Booking;
import com.trainbooking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpiredBookingCleaner {

    private final BookingRepository bookingRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanExpiredPendingBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore("PENDING", threshold);
        if (expiredBookings != null && !expiredBookings.isEmpty()) {
            for (Booking booking : expiredBookings) {
                booking.setStatus("CANCELLED");
                if (booking.getTickets() != null) {
                    booking.getTickets().forEach(t -> t.setStatus("CANCELLED"));
                }
                bookingRepository.save(booking);
                log.info("Auto-cancelled expired booking {} and released hold seats", booking.getBookingCode());
            }
        }
    }
}
