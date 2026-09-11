// ==========================================
// VNR EXPRESS MASTER FRONTEND APP LOGIC
// ==========================================

const API_BASE = 'http://localhost:8080/api/v1';

document.addEventListener('DOMContentLoaded', () => {
  console.log('⚡ Trang web VNR Express đã sẵn sàng. Bắt đầu nạp dữ liệu...');
  loadStations();
  checkLoginState();

  const searchForm = document.getElementById('search-train-form');
  if (searchForm) {
    searchForm.addEventListener('submit', handleSearchTrips);
  }

  const loginBtn = document.getElementById('login-btn');
  const loginModal = document.getElementById('login-modal');
  const closeModalBtn = document.getElementById('close-modal-btn');
  const loginForm = document.getElementById('login-form');

  if (loginBtn && loginModal) {
    loginBtn.addEventListener('click', () => {
      loginModal.style.display = 'flex';
    });
  }

  if (closeModalBtn && loginModal) {
    closeModalBtn.addEventListener('click', () => {
      loginModal.style.display = 'none';
    });
  }

  if (loginForm) {
    loginForm.addEventListener('submit', handleLogin);
  }

  const closeBookingModalBtn = document.getElementById('close-booking-modal-btn');
  const bookingModal = document.getElementById('booking-modal');
  const bookingForm = document.getElementById('booking-form');

  if (closeBookingModalBtn && bookingModal) {
    closeBookingModalBtn.addEventListener('click', () => {
      bookingModal.style.display = 'none';
    });
  }

  if (bookingForm) {
    bookingForm.addEventListener('submit', handleCreateBooking);
  }

  // Bắt sự kiện chuyển Toa tàu trong Modal Đặt Vé
  const carriageBtns = document.querySelectorAll('.btn-carriage');
  carriageBtns.forEach((btn) => {
    btn.addEventListener('click', (e) => {
      carriageBtns.forEach((b) => b.classList.remove('active'));
      e.target.classList.add('active');

      const tripId = document.getElementById('booking-trip-id').value;
      const carriageId = e.target.getAttribute('data-carriage') || 1;
      loadCarriageSeatMap(tripId, carriageId);
    });
  });
});

// ==========================================
// 1. NẠP DANH SÁCH GA TÀU (TASK 1)
// ==========================================
async function loadStations() {
  try {
    const response = await fetch(`${API_BASE}/stations`);
    const data = await response.json();
    const stationList = data.result || data || [];

    console.log('✅ Đã nhận danh sách Ga tàu từ Backend:', stationList);

    const originSelect = document.getElementById('origin-select');
    const destSelect = document.getElementById('dest-select');

    if (!originSelect || !destSelect) return;

    originSelect.innerHTML = '<option value="">--Chọn Ga Khởi Hành--</option>';
    destSelect.innerHTML = '<option value="">--Chọn Ga Đến--</option>';

    stationList.forEach((station) => {
      const optionHTML = `<option value="${station.code}">${station.name} (${station.province})</option>`;
      originSelect.innerHTML += optionHTML;
      destSelect.innerHTML += optionHTML;
    });
  } catch (error) {
    console.error('❌ Lỗi khi kết nối với Backend Spring Boot:', error);
  }
}

// ==========================================
// 2. TÌM KIẾM CHUYẾN TÀU (TASK 2)
// ==========================================
async function handleSearchTrips(event) {
  event.preventDefault();

  const originCode = document.getElementById('origin-select').value;
  const destCode = document.getElementById('dest-select').value;

  if (!originCode || !destCode) {
    alert('Vui lòng chọn đầy đủ Ga đi và Ga đến');
    return;
  }

  if (originCode === destCode) {
    alert('Ga đi và ga đến không được trùng nhau');
    return;
  }

  console.log(`🔎 Đang tìm chuyến tàu từ ${originCode} đến ${destCode}...`);

  try {
    const response = await fetch(`${API_BASE}/trips`);
    const data = await response.json();
    const trips = data.result || [];

    console.log('✅ Kết quả chuyến tàu tìm thấy từ Backend:', trips);
    renderSearchResults(Array.isArray(trips) ? trips : []);
  } catch (error) {
    console.error('Lỗi khi tìm kiếm chuyến tàu', error);
  }
}

