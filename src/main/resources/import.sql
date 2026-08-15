-- 1. TẠO DỮ LIỆU 5 GA TÀU
INSERT INTO stations (id, code, name, province) VALUES (1, 'HAN', 'Ga Hà Nội', 'Hà Nội');
INSERT INTO stations (id, code, name, province) VALUES (2, 'PLY', 'Ga Phủ Lý', 'Hà Nam');
INSERT INTO stations (id, code, name, province) VALUES (3, 'NDI', 'Ga Nam Định', 'Nam Định');
INSERT INTO stations (id, code, name, province) VALUES (4, 'THO', 'Ga Thanh Hóa', 'Thanh Hóa');
INSERT INTO stations (id, code, name, province) VALUES (5, 'DNA', 'Ga Đà Nẵng', 'Đà Nẵng');

-- 2. TẠO TUYẾN ĐƯỜNG BẮC - NAM
INSERT INTO routes (id, route_code, route_name) VALUES (1, 'HN-DN', 'Tuyến Bắc Nam (Hà Nội - Đà Nẵng)');

-- 3. TẠO HÀNH TRÌNH CHẶNG GA FOR TUYẾN ĐƯỜNG
INSERT INTO route_stations (id, route_id, station_id, order_index, distance_from_origin) VALUES (1, 1, 1, 1, 0.0);
INSERT INTO route_stations (id, route_id, station_id, order_index, distance_from_origin) VALUES (2, 1, 2, 2, 56.0);
INSERT INTO route_stations (id, route_id, station_id, order_index, distance_from_origin) VALUES (3, 1, 3, 3, 87.0);
INSERT INTO route_stations (id, route_id, station_id, order_index, distance_from_origin) VALUES (4, 1, 4, 4, 175.0);
INSERT INTO route_stations (id, route_id, station_id, order_index, distance_from_origin) VALUES (5, 1, 5, 5, 791.0);

-- 4. TẠO ĐOÀN TÀU SE1
INSERT INTO trains (id, train_code, train_name, base_price_per_km, speed) VALUES (1, 'SE1', 'Tàu Tốc Hành SE1', 1000.0, 80.0);

-- 5. TẠO 2 TOA TÀU
INSERT INTO carriages (id, train_id, carriage_number, carriage_type, total_seats) VALUES (1, 1, 1, 'Ngồi mềm điều hòa', 8);
INSERT INTO carriages (id, train_id, carriage_number, carriage_type, total_seats) VALUES (2, 1, 2, 'Giường nằm khoang 4', 8);

-- 6. TẠO DỮ LIỆU GHẾ (TOA 1: ID 1-8, TOA 2: ID 9-16)
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (1, 1, 1, 'WINDOW');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (2, 1, 2, 'AISLE');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (3, 1, 3, 'AISLE');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (4, 1, 4, 'WINDOW');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (5, 1, 5, 'WINDOW');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (6, 1, 6, 'AISLE');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (7, 1, 7, 'AISLE');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (8, 1, 8, 'WINDOW');

INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (9, 2, 1, 'FLOOR_1');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (10, 2, 2, 'FLOOR_2');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (11, 2, 3, 'FLOOR_1');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (12, 2, 4, 'FLOOR_2');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (13, 2, 5, 'FLOOR_1');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (14, 2, 6, 'FLOOR_2');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (15, 2, 7, 'FLOOR_1');
INSERT INTO seats (id, carriage_id, seat_number, seat_type) VALUES (16, 2, 8, 'FLOOR_2');

-- 7. TẠO CHUYẾN TÀU SE1 CHẠY NGÀY 25/12/2026
INSERT INTO trips (id, train_id, route_id, trip_code, departure_time, arrival_time, status) VALUES (1, 1, 1, 'SE1-20261225', '2026-12-25 06:00:00', '2026-12-25 18:00:00', 'SCHEDULED');

-- 8. TẠO LỊCH TRÌNH DỪNG GA
INSERT INTO trip_stops (id, trip_id, station_id, order_index, arrival_time, departure_time) VALUES (1, 1, 1, 1, NULL, '2026-12-25 06:00:00');
INSERT INTO trip_stops (id, trip_id, station_id, order_index, arrival_time, departure_time) VALUES (2, 1, 2, 2, '2026-12-25 06:42:00', '2026-12-25 06:52:00');
INSERT INTO trip_stops (id, trip_id, station_id, order_index, arrival_time, departure_time) VALUES (3, 1, 3, 3, '2026-12-25 07:15:00', '2026-12-25 07:25:00');
INSERT INTO trip_stops (id, trip_id, station_id, order_index, arrival_time, departure_time) VALUES (4, 1, 4, 4, '2026-12-25 08:31:00', '2026-12-25 08:41:00');
INSERT INTO trip_stops (id, trip_id, station_id, order_index, arrival_time, departure_time) VALUES (5, 1, 5, 5, '2026-12-25 18:00:00', NULL);

-- 9. TẠO 1 ĐƠN HÀNG MẪU VÀ 2 VÉ ĐÃ BÁN MẪU (GHẾ SỐ 1 VÀ GHẾ SỐ 2 CHẶNG HÀ NỘI 1 -> NAM ĐỊNH 3)
INSERT INTO bookings (id, booking_code, customer_name, customer_phone, customer_email, total_amount, status, created_at) VALUES (1, 'BK-TEST001', 'Nguyễn Văn A', '0912345678', 'nguyenvana@gmail.com', 174000.0, 'CONFIRMED', NOW());

INSERT INTO tickets (id, booking_id, trip_id, seat_id, start_station_id, end_station_id, start_order_index, end_order_index, passenger_name, passenger_id_card, price, status) VALUES (1, 1, 1, 1, 1, 3, 1, 3, 'Nguyễn Văn A', '001099123456', 87000.0, 'VALID');
INSERT INTO tickets (id, booking_id, trip_id, seat_id, start_station_id, end_station_id, start_order_index, end_order_index, passenger_name, passenger_id_card, price, status) VALUES (2, 1, 1, 2, 1, 3, 1, 3, 'Trần Thị B', '001099654321', 87000.0, 'VALID');
