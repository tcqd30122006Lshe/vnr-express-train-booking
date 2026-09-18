# Báo cáo đánh giá và yêu cầu chỉnh sửa luồng nghiệp vụ
**Dự án:** Đặt vé tàu (Train Ticket Booking)

Sau khi kiểm tra các luồng nghiệp vụ chính của hệ thống, dưới đây là danh sách các lỗi logic (business logic flaws) và các điểm cần phải chỉnh sửa lại để hệ thống hoạt động chính xác và an toàn:

## 1. Lỗi đồng bộ (Race Condition) khi đặt vé (BookingServiceImpl)
- **Vấn đề:** Trong hàm `createBooking`, hệ thống kiểm tra ghế đã được đặt hay chưa bằng hàm `ticketRepository.existsOverlappingTicket()`. Tuy nhiên, đoạn code này không có cơ chế khóa (Lock). Nếu 2 người dùng cùng lúc bấm đặt cùng 1 ghế trên cùng 1 chuyến tàu, cả 2 request sẽ cùng đi qua hàm check này (do database chưa kịp lưu), dẫn đến việc tạo ra **2 vé trùng lặp cho cùng 1 ghế**.
- **Yêu cầu chỉnh sửa:** Bổ sung cơ chế Pessimistic Lock ở câu lệnh SQL (VD: `@Lock(LockModeType.PESSIMISTIC_WRITE)` trong Repository) hoặc sử dụng Distributed Lock (Redis/Redisson) đối với seat/trip cụ thể trước khi thực hiện logic tạo booking.

## 2. Bỏ sót logic tính giá theo Loại Ghế (BookingServiceImpl / TripServiceImpl)
- **Vấn đề:** Hiện tại công thức tính giá vé đang fix cứng là: `distance * basePricePerKm` (Quãng đường x Giá cơ bản). Logic này hoàn toàn bỏ qua loại ghế (`seatType`) hay loại toa (`carriageType`). Thực tế ghế mềm điều hòa, hay giường nằm phải đắt hơn ghế ngồi cứng.
- **Yêu cầu chỉnh sửa:** Cần bổ sung hệ số nhân giá (multiplier) hoặc phụ phí dựa vào `seat.getSeatType()` vào công thức tính `ticketPrice` khi sinh vé.

## 3. Lỗ hổng xử lý IPN/Callback Thanh Toán (VnPayServiceImpl)
- **Vấn đề:** Trong hàm `processVnPayCallback`, khi nhận mã `"00"` (thanh toán thành công) từ VNPAY, code đang trực tiếp đổi status của `Booking` thành `CONFIRMED` và `Ticket` thành `VALID`. Nhưng không hề kiểm tra trạng thái hiện tại của Booking!
- **Nguy cơ:** Nếu người dùng mất hơn 15 phút mới thanh toán, cronjob `BookingExpirationTask` đã chuyển Booking thành `EXPIRED` và hủy vé (nhả ghế cho người khác). Khi callback của VNPAY về tới hệ thống (có thể do delay), nó sẽ "hồi sinh" booking đã hết hạn này thành `CONFIRMED`, dẫn đến trùng ghế hoặc mất tính toàn vẹn dữ liệu.
- **Yêu cầu chỉnh sửa:** Thêm câu lệnh kiểm tra trước khi update: `if (!"PENDING".equals(booking.getStatus())) { throw new AppException(...) / return error; }`

## 4. Thiếu chặt chẽ khi Hủy vé & Hoàn tiền (TicketServiceImpl)
- **Vấn đề:** Trong hàm `cancelTicket`:
  - Đang ném ra `RuntimeException("Không thể hủy vé duowis 4 tiếng")` thay vì dùng chuẩn `AppException` của hệ thống.
  - Khi hủy 1 vé, code chỉ set trạng thái của `Ticket` thành `CANCELLED`. Tuy nhiên, **tổng tiền (`totalAmount`) của `Booking` gốc không được cập nhật trừ đi**. Nếu người dùng hủy 1 vé trong đơn hàng có 3 vé, tổng tiền của booking bị sai lệch.
  - Trạng thái của `Booking` cũng không được quản lý (nếu hủy hết tất cả các vé thì trạng thái Booking cần được đổi thành `CANCELLED` hoặc `PARTIALLY_CANCELLED`).
- **Yêu cầu chỉnh sửa:** Đồng bộ sử dụng `AppException`; Cập nhật lại số tiền hoặc trạng thái của `Booking` gốc khi một ticket thành phần bị thay đổi trạng thái.

## 5. Lỗ hổng bảo mật khi quét mã QR kiểm soát vé (TicketServiceImpl)
- **Vấn đề:** Trong hàm `verifyTicketQr`, logic kiểm tra đang là: `if (status.equals("VALID")) { cập nhật thành USED } else if (status.equals("USED")) { báo lỗi }`.
- **Nguy cơ:** Nếu trạng thái của vé là `"CANCELLED"` (vé đã bị hủy/chưa thanh toán), code không đi vào 2 nhánh if trên và sẽ **kết thúc thành công mà không báo bất kỳ lỗi nào**. Hậu quả: Hành khách dùng vé đã hủy để qua trạm kiểm soát một cách trót lọt.
- **Yêu cầu chỉnh sửa:** Bổ sung xử lý bao quát trạng thái. Nếu `!status.equals("VALID")` thì phải ném ra exception ngay lập tức (VD: Vé không hợp lệ, vé đã bị hủy).

## 6. Rủi ro tính toán sai lịch trình chuyến (TripServiceImpl)
- **Vấn đề:** Hàm `createTrip` lặp qua danh sách `route.getRouteStationList()` để tính toán thời gian `arrivalTime` và `departureTime`. Tuy nhiên, logic này giả định mặc định danh sách List trả về từ database đã được sắp xếp đúng theo `orderIndex`. Nếu Hibernate trả về danh sách ngẫu nhiên, việc tính toán `segmentDistance` sẽ bị số âm hoặc sai lệch thời gian toàn bộ chuyến tàu.
- **Yêu cầu chỉnh sửa:** Đảm bảo phải sắp xếp danh sách các trạm dừng theo thứ tự tuyến đường: `route.getRouteStationList().sort(Comparator.comparing(RouteStation::getOrderIndex))` trước khi lặp tính toán.
