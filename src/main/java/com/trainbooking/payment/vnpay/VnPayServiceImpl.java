package com.trainbooking.payment.vnpay;

import com.trainbooking.dto.response.PaymentResponse;
import com.trainbooking.entity.Booking;
import com.trainbooking.entity.Ticket;
import com.trainbooking.exception.AppException;
import com.trainbooking.exception.ErrorCode;
import com.trainbooking.repository.BookingRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VnPayServiceImpl implements VnPayService {

    // 1. Đọc các thông số cấu hình VNPAY Sandbox từ application.properties
    @NonFinal
    @Value("${vnpay.url}")
    String vnp_PayUrl;
    @NonFinal
    @Value("${vnpay.returnUrl}")
    String vnp_ReturnUrl;
    @NonFinal
    @Value("${vnpay.tmnCode}")
    String vnp_TmnCode;
    @NonFinal
    @Value("${vnpay.secretKey}")
    String secretKey;
    @NonFinal
    @Value("${vnpay.version}")
    String vnp_Version;
    @NonFinal
    @Value("${vnpay.command}")
    String vnp_Command;

    BookingRepository bookingRepository;
    @Override
    public PaymentResponse createVnPayPayment(String bookingCode, HttpServletRequest request) {
        //Tìm đơn hàng trong mySql
        Booking booking=bookingRepository.findByBookingCode(bookingCode).orElseThrow(()->new AppException(ErrorCode.BOOKING_NOT_FOUND));

        //Tính tổng tiền
        long amount=(long) (booking.getTotalAmount()*100);
        // BƯỚC 3: Đóng gói tất cả các tham số bắt buộc theo chuẩn VNPAY
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB"); // Mở thẳng trang nhập thẻ NCB Sandbox
        vnp_Params.put("vnp_TxnRef", booking.getBookingCode());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan ve tau don hang: " + booking.getBookingCode());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", VnPayUtil.getIpAddress(request));
        // BƯỚC 4: Tạo thời gian tạo giao dịch & thời gian hết hạn (sau 15 phút)
        // Dùng ZonedDateTime ép chuẩn giờ Việt Nam (Asia/Ho_Chi_Minh) bất chấp máy tính ở Ấn Độ hay Mỹ
        java.time.ZonedDateTime nowInVietnam = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String vnp_CreateDate = nowInVietnam.format(formatter);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        String vnp_ExpireDate = nowInVietnam.plusMinutes(15).format(formatter);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        // BƯỚC 5: Sắp xếp danh sách các tham số theo thứ tự bảng chữ cái Alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Nối chuỗi để tính chữ ký băm HMAC
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                // Nối chuỗi query parameters cho URL
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        // BƯỚC 6: Tính toán chữ ký băm HMAC-SHA512 bảo mật
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayUtil.hmacSHA512(secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        // BƯỚC 7: Nối thành đường link URL hoàn chỉnh của VNPAY
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;
        return PaymentResponse.builder()
                .status("OK")
                .message("Tạo đường link thanh toán VNPAY QR thành công")
                .paymentUrl(paymentUrl)
                .build();




    }

    @Override
    public PaymentResponse processVnPayCallback(Map<String, String> queryParams) {
        String vnp_SecureHash = queryParams.get("vnp_SecureHash");
        // Loại bỏ tham số chữ ký cũ ra khỏi Map để đối soát
        Map<String, String> fields = new HashMap<>(queryParams);
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        // Sắp xếp lại danh sách tham số theo alphabet
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        // Tự tính lại chữ ký băm
        String calculatedHash = VnPayUtil.hmacSHA512(secretKey, hashData.toString());
        // Đối soát chữ ký (Chống sửa tiền trên đường truyền)
        if (!calculatedHash.equalsIgnoreCase(vnp_SecureHash)) {
            return PaymentResponse.builder()
                    .status("FAILED")
                    .message("Chữ ký không hợp lệ! Nghi vấn gian lận.")
                    .build();
        }
        String responseCode = queryParams.get("vnp_ResponseCode");
        String bookingCode = queryParams.get("vnp_TxnRef");
        // Mã "00" = Thanh toán thành công 100%!
        if ("00".equals(responseCode)) {
            Booking booking = bookingRepository.findByBookingCode(bookingCode)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
            // Đổi trạng thái Booking -> CONFIRMED (Đã thanh toán)
            booking.setStatus("CONFIRMED");
            // Đổi trạng thái tất cả Ticket -> VALID (Có hiệu lực đi tàu!)
            if (booking.getTickets() != null) {
                for (Ticket ticket : booking.getTickets()) {
                    ticket.setStatus("VALID");
                }
            }
            bookingRepository.save(booking); // Lưu xuống MySQL
            return PaymentResponse.builder()
                    .status("OK")
                    .message("Thanh toán thành công! Vé đi tàu của bạn đã chính thức có hiệu lực.")
                    .build();
        } else {
            return PaymentResponse.builder()
                    .status("FAILED")
                    .message("Thanh toán thất bại hoặc bị hủy bỏ.")
                    .build();
        }
    }
}
