package com.trainbooking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Mã lỗi không hợp lệ", HttpStatus.BAD_REQUEST),

    // Station ErrorCodes (1002 - 1004, 1014-1015)
    STATION_EXISTED(1002, "Mã ga đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    STATION_NOT_FOUND(1003, "Không tìm thấy ga tàu", HttpStatus.NOT_FOUND),
    STATION_CODE_BLANK(1004, "Mã ga không được để trống", HttpStatus.BAD_REQUEST),
    STATION_NAME_BLANK(1014, "Tên ga không được để trống", HttpStatus.BAD_REQUEST),
    STATION_PROVINCE_BLANK(1015, "Tỉnh/Thành phố không được để trống", HttpStatus.BAD_REQUEST),
    STATION_CODE_INVALID(1016, "Mã ga không được vượt quá 20 ký tự", HttpStatus.BAD_REQUEST),
    STATION_NAME_INVALID(1017, "Tên ga không được vượt quá 100 ký tự", HttpStatus.BAD_REQUEST),

    // User & Security ErrorCodes (5001 - 5010)
    USER_EXISTED(5001, "Tên người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(5002, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(5003, "Tên người dùng phải chứa ít nhất 4 ký tự", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(5004, "Mật khẩu phải chứa ít nhất 6 ký tự", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Chưa xác thực hoặc Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Bạn không có quyền truy cập chức năng này", HttpStatus.FORBIDDEN),

    // Route ErrorCodes (1005 - 1013)
    ROUTE_CODE_BLANK(1005, "Mã tuyến không được để trống", HttpStatus.BAD_REQUEST),
    ROUTE_NAME_BLANK(1006, "Tên tuyến không được để trống", HttpStatus.BAD_REQUEST),
    ROUTE_STATIONS_EMPTY(1007, "Tuyến đường phải có ít nhất 1 ga dừng", HttpStatus.BAD_REQUEST),
    STATION_ID_NULL(1008, "ID ga tàu không được để trống", HttpStatus.BAD_REQUEST),
    ORDER_INDEX_NULL(1009, "Thứ tự dừng không được để trống", HttpStatus.BAD_REQUEST),
    DISTANCE_INVALID(1010, "Khoảng cách phải lớn hơn hoặc bằng 0", HttpStatus.BAD_REQUEST),
    ROUTE_EXISTED(1011, "Mã tuyến đường đã tồn tại", HttpStatus.BAD_REQUEST),
    ROUTE_NOT_FOUND(1012, "Không tìm thấy tuyến đường", HttpStatus.NOT_FOUND),
    DUPLICATE_STATION_IN_ROUTE(1013, "Một ga không được xuất hiện 2 lần trong cùng một tuyến", HttpStatus.BAD_REQUEST),

    // Train ErrorCodes (2001 - 2004, 2013)
    TRAIN_CODE_BLANK(2001, "Mã đoàn tàu không được để trống", HttpStatus.BAD_REQUEST),
    TRAIN_NAME_BLANK(2002, "Tên đoàn tàu không được để trống", HttpStatus.BAD_REQUEST),
    TRAIN_EXISTED(2003, "Mã đoàn tàu đã tồn tại", HttpStatus.BAD_REQUEST),
    TRAIN_NOT_FOUND(2004, "Không tìm thấy đoàn tàu", HttpStatus.NOT_FOUND),
    TRAIN_SPEED_INVALID(2013, "Vận tốc đoàn tàu phải lớn hơn 0", HttpStatus.BAD_REQUEST),

    // Carriage ErrorCodes (2005 - 2009, 2012)
    CARRIAGE_NUMBER_INVALID(2005, "Số thứ tự toa phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    CARRIAGE_TYPE_BLANK(2006, "Loại toa không được để trống", HttpStatus.BAD_REQUEST),
    TOTAL_SEATS_INVALID(2007, "Tổng số ghế phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    CARRIAGE_NOT_FOUND(2008, "Không tìm thấy toa tàu", HttpStatus.NOT_FOUND),
    DUPLICATE_CARRIAGE_NUMBER(2009, "Số toa này đã tồn tại trên đoàn tàu", HttpStatus.BAD_REQUEST),

    // Seat ErrorCodes (2010 - 2011)
    SEAT_NUMBER_INVALID(2010, "Số ghế không được để trống", HttpStatus.BAD_REQUEST),
    SEAT_NOT_FOUND(2011, "Không tìm thấy ghế ngồi", HttpStatus.NOT_FOUND),
    CARRIAGE_LIST_EMPTY(2012, "Đoàn tàu phải có ít nhất 1 toa", HttpStatus.BAD_REQUEST),

    // Trip ErrorCodes (3001 - 3010)
    TRIP_CODE_BLANK(3001, "Mã chuyến tàu không được để trống", HttpStatus.BAD_REQUEST),
    TRAIN_ID_NULL(3002, "ID đoàn tàu không được để trống", HttpStatus.BAD_REQUEST),
    ROUTE_ID_NULL(3003, "ID tuyến đường không được để trống", HttpStatus.BAD_REQUEST),
    DEPARTURE_TIME_NULL(3004, "Thời gian khởi hành không được để trống", HttpStatus.BAD_REQUEST),
    TRIP_EXISTED(3005, "Mã chuyến tàu đã tồn tại", HttpStatus.BAD_REQUEST),
    TRIP_NOT_FOUND(3006, "Không tìm thấy chuyến tàu", HttpStatus.NOT_FOUND),

    // Booking ErrorCodes (4001 - 4010)
    BOOKING_NOT_FOUND(4001, "Không tìm thấy đơn đặt vé", HttpStatus.NOT_FOUND),
    SEAT_ALREADY_BOOKED(4002, "Ghế đã có người đặt trong khoảng hành trình này", HttpStatus.BAD_REQUEST),
    INVALID_JOURNEY(4003, "Ga xuất phát và Ga đến không hợp lệ trên tuyến", HttpStatus.BAD_REQUEST),
    CUSTOMER_NAME_BLANK(4004, "Tên người đặt không được để trống", HttpStatus.BAD_REQUEST),
    CUSTOMER_PHONE_INVALID(4005, "Số điện thoại phải bao gồm 10 chữ số", HttpStatus.BAD_REQUEST),
    CUSTOMER_EMAIL_INVALID(4006, "Định dạng email không hợp lệ", HttpStatus.BAD_REQUEST),
    TICKET_LIST_EMPTY(4007, "Đơn đặt vé phải có ít nhất 1 vé", HttpStatus.BAD_REQUEST),
    PASSENGER_NAME_BLANK(4008, "Tên hành khách trên vé không được để trống", HttpStatus.BAD_REQUEST),
    PASSENGER_IDCARD_BLANK(4009, "Số CCCD/CMND của hành khách không được để trống", HttpStatus.BAD_REQUEST),
    TICKET_NOT_FOUND(4010,"Không tìm thấy vé",HttpStatus.NOT_FOUND),
    TICKET_HAS_USED(4011,"Vé đã được sử dụng",HttpStatus.IM_USED),

    // QR ErrorCodes ( 5000- )
    QR_DATA_NOT_FOUND(5000,"Dữ liệu của QrCode không có",HttpStatus.NOT_FOUND),
    INVALID_QR_CODE(5001,"Mã QR không hiệu lực",HttpStatus.BAD_REQUEST),

    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
