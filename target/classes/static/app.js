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

  const closeBookingModalBtn = document.getElementById(
    'close-booking-modal-btn',
  );
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
      <div class="empty-state" style="margin-top: 20px; padding: 20px; background: #1E1E1E; border-radius: 8px;">
        <p>🚫 Không tìm thấy chuyến tàu nào phù hợp cho hành trình này.</p>
      </div>
    `;
    return;
  }

  let html = `<h3 style="margin-top: 30px; margin-bottom: 15px; color: var(--primary);">🎉 Chuyến Tàu Phù Hợp</h3>`;

  trips.forEach((trip) => {
    const formattedPrice = trip.basePrice
      ? Number(trip.basePrice).toLocaleString('vi-VN')
      : '750.000';
    const trainCode = trip.train ? trip.train.code : trip.trainCode || 'SE1';
    const trainName = trip.train
      ? trip.train.name
      : trip.routeName || 'Tàu Tốc Hành Bắc Nam';

    html += `
      <article class="trip-card" style="margin-bottom: 15px; background: #1E1E1E; padding: 20px; border-radius: 8px; border-left: 4px solid var(--primary); text-align: left;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <h4 style="font-size: 18px; color: #FFF;">🚆 ${trainCode} - ${trainName}</h4>
            <p style="color: #9E9E9E; font-size: 14px; margin-top: 5px;">
              Khởi hành: <strong>${trip.departureTime ? trip.departureTime.substring(11, 16) : '08:00'}</strong> ➔ Đến: <strong>${trip.arrivalTime ? trip.arrivalTime.substring(11, 16) : '20:30'}</strong>
            </p>
          </div>
          <div style="text-align: right;">
            <p style="color: var(--primary); font-size: 18px; font-weight: bold;">${formattedPrice} VNĐ</p>
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
    // 👇 GỌI HÀM loadMyBookings() KHI ĐỐI TƯỢNG ĐÃ ĐĂNG NHẬP (TASK 6) 👇
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
// 7. MỞ POPUP ĐẶT VÉ (TASK 5)
// ==========================================
function openBookingModal(tripId) {
  const token = localStorage.getItem('vnr_jwt_token');
  if (!token) {
    alert('🔒 Bạn cần Đăng nhập trước khi thực hiện Đặt vé!');
    document.getElementById('login-modal').style.display = 'flex';
    return;
  }

  document.getElementById('booking-trip-id').value = tripId;
  document.getElementById('booking-modal').style.display = 'flex';
}

// ==========================================
// 8. TẠO ĐƠN ĐẶT VÉ (TASK 5)
// ==========================================
async function handleCreateBooking(event) {
  event.preventDefault();

  const token = localStorage.getItem('vnr_jwt_token');
  if (!token) {
    alert('🔒 Phiên đăng nhập đã hết hạn! Vui lòng đăng nhập lại.');
    return;
  }

  const tripId = document.getElementById('booking-trip-id').value;
  const name = document.getElementById('customer-name').value;
  const phone = document.getElementById('customer-phone').value;
  const email = document.getElementById('customer-email').value;
  const idCard = document.getElementById('passenger-idcard').value;

  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');

  const startId =
    originSelect && originSelect.selectedIndex > 0
      ? originSelect.selectedIndex
      : 1;
  const endId =
    destSelect && destSelect.selectedIndex > 0 ? destSelect.selectedIndex : 5;

  const bookingData = {
    customerName: name,
    customerPhone: phone,
    customerEmail: email,
    tripId: Number(tripId || 1),
    startStationId: Number(startId),
    endStationId: Number(endId),
    tickets: [
      {
        seatId: Math.floor(Math.random() * 50) + 2,
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
      alert(
        `🎉 ĐẶT VÉ THÀNH CÔNG!\nMã đơn đặt vé của bạn là: ${data.result.bookingCode || 'VNR-' + Date.now()}`,
      );
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
// 9. TẢI DANH SÁCH VÉ CỦA TÔI (TASK 6 - BẠN TỰ TAY GÕ HÀM loadMyBookings DƯỚI ĐÂY)
// ==========================================
// 1. Hàm nạp danh sách Đơn vé cá nhân từ CSDL MySQL thông qua Spring Boot
async function loadMyBookings() {
  const token = localStorage.getItem('vnr_jwt_token');
  const container = document.getElementById('my-bookings-container');
  if (!container) return;
  if (!token) {
    container.innerHTML = `
      <div class="empty-state" style="padding: 20px; background: #1E1E1E; border-radius: 8px; text-align: center;">
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
        <div class="empty-state" style="padding: 20px; background: #1E1E1E; border-radius: 8px; text-align: center;">
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
      const statusBadgeHTML = isCancelled
        ? `<span style="background: rgba(244, 67, 54, 0.15); color: #F44336; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: bold;">ĐÃ HỦY</span>`
        : `<span style="background: rgba(76, 175, 80, 0.15); color: #4CAF50; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: bold;">THÀNH CÔNG</span>`;
      const cancelBtnHTML = isCancelled
        ? ''
        : `<button type="button" class="btn btn-outline" style="border-color: #F44336; color: #F44336; margin-left: 10px;" onclick="handleCancelBooking('${item.bookingCode}')">
            🗑️ Hủy Vé
          </button>`;
      html += `
        <article class="booking-card" style="background: #1E1E1E; border: 1px solid #333; border-left: 4px solid ${isCancelled ? '#F44336' : '#4CAF50'}; border-radius: 8px; padding: 20px; margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="display: flex; align-items: center; gap: 10px;">
              <p style="color: var(--primary); font-weight: bold; font-size: 16px;">Mã vé: ${item.bookingCode}</p>
              ${statusBadgeHTML}
            </div>
            <p style="color: #FFF; font-size: 14px; margin-top: 6px;">Hành khách: <strong>${item.customerName}</strong> (${item.customerPhone})</p>
            <p style="color: #9E9E9E; font-size: 13px; margin-top: 2px;">Email: ${item.customerEmail}</p>
          </div>
          <div style="text-align: right;">
            <p style="color: var(--primary); font-weight: bold; font-size: 18px; margin-bottom: 6px;">${formattedTotal} VNĐ</p>
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
// 2. Hàm gửi API Hủy Vé sang Backend Spring Boot (DELETE /api/v1/bookings/{bookingCode})
async function handleCancelBooking(bookingCode) {
  const token = localStorage.getItem('vnr_jwt_token');
  if (!token) return;
  const confirmCancel = confirm(
    `⚠️ Bạn có chắc chắn muốn HỦY đơn đặt vé mã ${bookingCode} không?`,
  );
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
