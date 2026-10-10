package com.trainbooking.service.impl;

import com.trainbooking.dto.response.TicketCancellationResponse;
import com.trainbooking.dto.response.TicketResponse;
import com.trainbooking.entity.Ticket;
import com.trainbooking.exception.AppException;
import com.trainbooking.exception.ErrorCode;
import com.trainbooking.repository.TicketRepository;
import com.trainbooking.service.TicketService;
import com.trainbooking.util.QrCodeTicketUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketServiceImpl implements TicketService {

    TicketRepository ticketRepository;


    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long ticketId) {
        Ticket ticket=ticketRepository.findById(ticketId).orElseThrow(()-> new AppException(ErrorCode.TICKET_NOT_FOUND));
        checkTicketOwnership(ticket);
        return mapToTicketResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByBookingCode(String bookingCode) {
        List<Ticket> tickets = ticketRepository.findByBookingCode(bookingCode);
        if (tickets.isEmpty()) {
            throw new AppException(ErrorCode.TICKET_NOT_FOUND);
        }
        checkTicketOwnership(tickets.get(0));
        return tickets.stream().map(this::mapToTicketResponse).toList();
    }

    @Override
    @Transactional
    public void verifyTicketQr(String qrData) {
        if (qrData == null || !qrData.startsWith("TICKET:")) {
            throw new AppException(ErrorCode.INVALID_QR_CODE);
        }
        String []parts=qrData.split(":");
        Long ticketId=Long.parseLong(parts[1]);
        Ticket ticket=ticketRepository.findById(ticketId).orElseThrow(()->new AppException(ErrorCode.TICKET_NOT_FOUND));

        String status=ticket.getStatus();

        if (status.equals("VALID")) {
            ticket.setStatus("USED");
            ticketRepository.save(ticket);
        }else if(status.equals("USED")){
            throw new AppException(ErrorCode.TICKET_HAS_USED);
        }


    }

    @Override
    @Transactional
    public TicketCancellationResponse cancelTicket(Long ticketId) {
        Ticket ticket= ticketRepository.findById(ticketId).orElseThrow(()-> new AppException(ErrorCode.TICKET_NOT_FOUND));
        checkTicketOwnership(ticket);

        if(!"VALID".equalsIgnoreCase(ticket.getStatus())){
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        // 3. Tính khoảng thời gian từ bây giờ đến giờ tàu chạy (departureTime)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = ticket.getTrip().getDepartureTime();

        // 4. Kiểm tra điều kiện thời gian
        if (now.isAfter(departureTime)) {
            throw new AppException(ErrorCode.CANCELLATION_TIME_EXPIRED);
        }
        
        long hoursUntilDeparture = Duration.between(now, departureTime).toHours();

        if (hoursUntilDeparture < 4) {
            throw new AppException(ErrorCode.CANCELLATION_TIME_EXPIRED);
        }
        
        double feePercentage = (hoursUntilDeparture >= 24) ? 0.10 : 0.20;
        double originPrice=ticket.getPrice();
        double cancellationFee=originPrice*feePercentage;
        double refundAmount=originPrice-cancellationFee;

        // 5. Cập nhật trạng thái vé sang CANCELLED
        ticket.setStatus("CANCELLED");
        ticketRepository.save(ticket);

        // 6. Trả về kết quả
        return TicketCancellationResponse.builder()
                .ticketId(ticketId)
                .originPrice(originPrice)
                .cancellationPrice(cancellationFee)
                .refundAmount(refundAmount)
                .status(ticket.getStatus())
                .message("Hủy vé thành công! Phí hủy: "+feePercentage+"%,Số tiền hoàn lại: "
                + refundAmount+" VNĐ"
                )
                .build();
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        String qrData = "TICKET:" + ticket.getId() + ":"
                + (ticket.getBooking() != null ? ticket.getBooking().getBookingCode() : "") + ":"
                + (ticket.getPassengerIdCard() != null ? ticket.getPassengerIdCard() : "");

        String qrCodeBase64 = QrCodeTicketUtils.generateQrCodeBase64(qrData, 250, 250);
        return TicketResponse.builder()
                .id(ticket.getId())
                .passengerName(ticket.getPassengerName())
                .passengerIdCard(ticket.getPassengerIdCard())
                .seatId(ticket.getSeat() != null ? ticket.getSeat().getId() : null)
                .seatNumber(ticket.getSeat() != null ? ticket.getSeat().getSeatNumber() : null)
                .seatType(ticket.getSeat() != null ? ticket.getSeat().getSeatType() : null)
                .carriageNumber(ticket.getSeat() != null && ticket.getSeat().getCarriage() != null ? ticket.getSeat().getCarriage().getCarriageNumber() : null)
                .carriageType(ticket.getSeat() != null && ticket.getSeat().getCarriage() != null ? ticket.getSeat().getCarriage().getCarriageType() : null)
                .price(ticket.getPrice())
                .status(ticket.getStatus())
                .qrData(qrData)
                .qrCodeBase64(qrCodeBase64)
                .build();
    }

    private void checkTicketOwnership(Ticket ticket) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (!isAdmin && ticket.getBooking() != null && ticket.getBooking().getUserUsername() != null && !ticket.getBooking().getUserUsername().equals(authentication.getName())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
    }
}
