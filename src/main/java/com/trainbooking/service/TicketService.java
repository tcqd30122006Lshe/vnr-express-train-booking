package com.trainbooking.service;

import com.trainbooking.dto.response.TicketCancellationResponse;
import com.trainbooking.dto.response.TicketResponse;
import com.trainbooking.entity.Ticket;

import java.util.List;

public interface TicketService {
    TicketResponse getTicketById(Long ticketId);

    List<TicketResponse> getTicketsByBookingCode(String bookingCode);

    void verifyTicketQr(String qrData);

    TicketCancellationResponse cancelTicket(Long ticketId);

}