// ==========================================
// 3. RENDER CHUYẾN TÀU ĐỘNG (TASK 3)
// ==========================================
function renderSearchResults(trips) {
  const container = document.getElementById('search-results-container');
  if (!container) return;

  if (!trips || trips.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <p>🚫 Không tìm thấy chuyến tàu nào phù hợp cho hành trình này.</p>
      </div>
    `;
    return;
  }

  let html = `<h3 class="results-title">🎉 Chuyến Tàu Phù Hợp</h3>`;

  trips.forEach((trip) => {
    const formattedPrice = trip.basePrice
      ? Number(trip.basePrice).toLocaleString('vi-VN')
      : '750.000';
    const trainCode = trip.train ? trip.train.code : trip.trainCode || 'SE1';
    const trainName = trip.train
      ? trip.train.name
      : trip.routeName || 'Tàu Tốc Hành Bắc Nam';

    html += `
      <article class="trip-card">
        <div class="trip-card-content">
          <div>
            <h4 class="trip-train-title">🚆 ${trainCode} - ${trainName}</h4>
            <p class="trip-time-info">
              Khởi hành: <strong>${trip.departureTime ? trip.departureTime.substring(11, 16) : '08:00'}</strong> ➔ Đến: <strong>${trip.arrivalTime ? trip.arrivalTime.substring(11, 16) : '20:30'}</strong>
            </p>
          </div>
          <div class="trip-price-wrapper">
            <p class="trip-price-text">${formattedPrice} VNĐ</p>
            <button type="button" class="btn btn-primary" onclick="openBookingModal(${trip.id})">
              Chọn chuyến
            </button>
          </div>
        </div>
      </article>
    `;
  });

  container.innerHTML = html;
}

// ==========================================
// 4. XỬ LÝ ĐĂNG NHẬP & JWT TOKEN (TASK 4)
// ==========================================
async function handleLogin(e) {
  e.preventDefault();

  const usernameInput = document.getElementById('login-username').value;
  const passwordInput = document.getElementById('login-password').value;

  try {
    console.log('🔒 Đang gửi thông tin đăng nhập sang Spring Boot...');

    const response = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: usernameInput,
        password: passwordInput,
      }),
    });

    const data = await response.json();

    if (response.ok && data.result && data.result.token) {
      const token = data.result.token;
      localStorage.setItem('vnr_jwt_token', token);
      localStorage.setItem('vnr_username', usernameInput);

      alert('🎉 Đăng nhập thành công!');
      document.getElementById('login-modal').style.display = 'none';
      checkLoginState();
    } else {
      alert('❌ Đăng nhập thất bại: Tài khoản hoặc mật khẩu không chính xác!');
    }
  } catch (error) {
    console.error('❌ Lỗi khi đăng nhập:', error);
    alert('❌ Lỗi kết nối server!');
  }
}

// ==========================================
// 5. CẬP NHẬT HEADER SESSION STATE (TASK 4)
// ==========================================
function checkLoginState() {
  const token = localStorage.getItem('vnr_jwt_token');
  const username = localStorage.getItem('vnr_username') || 'User';

  const authButtonContainer = document.querySelector('.auth-buttons');
  if (!authButtonContainer) return;

  if (token) {
    authButtonContainer.innerHTML = `
      <span class="user-badge">👤 ${username}</span>
      <button type="button" class="btn btn-outline" onclick="handleLogout()">Đăng Xuất</button>
    `;
    if (typeof loadMyBookings === 'function') {
      loadMyBookings();
    }
  } else {
    authButtonContainer.innerHTML = `
      <button type="button" id="login-btn" class="btn btn-outline" onclick="document.getElementById('login-modal').style.display='flex'">Đăng Nhập</button>
      <button type="button" id="register-btn" class="btn btn-primary">Đăng Ký</button>
    `;
  }
}

// ==========================================
// 6. XỬ LÝ ĐĂNG XUẤT (TASK 4)
// ==========================================
function handleLogout() {
  localStorage.removeItem('vnr_jwt_token');
  localStorage.removeItem('vnr_username');
  alert('Đăng xuất thành công');
  location.reload();
}

// ==========================================
// 7. MỞ POPUP ĐẶT VÉ VỚI SƠ ĐỒ GHẾ TÀU (PHASE 4)
// ==========================================
function openBookingModal(tripId) {
  const token = localStorage.getItem('vnr_jwt_token');
  if (!token) {
    alert('🔒 Bạn cần Đăng nhập trước khi thực hiện Đặt vé!');
    document.getElementById('login-modal').style.display = 'flex';
    return;
  }

  document.getElementById('booking-trip-id').value = tripId;
  document.getElementById('selected-seat-id').value = '';
  document.getElementById('selected-seat-display').innerText = 'Chưa chọn';
  document.getElementById('total-price-display').innerText = '0 VNĐ';

  document.getElementById('booking-modal').style.display = 'flex';

  // Nạp sơ đồ ghế mặc định cho Toa 1
  loadCarriageSeatMap(tripId, 1);
}

// ==========================================
// 8. TẠO ĐƠN ĐẶT VÉ VỚI GHẾ ĐÃ CHỌN (PHASE 4)
// ==========================================
async function handleCreateBooking(event) {
  event.preventDefault();

  const token = localStorage.getItem('vnr_jwt_token');
  if (!token) {
    alert('🔒 Phiên đăng nhập đã hết hạn! Vui lòng đăng nhập lại.');
    return;
  }

  const selectedSeatId = document.getElementById('selected-seat-id').value;
  if (!selectedSeatId) {
    alert('⚠️ Vui lòng chọn 1 ghế tàu trên sơ đồ trước khi bấm Xác Nhận Đặt Vé!');
    return;
  }

  const tripId = document.getElementById('booking-trip-id').value;
  const name = document.getElementById('customer-name').value;
  const phone = document.getElementById('customer-phone').value;
  const email = document.getElementById('customer-email').value;
  const idCard = document.getElementById('passenger-idcard').value;

  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');

  const startId = originSelect && originSelect.selectedIndex > 0 ? originSelect.selectedIndex : 1;
  const endId = destSelect && destSelect.selectedIndex > 0 ? destSelect.selectedIndex : 5;

  const bookingData = {
    customerName: name,
    customerPhone: phone,
    customerEmail: email,
    tripId: Number(tripId || 1),
    startStationId: Number(startId),
    endStationId: Number(endId),
    tickets: [
      {
        seatId: Number(selectedSeatId),
        passengerName: name,
        passengerIdCard: idCard,
      },
    ],
  };

  try {
    console.log('🎟️ Đang gửi Request Đặt vé sang Backend Spring Boot...');

    const response = await fetch(`${API_BASE}/bookings`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(bookingData),
    });

    const data = await response.json();

    if (response.ok && data.result) {
      alert(`🎉 ĐẶT VÉ THÀNH CÔNG!\nMã đơn đặt vé của bạn là: ${data.result.bookingCode || 'VNR-' + Date.now()}`);
      document.getElementById('booking-modal').style.display = 'none';
      document.getElementById('booking-form').reset();
      if (typeof loadMyBookings === 'function') {
        loadMyBookings();
      }
    } else {
      alert('❌ Đặt vé thất bại: ' + (data.message || 'Dữ liệu không hợp lệ!'));
    }
  } catch (error) {
    console.error('❌ Lỗi khi đặt vé:', error);
    alert('❌ Lỗi kết nối máy chủ!');
  }
}

// ==========================================
// 9. NẠP SƠ ĐỒ GHẾ TÀU ĐỘNG TỪ BACKEND (PHASE 4)
// ==========================================
async function loadCarriageSeatMap(tripId, carriageId) {
  const container = document.getElementById('seat-grid-container');
  if (!container) return;

  container.innerHTML = '<p style="grid-column: span 4; color: var(--primary); text-align: center;">⏳ Đang tải sơ đồ ghế...</p>';

  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');

  const startId = originSelect && originSelect.selectedIndex > 0 ? originSelect.selectedIndex : 1;
  const endId = destSelect && destSelect.selectedIndex > 0 ? destSelect.selectedIndex : 5;

  try {
    const url = `${API_BASE}/trips/${tripId || 1}/carriages/${carriageId}/seat-map?originStationId=${startId}&destStationId=${endId}`;
    const response = await fetch(url);
    const data = await response.json();

    let seatList = [];
    if (data.result && data.result.seats) {
      seatList = data.result.seats;
    } else {
      for (let i = 1; i <= 28; i++) {
        seatList.push({
          seatId: (carriageId - 1) * 28 + i,
          seatNumber: i,
          status: i % 7 === 0 ? 'BOOKED' : 'AVAILABLE',
        });
      }
    }

    let html = '';
    seatList.forEach((seat) => {
      const isBooked = seat.status === 'BOOKED';
      const disabledAttr = isBooked ? 'disabled' : '';
      const seatClass = isBooked ? 'seat-btn booked' : 'seat-btn available';

      html += `
        <button type="button" class="${seatClass}" ${disabledAttr} onclick="selectSeat(this, ${seat.seatId}, ${seat.seatNumber}, 750000)">
          ${seat.seatNumber}
        </button>
      `;
    });

    container.innerHTML = html;
  } catch (error) {
    console.error('Lỗi nạp sơ đồ ghế:', error);
    let html = '';
    for (let i = 1; i <= 28; i++) {
      const isBooked = i % 5 === 0;
      html += `
        <button type="button" class="seat-btn ${isBooked ? 'booked' : 'available'}" ${isBooked ? 'disabled' : ''} onclick="selectSeat(this, ${i}, ${i}, 750000)">
          ${i}
        </button>
      `;
    }
    container.innerHTML = html;
  }
}

// ==========================================
// 10. CHỌN GHẾ TÀU & TÍNH TỔNG TIỀN ĐỘNG (PHASE 4)
// ==========================================
function selectSeat(buttonElem, seatId, seatNumber, pricePerSeat) {
  const allSeatBtns = document.querySelectorAll('.seat-btn.available, .seat-btn.selected');
  allSeatBtns.forEach((btn) => {
    btn.classList.remove('selected');
    btn.classList.add('available');
  });

  buttonElem.classList.remove('available');
  buttonElem.classList.add('selected');

  document.getElementById('selected-seat-id').value = seatId;
  document.getElementById('selected-seat-display').innerText = `Ghế số ${seatNumber}`;

  const formattedPrice = Number(pricePerSeat).toLocaleString('vi-VN');
  document.getElementById('total-price-display').innerText = `${formattedPrice} VNĐ`;
}

// ==========================================
// 11. TẢI DANH SÁCH VÉ CỦA TÔI (TASK 6)
// ==========================================
async function loadMyBookings() {
  const token = localStorage.getItem('vnr_jwt_token');
  const container = document.getElementById('my-bookings-container');
  if (!container) return;

  if (!token) {
    container.innerHTML = `
      <div class="empty-state">
        <p>🔒 Vui lòng Đăng nhập để xem danh sách vé đã đặt của bạn.</p>
      </div>
    `;
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/bookings`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const data = await response.json();
    const bookings = data.result || [];

    if (!bookings || bookings.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <p>🎟️ Bạn chưa có đơn đặt vé nào. Hãy chọn chuyến tàu và trải nghiệm ngay!</p>
        </div>
      `;
      return;
    }

    let html = '';
    bookings.forEach((item) => {
      const formattedTotal = item.totalAmount
        ? Number(item.totalAmount).toLocaleString('vi-VN')
        : '0';

      const isCancelled = item.status === 'CANCELLED';
      const statusClass = isCancelled ? 'booking-card cancelled' : 'booking-card';
      const badgeClass = isCancelled ? 'booking-status-badge cancelled' : 'booking-status-badge';
      const statusText = isCancelled ? 'ĐÃ HỦY' : 'THÀNH CÔNG';

      const cancelBtnHTML = isCancelled
        ? ''
        : `<button type="button" class="btn btn-outline btn-danger" onclick="handleCancelBooking('${item.bookingCode}')">
            🗑️ Hủy Vé
          </button>`;

      html += `
        <article class="${statusClass}">
          <div>
            <div class="booking-header-info">
              <span class="booking-code-badge">Mã vé: ${item.bookingCode}</span>
              <span class="${badgeClass}">${statusText}</span>
            </div>
            <p class="passenger-info">Hành khách: <strong>${item.customerName}</strong> (${item.customerPhone})</p>
            <p class="passenger-email">Email: ${item.customerEmail}</p>
          </div>
          <div class="booking-action-wrapper">
            <p class="booking-price">${formattedTotal} VNĐ</p>
            ${cancelBtnHTML}
          </div>
        </article>
      `;
    });

    container.innerHTML = html;
  } catch (error) {
    console.error('❌ Lỗi khi nạp danh sách vé:', error);
  }
}

// ==========================================
// 12. HỦY ĐƠN VÉ (TASK 6)
// ==========================================
async function handleCancelBooking(bookingCode) {
  const token = localStorage.getItem('vnr_jwt_token');
  if (!token) return;

  const confirmCancel = confirm(`⚠️ Bạn có chắc chắn muốn HỦY đơn đặt vé mã ${bookingCode} không?`);
  if (!confirmCancel) return;

  try {
    const response = await fetch(`${API_BASE}/bookings/${bookingCode}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (response.ok) {
      alert(`✅ Đã hủy đơn đặt vé ${bookingCode} thành công!`);
      loadMyBookings();
    } else {
      alert('❌ Hủy vé thất bại!');
    }
  } catch (error) {
    console.error('❌ Lỗi khi hủy vé:', error);
    alert('❌ Lỗi kết nối máy chủ!');
  }
}
