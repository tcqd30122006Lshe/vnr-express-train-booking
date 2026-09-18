// ==========================================
// VNR EXPRESS WORLD-CLASS RAILWAY PORTAL APP LOGIC (ENHANCED ENGINE)
// ==========================================

const API_BASE = '/api/v1';

let pendingBookingCodeToCancel = null;
let currentBaseSeatPrice = 750000;
let holdTimerInterval = null;
let holdSeconds = 600; // 10-minute hold countdown
let currentSearchResults = [];
let selectedSeatIds = []; // Array tracking selected seat IDs
let maxSeatsAllowed = 1; // Based on passenger count select

document.addEventListener('DOMContentLoaded', () => {
  console.log('⚡ VNR Express Enhanced Railway Portal Initialized.');
  loadStations();
  checkLoginState();
  loadRecentSearches();
  initThemeToggle();
  initJourneyTabs();
  initDateConstraints();
  initNetworkMonitor();

  // Search Form Submit
  const searchForm = document.getElementById('search-train-form');
  if (searchForm) {
    searchForm.addEventListener('submit', handleSearchTrips);
  }

  // Station Code Preview Listeners
  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');
  
  if (originSelect) {
    originSelect.addEventListener('change', () => {
      const codeTag = document.getElementById('origin-code-tag');
      if (codeTag) codeTag.innerText = originSelect.value || 'DI';
    });
  }

  if (destSelect) {
    destSelect.addEventListener('change', () => {
      const codeTag = document.getElementById('dest-code-tag');
      if (codeTag) codeTag.innerText = destSelect.value || 'DEN';
    });
  }

  // Passenger Count Change Listener
  const passengerCountSelect = document.getElementById('passenger-count-select');
  if (passengerCountSelect) {
    passengerCountSelect.addEventListener('change', () => {
      maxSeatsAllowed = parseInt(passengerCountSelect.value || '1');
    });
  }

  // Sort Trips Listener
  const sortSelect = document.getElementById('sort-trips-select');
  if (sortSelect) {
    sortSelect.addEventListener('change', (e) => {
      sortSearchResults(e.target.value);
    });
  }

  // Login Modal Events
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

  // Register Modal Events (Spring Boot & MySQL Integration)
  const registerBtn = document.getElementById('register-btn');
  const registerModal = document.getElementById('register-modal');
  const closeRegisterModalBtn = document.getElementById('close-register-modal-btn');
  const registerForm = document.getElementById('register-form');

  if (registerBtn && registerModal) {
    registerBtn.addEventListener('click', () => {
      registerModal.style.display = 'flex';
    });
  }

  if (closeRegisterModalBtn && registerModal) {
    closeRegisterModalBtn.addEventListener('click', () => {
      registerModal.style.display = 'none';
    });
  }

  if (registerForm) {
    registerForm.addEventListener('submit', handleRegister);
  }

  // Booking Modal Events
  const closeBookingModalBtn = document.getElementById('close-booking-modal-btn');
  const bookingModal = document.getElementById('booking-modal');
  const bookingForm = document.getElementById('booking-form');

  if (closeBookingModalBtn && bookingModal) {
    closeBookingModalBtn.addEventListener('click', () => {
      bookingModal.style.display = 'none';
      stopHoldTimer();
    });
  }

  if (bookingForm) {
    bookingForm.addEventListener('submit', handleCreateBooking);
  }

  // Swap Station button (🔄)
  const swapBtn = document.getElementById('swap-stations-btn');
  if (swapBtn) {
    swapBtn.addEventListener('click', handleSwapStations);
  }

  // PNR Quick Lookup Button & Auto Capitalize Input
  const pnrInputCode = document.getElementById('pnr-input-code');
  if (pnrInputCode) {
    pnrInputCode.addEventListener('input', (e) => {
      e.target.value = e.target.value.toUpperCase().replace(/\s+/g, '');
    });
  }

  const pnrLookupBtn = document.getElementById('btn-do-pnr-lookup');
  if (pnrLookupBtn) {
    pnrLookupBtn.addEventListener('click', handleDoPnrLookup);
  }

  // Centered Navbar Screen Switcher
  const navPills = document.querySelectorAll('.nav-list-middle .nav-link');
  navPills.forEach((pill) => {
    pill.addEventListener('click', (e) => {
      navPills.forEach((p) => p.classList.remove('active'));
      const targetPill = e.target.closest('.nav-link');
      if (targetPill) targetPill.classList.add('active');
    });
  });

  // Custom Cancellation Confirm Modal Events
  const closeCancelModalBtn = document.getElementById('close-cancel-modal-btn');
  const cancelModalCloseAction = document.getElementById('cancel-modal-close-action');
  const confirmCancelBtn = document.getElementById('confirm-cancel-booking-btn');
  const cancelModal = document.getElementById('cancel-confirm-modal');

  if (closeCancelModalBtn && cancelModal) {
    closeCancelModalBtn.addEventListener('click', () => {
      cancelModal.style.display = 'none';
      pendingBookingCodeToCancel = null;
    });
  }

  if (cancelModalCloseAction && cancelModal) {
    cancelModalCloseAction.addEventListener('click', () => {
      cancelModal.style.display = 'none';
      pendingBookingCodeToCancel = null;
    });
  }

  if (confirmCancelBtn) {
    confirmCancelBtn.addEventListener('click', executeCancelBooking);
  }

  // Close Print E-Ticket Modal
  const closePrintModalBtn = document.getElementById('close-print-modal-btn');
  const printModal = document.getElementById('print-ticket-modal');
  if (closePrintModalBtn && printModal) {
    closePrintModalBtn.addEventListener('click', () => {
      printModal.style.display = 'none';
    });
  }

  // Quick Date Chips Listener
  const dateChips = document.querySelectorAll('.chip-btn');
  dateChips.forEach((chip) => {
    chip.addEventListener('click', (e) => {
      dateChips.forEach((c) => c.classList.remove('active'));
      e.target.classList.add('active');

      const dateInput = document.getElementById('depart-date-select');
      const chipType = e.target.getAttribute('data-date');
      const today = new Date();

      if (chipType === 'tomorrow') {
        today.setDate(today.getDate() + 1);
      } else if (chipType === 'weekend') {
        const day = today.getDay();
        const diff = today.getDate() + (6 - day);
        today.setDate(diff);
      }

      if (dateInput) {
        dateInput.value = today.toISOString().split('T')[0];
        dateInput.dispatchEvent(new Event('change'));
      }
    });
  });

  // Quick Seat Selection Shortcuts
  document.getElementById('btn-select-window')?.addEventListener('click', () => selectShortcutSeat('window'));
  document.getElementById('btn-select-aisle')?.addEventListener('click', () => selectShortcutSeat('aisle'));
  document.getElementById('btn-select-random')?.addEventListener('click', () => selectShortcutSeat('random'));

  // Discount Passenger Type Change Listener
  document.getElementById('passenger-type-select')?.addEventListener('change', updatePriceWithDiscount);

  // Payment Method Selection Cards
  const paymentCards = document.querySelectorAll('.payment-method-card');
  paymentCards.forEach((card) => {
    card.addEventListener('click', () => {
      paymentCards.forEach((c) => c.classList.remove('active'));
      card.classList.add('active');
      const radio = card.querySelector('input');
      if (radio) radio.checked = true;
    });
  });

  // Quick Search Filter Pills
  const filterPills = document.querySelectorAll('.filter-pill');
  filterPills.forEach((pill) => {
    pill.addEventListener('click', (e) => {
      filterPills.forEach((p) => p.classList.remove('active'));
      e.target.classList.add('active');
      const filterType = e.target.getAttribute('data-filter');
      applyFilterResults(filterType);
    });
  });

  // Carriage selector
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
// 1. RÀNG BUỘC NGÀY KHỞI HÀNH & NĂM TRỞ VỀ
// ==========================================
function initDateConstraints() {
  const departInput = document.getElementById('depart-date-select');
  const returnInput = document.getElementById('return-date-select');

  const todayStr = new Date().toISOString().split('T')[0];

  if (departInput) {
    departInput.min = todayStr;
    if (!departInput.value) departInput.value = todayStr;

    departInput.addEventListener('change', () => {
      if (returnInput) {
        returnInput.min = departInput.value;
        if (returnInput.value && returnInput.value < departInput.value) {
          returnInput.value = departInput.value;
          showToast('ℹ️ Ngày trở về đã được tự động điều chỉnh bằng Ngày đi.', 'info');
        }
      }
    });
  }

  if (returnInput) {
    returnInput.min = todayStr;
  }
}

// ==========================================
// 2. NETWORK MONITOR (KIỂM TRA MẤT MẠNG INTERNET)
// ==========================================
function initNetworkMonitor() {
  window.addEventListener('offline', () => {
    showToast('⚠️ Mất kết nối Internet! Kiểm tra lại đường truyền của bạn.', 'error');
  });

  window.addEventListener('online', () => {
    showToast('⚡ Kết nối Internet đã được khôi phục thành công!', 'success');
  });
}

// ==========================================
// 3. JOURNEY TYPE TABS (ONE-WAY / ROUND-TRIP / PNR)
// ==========================================
function initJourneyTabs() {
  const tabOneWay = document.getElementById('tab-one-way');
  const tabRoundTrip = document.getElementById('tab-round-trip');
  const tabPnrMode = document.getElementById('tab-pnr-mode');

  const searchCardForm = document.getElementById('search-train-form');
  const pnrCard = document.getElementById('pnr-lookup-card');
  const returnDateGroup = document.getElementById('return-date-group');

  if (tabOneWay) {
    tabOneWay.addEventListener('click', () => {
      [tabOneWay, tabRoundTrip, tabPnrMode].forEach((t) => t && t.classList.remove('active'));
      tabOneWay.classList.add('active');
      if (searchCardForm) searchCardForm.style.display = 'block';
      if (pnrCard) pnrCard.style.display = 'none';
      if (returnDateGroup) returnDateGroup.style.display = 'none';
    });
  }

  if (tabRoundTrip) {
    tabRoundTrip.addEventListener('click', () => {
      [tabOneWay, tabRoundTrip, tabPnrMode].forEach((t) => t && t.classList.remove('active'));
      tabRoundTrip.classList.add('active');
      if (searchCardForm) searchCardForm.style.display = 'block';
      if (pnrCard) pnrCard.style.display = 'none';
      if (returnDateGroup) returnDateGroup.style.display = 'block';

      const departInput = document.getElementById('depart-date-select');
      const returnInput = document.getElementById('return-date-select');
      if (departInput && returnInput) {
        returnInput.min = departInput.value;
      }
      showToast('🔄 Đã chọn Chặng Khứ Hồi! Vui lòng chọn Ngày Trở Về.', 'info');
    });
  }

  if (tabPnrMode) {
    tabPnrMode.addEventListener('click', () => {
      [tabOneWay, tabRoundTrip, tabPnrMode].forEach((t) => t && t.classList.remove('active'));
      tabPnrMode.classList.add('active');
      if (searchCardForm) searchCardForm.style.display = 'none';
      if (pnrCard) pnrCard.style.display = 'block';
    });
  }
}

// ==========================================
// 4. TRA CỨU MÃ VÉ PNR LOOKUP WIDGET
// ==========================================
function handleDoPnrLookup() {
  const codeInput = document.getElementById('pnr-input-code').value.trim();
  const phoneInput = document.getElementById('pnr-input-phone').value.trim();
  const outputElem = document.getElementById('pnr-result-output');

  if (!codeInput) {
    showToast('Vui lòng nhập Mã đơn vé cần tra cứu (VD: BK-93FC2027)', 'error');
    return;
  }

  outputElem.innerHTML = '<p style="color: var(--primary); font-size: 14px; font-weight: 600;">⏳ Đang kết nối tra cứu vé trên hệ thống VNR Express...</p>';

  setTimeout(() => {
    outputElem.innerHTML = `
      <div style="background: var(--surface-dark); padding: 20px; border-radius: var(--radius-md); border-left: 5px solid var(--success-green); text-align: left;">
        <h4 style="color: var(--success-green); font-size: 17px; margin-bottom: 6px;">🎟️ Đơn Vé Điện Tử HỢP LỆ</h4>
        <p style="font-size: 14px; color: var(--white); margin-top: 4px;">Mã Đơn Vé: <strong class="font-mono" style="color: var(--primary);">${codeInput.toUpperCase()}</strong></p>
        <p style="font-size: 13px; color: var(--gray-text); margin-top: 4px;">Trạng thái: <span class="booking-status-badge">ĐÃ XÁC NHẬN (THÀNH CÔNG)</span></p>
        <p style="font-size: 14px; color: var(--white-muted); margin-top: 6px;">Hành trình: <strong>Ga Hà Nội ➔ Ga Đà Nẵng</strong> (Tàu SE1 - Ghế 12)</p>
        <button type="button" class="btn btn-outline" style="margin-top: 14px; font-size: 13px;" onclick="openPrintModal('${codeInput.toUpperCase()}', 'Hành khách VNR', '${phoneInput || '0987654321'}', '750.000')">
          🖨️ In Vé Điện Tử & Xem Mã QR
        </button>
      </div>
    `;
    showToast(`✅ Tìm thấy dữ liệu vé mã ${codeInput.toUpperCase()}!`, 'success');
  }, 600);
}

// ==========================================
// 5. THEME TOGGLE (LIGHT / DARK OBSIDIAN MODE)
// ==========================================
function initThemeToggle() {
  const toggleBtn = document.getElementById('theme-toggle-btn');
  const savedTheme = localStorage.getItem('vnr_theme') || 'dark';

  if (savedTheme === 'light') {
    document.body.classList.add('light-theme');
    if (toggleBtn) toggleBtn.innerText = '☀️';
  }

  if (toggleBtn) {
    toggleBtn.addEventListener('click', () => {
      document.body.classList.toggle('light-theme');
      const isLight = document.body.classList.contains('light-theme');
      toggleBtn.innerText = isLight ? '☀️' : '🌙';
      localStorage.setItem('vnr_theme', isLight ? 'light' : 'dark');
      showToast(isLight ? '☀️ Đã bật Chế độ Sáng' : '🌙 Đã bật Chế độ Tối Obsidian', 'info');
    });
  }
}

// ==========================================
// 6. FLOATING TOAST NOTIFICATION SYSTEM
// ==========================================
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icon = type === 'success' ? '🎉' : type === 'error' ? '❌' : 'ℹ️';
  toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// ==========================================
// 7. ĐỔI CHIỀU GA ĐI & GA ĐẾN (SWAP STATIONS)
// ==========================================
function handleSwapStations() {
  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');

  if (!originSelect || !destSelect) return;

  const tempValue = originSelect.value;
  originSelect.value = destSelect.value;
  destSelect.value = tempValue;

  originSelect.dispatchEvent(new Event('change'));
  destSelect.dispatchEvent(new Event('change'));

  showToast('🔄 Đã đổi chiều Ga Đi ➔ Ga Đến thành công!', 'info');
}

// ==========================================
// 8. LỊCH SỬ TÌM KIẾM GẦN ĐÂY
// ==========================================
function saveRecentSearch(origin, dest) {
  let searches = JSON.parse(localStorage.getItem('vnr_recent_searches') || '[]');
  const itemStr = `${origin} ➔ ${dest}`;
  searches = searches.filter((s) => s !== itemStr);
  searches.unshift(itemStr);
  if (searches.length > 3) searches.pop();
  localStorage.setItem('vnr_recent_searches', JSON.stringify(searches));
  loadRecentSearches();
}

function loadRecentSearches() {
  const container = document.getElementById('recent-searches-wrapper');
  if (!container) return;

  const searches = JSON.parse(localStorage.getItem('vnr_recent_searches') || '[]');
  if (searches.length === 0) {
    container.innerHTML = '';
    return;
  }

  let html = '<span style="color: var(--gray-text); font-weight: 600;">🕒 Gần đây:</span> ';
  searches.forEach((s) => {
    html += `<span class="recent-chip" onclick="applyRecentSearch('${s}')">${s}</span>`;
  });
  container.innerHTML = html;
}

function applyRecentSearch(searchStr) {
  const parts = searchStr.split(' ➔ ');
  if (parts.length === 2) {
    const originSelect = document.getElementById('origin-select');
    const destSelect = document.getElementById('dest-select');
    if (originSelect && destSelect) {
      originSelect.value = parts[0];
      destSelect.value = parts[1];
      originSelect.dispatchEvent(new Event('change'));
      destSelect.dispatchEvent(new Event('change'));
    }
    showToast(`📍 Đã áp dụng tìm kiếm gần đây: ${searchStr}`, 'info');
  }
}

// ==========================================
// 9. NẠP DANH SÁCH GA TÀU
// ==========================================
async function loadStations() {
  try {
    const response = await fetch(`${API_BASE}/stations`);
    const data = await response.json();
    const stationList = data.result || data || [];

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
    console.error('❌ Lỗi khi nạp danh sách Ga Tàu từ Spring Boot:', error);
  }
}

// ==========================================
// 10. TÌM KIẾM CHUYẾN TÀU & LỌC / SẮP XẾP
// ==========================================
async function handleSearchTrips(event) {
  event.preventDefault();

  const originCode = document.getElementById('origin-select').value;
  const destCode = document.getElementById('dest-select').value;

  if (!originCode || !destCode) {
    showToast('Vui lòng chọn đầy đủ Ga đi và Ga đến', 'error');
    return;
  }

  if (originCode === destCode) {
    showToast('Ga đi và Ga đến không được trùng nhau', 'error');
    return;
  }

  saveRecentSearch(originCode, destCode);
  showToast(`🔎 Đang tìm kiếm chuyến tàu từ ${originCode} đến ${destCode}...`, 'info');

  try {
    const response = await fetch(`${API_BASE}/trips`);
    const data = await response.json();
    currentSearchResults = data.result || [];

    const filterWrapper = document.getElementById('filter-pills-wrapper');
    if (filterWrapper) filterWrapper.style.display = 'flex';

    const sortType = document.getElementById('sort-trips-select')?.value || 'PRICE_LOW';
    sortSearchResults(sortType);
  } catch (error) {
    console.error('Lỗi khi tìm kiếm chuyến tàu:', error);
    showToast('❌ Lỗi kết nối máy chủ khi tìm chuyến tàu', 'error');
  }
}

function sortSearchResults(sortType) {
  if (!currentSearchResults || currentSearchResults.length === 0) return;

  let trips = [...currentSearchResults];
  if (sortType === 'PRICE_LOW') {
    trips.sort((a, b) => (a.basePrice || 750000) - (b.basePrice || 750000));
  } else if (sortType === 'TIME_EARLIEST') {
    trips.sort((a, b) => (a.departureTime || '').localeCompare(b.departureTime || ''));
  } else if (sortType === 'DURATION_FASTEST') {
    trips.sort((a, b) => (a.durationMinutes || 870) - (b.durationMinutes || 870));
  }

  renderSearchResults(trips);
}

function applyFilterResults(filterType) {
  if (!currentSearchResults || currentSearchResults.length === 0) return;

  let filtered = [...currentSearchResults];
  if (filterType === 'se') {
    filtered = filtered.filter((t) => (t.train ? t.train.code.includes('SE') : true));
  } else if (filterType === 'morning') {
    filtered = filtered.filter((t) => {
      const hour = t.departureTime ? parseInt(t.departureTime.substring(11, 13)) : 8;
      return hour >= 5 && hour < 12;
    });
  } else if (filterType === 'evening') {
    filtered = filtered.filter((t) => {
      const hour = t.departureTime ? parseInt(t.departureTime.substring(11, 13)) : 20;
      return hour >= 18 && hour <= 23;
    });
  }

  renderSearchResults(filtered);
}

// ==========================================
// 11. RENDER CHUYẾN TÀU ĐỘNG VỚI JOURNEY TIMELINE VISUALIZER
// ==========================================
function renderSearchResults(trips) {
  const container = document.getElementById('search-results-container');
  if (!container) return;

  if (!trips || trips.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <span class="empty-icon">🚫</span>
        <p>Không tìm thấy chuyến tàu nào phù hợp cho bộ lọc này.</p>
      </div>
    `;
    return;
  }

  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');
  const originName = originSelect && originSelect.selectedIndex > 0 ? originSelect.options[originSelect.selectedIndex].text.split(' (')[0] : 'Ga đi';
  const destName = destSelect && destSelect.selectedIndex > 0 ? destSelect.options[destSelect.selectedIndex].text.split(' (')[0] : 'Ga đến';

  let html = `<h3 class="results-title">🎉 Danh Sách Chuyến Tàu Phù Hợp (${trips.length})</h3>`;

  trips.forEach((trip) => {
    const formattedPrice = trip.basePrice
      ? Number(trip.basePrice).toLocaleString('vi-VN')
      : '750.000';
    const trainCode = trip.train ? trip.train.code : trip.trainCode || 'SE1';
    const trainName = trip.train
      ? trip.train.name
      : trip.routeName || 'Tàu Tốc Hành Bắc Nam';

    const depTime = trip.departureTime ? trip.departureTime.substring(11, 16) : '08:00';
    const arrTime = trip.arrivalTime ? trip.arrivalTime.substring(11, 16) : '20:30';

    html += `
      <article class="trip-card">
        <div class="trip-card-content">
          <div style="flex: 1;">
            <h4 class="trip-train-title">
              🚆 ${trainCode} - ${trainName}
              <span class="train-tag">TỐC HÀNH</span>
            </h4>
            
            <!-- TIMELINE LINE VISUALIZER -->
            <div class="journey-timeline-bar">
              <div class="time-box">
                <span class="time">${depTime}</span>
                <div class="station">${originName}</div>
              </div>
              <div class="timeline-line-wrapper">
                <span class="duration-text">~14g 30m</span>
                <div class="timeline-line">
                  <span class="train-badge-pill">🚆 ${trainCode}</span>
                </div>
              </div>
              <div class="time-box">
                <span class="time">${arrTime}</span>
                <div class="station">${destName}</div>
              </div>
            </div>

            <div class="seat-class-tags">
              <span class="class-tag">💺 Ngồi mềm điều hòa</span>
              <span class="class-tag">🛏️ Giường nằm K4</span>
              <span class="class-tag">🛏️ Giường nằm K6</span>
            </div>
          </div>

          <div class="trip-price-wrapper" style="margin-left: 24px;">
            <p class="trip-price-text">${formattedPrice} VNĐ</p>
            <button type="button" class="btn btn-primary" onclick="openBookingModal(${trip.id}, ${trip.basePrice || 750000})">
              🎟️ Chọn Chuyến này
            </button>
          </div>
        </div>
      </article>
    `;
  });

  container.innerHTML = html;
}

// ==========================================
// 12. HOLD TIMER COUNTDOWN
// ==========================================
function startHoldTimer() {
  stopHoldTimer();
  holdSeconds = 600;
  updateHoldTimerUI();

  holdTimerInterval = setInterval(() => {
    holdSeconds--;
    if (holdSeconds <= 0) {
      stopHoldTimer();
      showToast('⚠️ Hết thời gian giữ ghế! Vui lòng chọn lại.', 'error');
      document.getElementById('booking-modal').style.display = 'none';
    } else {
      updateHoldTimerUI();
    }
  }, 1000);
}

function stopHoldTimer() {
  if (holdTimerInterval) {
    clearInterval(holdTimerInterval);
    holdTimerInterval = null;
  }
}

function updateHoldTimerUI() {
  const textElem = document.getElementById('hold-timer-text');
  const fillElem = document.getElementById('hold-timer-fill');

  const mins = Math.floor(holdSeconds / 60);
  const secs = holdSeconds % 60;
  const timeStr = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;

  if (textElem) textElem.innerText = timeStr;
  if (fillElem) {
    const percent = (holdSeconds / 600) * 100;
    fillElem.style.width = `${percent}%`;
  }
}

// ==========================================
// 13. XỬ LÝ ĐĂNG NHẬP & REGISTER (KẾT NỐI SPRING BOOT & MYSQL DB `users`)
// ==========================================
async function handleRegister(event) {
  event.preventDefault();

  const username = document.getElementById('register-username').value.trim();
  const fullname = document.getElementById('register-fullname').value.trim();
  const email = document.getElementById('register-email').value.trim();
  const phone = document.getElementById('register-phone')?.value.trim() || '0987654321';
  const password = document.getElementById('register-password').value;
  const confirmPassword = document.getElementById('register-confirm-password').value;

  if (password !== confirmPassword) {
    showToast('❌ Mật khẩu xác nhận không trùng khớp!', 'error');
    return;
  }

  const registerData = {
    username: username,
    password: password,
    fullName: fullname,
    email: email,
    phone: phone
  };

  try {
    showToast('🔒 Đang gửi dữ liệu Đăng ký sang Spring Boot & MySQL DB (users)...', 'info');

    const response = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(registerData)
    });

    const data = await response.json();

    if (response.ok && data.result) {
      showToast(`🎉 ĐĂNG KÝ THÀNH CÔNG! Đã lưu user '${username}' vào MySQL database 've_tau' (bảng users).`, 'success');
      
      localStorage.setItem('vnr_user_profile', JSON.stringify({
        username: username,
        fullName: fullname,
        email: email,
        phone: phone
      }));

      document.getElementById('register-modal').style.display = 'none';
      document.getElementById('register-form').reset();
      
      document.getElementById('login-username').value = username;
      document.getElementById('login-password').value = password;
      document.getElementById('login-modal').style.display = 'flex';
    } else {
      showToast('❌ Đăng ký thất bại: ' + (data.message || 'Tài khoản đã tồn tại!'), 'error');
    }
  } catch (error) {
    console.error('❌ Lỗi khi đăng ký:', error);
    showToast('❌ Lỗi kết nối máy chủ Spring Boot!', 'error');
  }
}

async function handleLogin(e) {
  e.preventDefault();

  const usernameInput = document.getElementById('login-username').value.trim();
  const passwordInput = document.getElementById('login-password').value;

  try {
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

      localStorage.setItem('vnr_user_profile', JSON.stringify({
        username: usernameInput,
        fullName: usernameInput === 'admin' ? 'Quản Trị Viên VNR' : usernameInput,
        email: `${usernameInput}@gmail.com`,
        phone: '0987654321'
      }));

      showToast(`🎉 Đăng nhập thành công! Chào mừng ${usernameInput}`, 'success');
      document.getElementById('login-modal').style.display = 'none';
      checkLoginState();
    } else {
      showToast('❌ Đăng nhập thất bại: Tài khoản hoặc mật khẩu không chính xác!', 'error');
    }
  } catch (error) {
    console.error('❌ Lỗi khi đăng nhập:', error);
    showToast('❌ Lỗi kết nối server!', 'error');
  }
}

function openLoginModal() {
  const modal = document.getElementById('login-modal');
  if (modal) modal.style.display = 'flex';
}

function closeLoginModal() {
  const modal = document.getElementById('login-modal');
  if (modal) modal.style.display = 'none';
}

function openRegisterModal() {
  const modal = document.getElementById('register-modal');
  if (modal) modal.style.display = 'flex';
}

function closeRegisterModal() {
  const modal = document.getElementById('register-modal');
  if (modal) modal.style.display = 'none';
}

function checkLoginState() {
  const token = localStorage.getItem('vnr_jwt_token');
  const username = localStorage.getItem('vnr_username') || 'User';

  const authButtonContainer = document.querySelector('.auth-buttons');
  if (!authButtonContainer) return;

  const themeBtnHTML = `<button type="button" id="theme-toggle-btn" class="btn-theme-toggle" title="Đổi Chế độ Sáng/Tối">🌙</button>`;

  if (token) {
    authButtonContainer.innerHTML = `
      ${themeBtnHTML}
      <span class="user-badge">👤 ${username}</span>
      <button type="button" class="btn btn-outline" onclick="handleLogout()">Đăng Xuất</button>
    `;
    initThemeToggle();
    if (typeof loadMyBookings === 'function') {
      loadMyBookings();
    }
  } else {
    authButtonContainer.innerHTML = `
      ${themeBtnHTML}
      <button type="button" id="login-btn" class="btn btn-outline" onclick="openLoginModal()">Đăng Nhập</button>
      <button type="button" id="register-btn" class="btn btn-primary" onclick="openRegisterModal()">Đăng Ký</button>
    `;
    initThemeToggle();
  }
}

function handleLogout() {
  localStorage.removeItem('vnr_jwt_token');
  localStorage.removeItem('vnr_username');
  localStorage.removeItem('vnr_user_profile');
  showToast('Đăng xuất thành công!', 'info');
  setTimeout(() => location.reload(), 800);
}

// ==========================================
// 14. MỞ MODAL ĐẶT VÉ VỚI TỰ ĐỘNG ĐIỀN THÔNG TIN USER & MULTI-SEAT SELECTION
// ==========================================
function openBookingModal(tripId, basePrice = 750000) {
  const token = localStorage.getItem('vnr_jwt_token');
  if (!token) {
    showToast('🔒 Bạn cần Đăng nhập trước khi thực hiện Đặt vé!', 'error');
    document.getElementById('login-modal').style.display = 'flex';
    return;
  }

  currentBaseSeatPrice = basePrice;
  selectedSeatIds = [];

  const passengerCountSelect = document.getElementById('passenger-count-select');
  maxSeatsAllowed = parseInt(passengerCountSelect ? passengerCountSelect.value : '1');

  document.getElementById('booking-trip-id').value = tripId;
  document.getElementById('selected-seat-id').value = '';
  document.getElementById('selected-seat-display').innerText = 'Chưa chọn';
  document.getElementById('total-price-display').innerText = '0 VNĐ';

  const profileStr = localStorage.getItem('vnr_user_profile');
  if (profileStr) {
    try {
      const profile = JSON.parse(profileStr);
      const nameInput = document.getElementById('customer-name');
      const phoneInput = document.getElementById('customer-phone');
      const emailInput = document.getElementById('customer-email');

      if (nameInput && !nameInput.value) nameInput.value = profile.fullName || profile.username || '';
      if (phoneInput && !phoneInput.value) phoneInput.value = profile.phone || '0987654321';
      if (emailInput && !emailInput.value) emailInput.value = profile.email || `${profile.username}@gmail.com`;
    } catch (e) {
      console.warn('Cannot parse profile:', e);
    }
  }

  document.getElementById('booking-modal').style.display = 'flex';
  startHoldTimer();
  loadCarriageSeatMap(tripId, 1);
}

// Render Sơ đồ Ghế Tàu 4 Hàng (2 hàng trên - Lối đi - 2 hàng dưới)
function loadCarriageSeatMap(tripId, carriageId) {
  const seatContainer = document.getElementById('seat-grid-container');
  if (!seatContainer) return;

  selectedSeatIds = [];

  let html = `
    <div class="seat-rows-wrapper">
      <div class="seat-row" id="row-1"></div>
      <div class="seat-row" id="row-2"></div>
      <div class="aisle-divider">=== HÀNH LANG (LỐI ĐI) ===</div>
      <div class="seat-row" id="row-3"></div>
      <div class="seat-row" id="row-4"></div>
    </div>
  `;
  seatContainer.innerHTML = html;

  const row1 = document.getElementById('row-1');
  const row2 = document.getElementById('row-2');
  const row3 = document.getElementById('row-3');
  const row4 = document.getElementById('row-4');

  for (let i = 1; i <= 32; i++) {
    const isBooked = (i % 7 === 0 || i % 11 === 0);
    const statusClass = isBooked ? 'booked' : 'available';
    const seatBtn = `<button type="button" class="seat-btn ${statusClass}" data-seat-id="${i}" data-seat-num="${i}" ${isBooked ? 'disabled' : ''} onclick="selectSeat(this, ${i})">${i}</button>`;

    if (i <= 8) row1.innerHTML += seatBtn;
    else if (i <= 16) row2.innerHTML += seatBtn;
    else if (i <= 24) row3.innerHTML += seatBtn;
    else row4.innerHTML += seatBtn;
  }
}

// LOGIC CHỌN NHIỀU GHẾ (MULTI-SEAT SELECTION)
function selectSeat(buttonElem, seatId) {
  if (buttonElem.classList.contains('booked')) return;

  const index = selectedSeatIds.indexOf(seatId);

  if (index > -1) {
    selectedSeatIds.splice(index, 1);
    buttonElem.classList.remove('selected');
    buttonElem.classList.add('available');
  } else {
    if (selectedSeatIds.length >= maxSeatsAllowed) {
      showToast(`⚠️ Bạn đã chọn đủ ${maxSeatsAllowed} ghế cho ${maxSeatsAllowed} hành khách!`, 'error');
      return;
    }
    selectedSeatIds.push(seatId);
    buttonElem.classList.remove('available');
    buttonElem.classList.add('selected');
  }

  document.getElementById('selected-seat-id').value = selectedSeatIds.join(',');

  const displayElem = document.getElementById('selected-seat-display');
  if (selectedSeatIds.length === 0) {
    displayElem.innerText = 'Chưa chọn';
  } else {
    displayElem.innerText = `Ghế số ${selectedSeatIds.join(', ')} (${selectedSeatIds.length}/${maxSeatsAllowed} ghế)`;
  }

  updatePriceWithDiscount();
}

// ==========================================
// 15. PHÍM TẮT CHỌN GHẾ NHANH
// ==========================================
function selectShortcutSeat(type) {
  const availableSeats = document.querySelectorAll('.seat-btn.available');
  if (availableSeats.length === 0) {
    showToast('⚠️ Không còn ghế trống trong toa này!', 'error');
    return;
  }

  document.querySelectorAll('.seat-btn.selected').forEach((btn) => {
    btn.classList.remove('selected');
    btn.classList.add('available');
  });
  selectedSeatIds = [];

  let pool = Array.from(availableSeats);
  if (type === 'window') {
    pool = pool.filter((s) => {
      const num = parseInt(s.getAttribute('data-seat-num'));
      return (num >= 1 && num <= 8) || (num >= 25 && num <= 32);
    });
  } else if (type === 'aisle') {
    pool = pool.filter((s) => {
      const num = parseInt(s.getAttribute('data-seat-num'));
      return (num >= 9 && num <= 24);
    });
  }

  if (pool.length === 0) pool = Array.from(availableSeats);

  for (let i = 0; i < maxSeatsAllowed && i < pool.length; i++) {
    const seatBtn = pool[i];
    const seatNum = parseInt(seatBtn.getAttribute('data-seat-num'));
    selectedSeatIds.push(seatNum);
    seatBtn.classList.remove('available');
    seatBtn.classList.add('selected');
  }

  document.getElementById('selected-seat-id').value = selectedSeatIds.join(',');
  document.getElementById('selected-seat-display').innerText = `Ghế số ${selectedSeatIds.join(', ')} (${selectedSeatIds.length}/${maxSeatsAllowed} ghế)`;
  updatePriceWithDiscount();

  showToast(`🎯 Đã chọn nhanh Ghế số ${selectedSeatIds.join(', ')}`, 'info');
}

// Update calculated price with passenger type discount
function updatePriceWithDiscount() {
  const discountSelect = document.getElementById('passenger-type-select');
  const discountRate = parseFloat(discountSelect ? discountSelect.value : '1');
  const seatCount = selectedSeatIds.length;

  if (seatCount === 0) {
    document.getElementById('total-price-display').innerText = '0 VNĐ';
    const detailElem = document.getElementById('discount-detail-text');
    if (detailElem) detailElem.innerText = 'Áp dụng mức giá chuẩn';
    return;
  }

  const finalPrice = currentBaseSeatPrice * seatCount * discountRate;
  const formatted = Number(finalPrice).toLocaleString('vi-VN');
  document.getElementById('total-price-display').innerText = `${formatted} VNĐ`;

  const detailElem = document.getElementById('discount-detail-text');
  if (detailElem) {
    if (discountRate === 0.9) detailElem.innerText = 'Đã áp dụng ưu đãi Sinh viên (-10%)';
    else if (discountRate === 0.85) detailElem.innerText = 'Đã áp dụng ưu đãi Người cao tuổi (-15%)';
    else if (discountRate === 0.5) detailElem.innerText = 'Đã áp dụng ưu đãi Trẻ em (-50%)';
    else detailElem.innerText = 'Áp dụng mức giá chuẩn (100%)';
  }
}

// ==========================================
// 16. TẠO ĐƠN ĐẶT VÉ
// ==========================================
async function handleCreateBooking(event) {
  event.preventDefault();

  const token = localStorage.getItem('vnr_jwt_token');
  if (!token) {
    showToast('🔒 Phiên đăng nhập đã hết hạn! Vui lòng đăng nhập lại.', 'error');
    return;
  }

  const selectedSeatId = document.getElementById('selected-seat-id').value;
  if (!selectedSeatId) {
    showToast('⚠️ Vui lòng chọn 1 ghế tàu trên sơ đồ trước khi bấm Xác Nhận Đặt Vé!', 'error');
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
      stopHoldTimer();
      showToast(`🎉 ĐẶT VÉ THÀNH CÔNG! Mã đơn vé: ${data.result.bookingCode || 'VNR-OK'}`, 'success');
      document.getElementById('booking-modal').style.display = 'none';
      document.getElementById('booking-form').reset();
      if (typeof loadMyBookings === 'function') {
        loadMyBookings();
      }
    } else {
      showToast('❌ Đặt vé thất bại: ' + (data.message || 'Dữ liệu không hợp lệ!'), 'error');
    }
  } catch (error) {
    console.error('❌ Lỗi khi đặt vé:', error);
    showToast('❌ Lỗi kết nối máy chủ!', 'error');
  }
}

// ==========================================
// 17. NẠP DANH SÁCH VÉ CỦA TÔI
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
        : '750.000';

      const isCancelled = item.status === 'CANCELLED';
      const statusClass = isCancelled ? 'booking-card cancelled' : 'booking-card';
      const badgeClass = isCancelled ? 'booking-status-badge cancelled' : 'booking-status-badge';
      const statusText = isCancelled ? 'ĐÃ HỦY' : 'THÀNH CÔNG';

      const cancelBtnHTML = isCancelled
        ? ''
        : `<button type="button" class="btn btn-outline btn-danger" onclick="openCancelModal('${item.bookingCode}')">
            🗑️ Hủy Vé
          </button>`;

      const printBtnHTML = isCancelled
        ? ''
        : `<button type="button" class="btn btn-outline" onclick="openPrintModal('${item.bookingCode}', '${item.customerName}', '${item.customerPhone}', '${formattedTotal}')">
            🖨️ In Vé QR
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
            <div style="display: flex; gap: 8px;">
              ${printBtnHTML}
              ${cancelBtnHTML}
            </div>
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
// 18. IN VÉ ĐIỆN TỬ MODAL PREVIEW
// ==========================================
function openPrintModal(code, name, phone, price) {
  document.getElementById('print-ticket-code').innerText = code;
  document.getElementById('print-customer-name').innerText = name;
  document.getElementById('print-customer-phone').innerText = phone;
  document.getElementById('print-total-price').innerText = `${price} VNĐ`;

  const qrImg = document.getElementById('print-qr-image');
  if (qrImg) {
    qrImg.src = `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=VNR_CHECKIN_TICKET_${code}_PASSENGER_${encodeURIComponent(name)}`;
  }

  const modal = document.getElementById('print-ticket-modal');
  if (modal) modal.style.display = 'flex';
}

// ==========================================
// 19. HỦY VÉ CUSTOM MODAL
// ==========================================
function openCancelModal(bookingCode) {
  pendingBookingCodeToCancel = bookingCode;
  const textElem = document.getElementById('cancel-booking-code-text');
  if (textElem) {
    textElem.innerText = bookingCode;
  }
  const modal = document.getElementById('cancel-confirm-modal');
  if (modal) {
    modal.style.display = 'flex';
  }
}

async function executeCancelBooking() {
  if (!pendingBookingCodeToCancel) return;

  const token = localStorage.getItem('vnr_jwt_token');
  if (!token) return;

  const bookingCode = pendingBookingCodeToCancel;
  const modal = document.getElementById('cancel-confirm-modal');

  try {
    const response = await fetch(`${API_BASE}/bookings/${bookingCode}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (modal) {
      modal.style.display = 'none';
    }

    if (response.ok) {
      showToast(`✅ Đã hủy đơn đặt vé ${bookingCode} thành công!`, 'success');
      pendingBookingCodeToCancel = null;
      loadMyBookings();
    } else {
      showToast('❌ Hủy vé thất bại!', 'error');
    }
  } catch (error) {
    console.error('❌ Lỗi khi hủy vé:', error);
    showToast('❌ Lỗi kết nối máy chủ!', 'error');
  }
}
