// ==========================================
// VNR EXPRESS WORLD-CLASS RAILWAY PORTAL APP LOGIC (ENHANCED ENGINE)
// ==========================================

const API_BASE = '/api/v1';

// ==========================================
// XSS DEFENSE HELPER: ESCAPE HTML SPECIAL CHARACTERS
// ==========================================
function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function isTokenExpired(token) {
  if (!token) return true;
  try {
    const parts = token.split('.');
    if (parts.length < 2) return true;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const payload = JSON.parse(jsonPayload);
    if (payload.exp && Date.now() >= payload.exp * 1000) {
      return true;
    }
    return false;
  } catch (e) {
    return true;
  }
}

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

  // Kiểm tra callback thanh toán VNPAY từ URL
  const urlParams = new URLSearchParams(window.location.search);
  const paymentStatus = urlParams.get('paymentStatus') || urlParams.get('vnpayStatus');
  const returnBookingCode = urlParams.get('bookingCode');
  if (paymentStatus === 'success' && returnBookingCode) {
    showToast(`🎉 THANH TOÁN VNPAY THÀNH CÔNG! Đơn vé: ${returnBookingCode}`, 'success');
    window.history.replaceState({}, document.title, window.location.pathname);
    setTimeout(() => {
      if (typeof openPrintModal === 'function') {
        openPrintModal(returnBookingCode, 'Hành khách VNR', '', '');
      }
    }, 600);
  } else if (paymentStatus === 'failed') {
    showToast(`❌ Thanh toán VNPAY không thành công hoặc giao dịch đã bị hủy.`, 'error');
    window.history.replaceState({}, document.title, window.location.pathname);
  }

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
      const selectedOption = originSelect.options[originSelect.selectedIndex];
      if (codeTag) codeTag.innerText = selectedOption && selectedOption.value ? selectedOption.getAttribute('data-code') : 'DI';
    });
  }

  if (destSelect) {
    destSelect.addEventListener('change', () => {
      const codeTag = document.getElementById('dest-code-tag');
      const selectedOption = destSelect.options[destSelect.selectedIndex];
      if (codeTag) codeTag.innerText = selectedOption && selectedOption.value ? selectedOption.getAttribute('data-code') : 'DEN';
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
      if (targetPill) {
        targetPill.classList.add('active');
        const screen = targetPill.getAttribute('data-screen');
        if (screen === 'pnr') {
          const tabPnr = document.getElementById('tab-pnr-mode');
          if (tabPnr) tabPnr.click();
          const pnrCard = document.getElementById('pnr-lookup-card');
          if (pnrCard) {
            pnrCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
            setTimeout(() => {
              const codeInput = document.getElementById('pnr-input-code');
              if (codeInput) codeInput.focus();
            }, 300);
          }
        } else if (screen === 'home') {
          const tabOneWay = document.getElementById('tab-one-way');
          if (tabOneWay) tabOneWay.click();
          const hero = document.getElementById('hero-section');
          if (hero) hero.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }
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
      if (searchCardForm) {
        searchCardForm.style.display = 'block';
        searchCardForm.classList.remove('display-none');
      }
      if (pnrCard) {
        pnrCard.style.display = 'none';
        pnrCard.classList.add('display-none');
      }
      if (returnDateGroup) returnDateGroup.style.display = 'none';
    });
  }

  if (tabRoundTrip) {
    tabRoundTrip.addEventListener('click', () => {
      [tabOneWay, tabRoundTrip, tabPnrMode].forEach((t) => t && t.classList.remove('active'));
      tabRoundTrip.classList.add('active');
      if (searchCardForm) {
        searchCardForm.style.display = 'block';
        searchCardForm.classList.remove('display-none');
      }
      if (pnrCard) {
        pnrCard.style.display = 'none';
        pnrCard.classList.add('display-none');
      }
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
      if (searchCardForm) {
        searchCardForm.style.display = 'none';
        searchCardForm.classList.add('display-none');
      }
      if (pnrCard) {
        pnrCard.style.display = 'block';
        pnrCard.classList.remove('display-none');
      }
      setTimeout(() => {
        const codeInput = document.getElementById('pnr-input-code');
        if (codeInput) codeInput.focus();
      }, 100);
    });
  }
}

// ==========================================
// 4. TRA CỨU MÃ VÉ PNR LOOKUP WIDGET (REAL API)
// ==========================================
async function handleDoPnrLookup() {
  const codeInput = document.getElementById('pnr-input-code');
  const phoneInput = document.getElementById('pnr-input-phone');
  const outputElem = document.getElementById('pnr-result-output');

  const rawCode = (codeInput ? codeInput.value : '').trim().toUpperCase().replace(/\s+/g, '');
  const rawPhone = (phoneInput ? phoneInput.value : '').trim();

  if (!rawCode) {
    showToast('Vui lòng nhập Mã đơn vé cần tra cứu (VD: BK-93FC2027)', 'error');
    if (codeInput) codeInput.focus();
    return;
  }

  if (!outputElem) return;

  outputElem.innerHTML = `
    <div style="padding: 16px; text-align: center; color: var(--primary);">
      <p style="font-size: 14px; font-weight: 600;">⏳ Đang kết nối tra cứu vé trên hệ thống VNR Express...</p>
    </div>
  `;

  try {
    const token = localStorage.getItem('vnr_jwt_token');
    const headers = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE}/bookings/${encodeURIComponent(rawCode)}`, {
      method: 'GET',
      headers: headers
    });

    const data = await res.json();

    if (!res.ok || !data.result) {
      const errMsg = (data && data.message) ? data.message : 'Không tìm thấy đơn vé với mã đã cung cấp.';
      outputElem.innerHTML = `
        <div style="background: rgba(239, 68, 68, 0.12); border: 1px solid var(--danger-red); padding: 18px; border-radius: var(--radius-md); text-align: left; margin-top: 15px;">
          <h4 style="color: var(--danger-red); font-size: 16px; margin-bottom: 6px;">❌ Không Tìm Thấy Đơn Vé</h4>
          <p style="font-size: 14px; color: var(--white); margin: 4px 0;">Mã đơn: <strong class="font-mono text-primary">${escapeHtml(rawCode)}</strong></p>
          <p style="font-size: 13px; color: var(--gray-text); margin-top: 6px;">${escapeHtml(errMsg)}. Vui lòng kiểm tra lại mã đơn vé hoặc liên hệ tổng đài 1900 1520 để được hỗ trợ.</p>
        </div>
      `;
      showToast('❌ Không tìm thấy thông tin đơn vé!', 'error');
      return;
    }

    const booking = data.result;

    // Kiểm tra số điện thoại (nếu khách có nhập)
    if (rawPhone && booking.customerPhone && !booking.customerPhone.includes(rawPhone) && !rawPhone.includes(booking.customerPhone)) {
      outputElem.innerHTML = `
        <div style="background: rgba(245, 158, 11, 0.12); border: 1px solid var(--accent-gold); padding: 18px; border-radius: var(--radius-md); text-align: left; margin-top: 15px;">
          <h4 style="color: var(--accent-gold); font-size: 16px; margin-bottom: 6px;">⚠️ Số Điện Thoại Không Khớp</h4>
          <p style="font-size: 13px; color: var(--white-muted);">Tìm thấy mã đơn vé <strong>${escapeHtml(rawCode)}</strong> nhưng số điện thoại bạn nhập không khớp với số điện thoại đặt vé. Vui lòng kiểm tra lại!</p>
        </div>
      `;
      showToast('⚠️ Số điện thoại không khớp với hồ sơ đơn vé!', 'warning');
      return;
    }

    const isCancelled = booking.status === 'CANCELLED';
    const statusBadge = isCancelled
      ? `<span class="booking-status-badge cancelled">ĐÃ HỦY</span>`
      : `<span class="booking-status-badge" style="background: rgba(16, 185, 129, 0.2); color: var(--success-green); border: 1px solid var(--success-green);">ĐÃ XÁC NHẬN (THÀNH CÔNG)</span>`;

    const formattedTotal = booking.totalAmount
      ? Number(booking.totalAmount).toLocaleString('vi-VN')
      : '0';

    const trainTitle = `${booking.tripCode || ''} ${booking.trainName || ''}`.trim() || 'Tàu Thống Nhất';
    const routeTitle = (booking.startStationName && booking.endStationName)
      ? `${booking.startStationName} ➔ ${booking.endStationName}`
      : 'Tuyến Bắc Nam';

    let ticketsHTML = '';
    if (booking.tickets && booking.tickets.length > 0) {
      ticketsHTML = `
        <div style="margin-top: 12px; padding-top: 10px; border-top: 1px dashed var(--border-color);">
          <p style="font-size: 13px; color: var(--gray-text); margin-bottom: 6px;">Danh sách ghế / hành khách:</p>
          <ul style="list-style: none; padding-left: 0; margin: 0; display: flex; flex-direction: column; gap: 6px;">
            ${booking.tickets.map(t => `
              <li style="font-size: 13px; color: var(--white); background: rgba(255,255,255,0.05); padding: 8px 12px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center;">
                <span>👤 <strong>${escapeHtml(t.passengerName || booking.customerName || 'Hành khách')}</strong> (Toa ${t.carriageNumber || 1} - Ghế ${t.seatNumber})</span>
                <span style="color: var(--accent-gold); font-weight: 700;">${Number(t.price || 0).toLocaleString('vi-VN')} đ</span>
              </li>
            `).join('')}
          </ul>
        </div>
      `;
    }

    const safeCode = escapeHtml(booking.bookingCode);
    const safeCustName = escapeHtml(booking.customerName || 'Hành khách');
    const safeCustPhone = escapeHtml(booking.customerPhone || '');

    outputElem.innerHTML = `
      <div style="background: var(--surface-dark); padding: 22px; border-radius: var(--radius-md); border-left: 5px solid ${isCancelled ? 'var(--danger-red)' : 'var(--success-green)'}; text-align: left; margin-top: 15px; box-shadow: var(--shadow-md);">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; margin-bottom: 12px;">
          <h4 style="color: ${isCancelled ? 'var(--danger-red)' : 'var(--success-green)'}; font-size: 17px; margin: 0;">
            ${isCancelled ? '❌ Đơn Vé Đã Bị Hủy' : '🎟️ Đơn Vé Điện Tử HỢP LỆ'}
          </h4>
          ${statusBadge}
        </div>
        <p style="font-size: 14px; color: var(--white); margin: 5px 0;">Mã Đơn Vé: <strong class="font-mono" style="color: var(--primary); font-size: 16px;">${safeCode}</strong></p>
        <p style="font-size: 14px; color: var(--white-muted); margin: 5px 0;">Hành khách: <strong>${safeCustName}</strong> ${safeCustPhone ? `(${safeCustPhone})` : ''}</p>
        <p style="font-size: 14px; color: var(--white-muted); margin: 5px 0;">Hành trình: <strong style="color: var(--white);">${escapeHtml(routeTitle)}</strong> (${escapeHtml(trainTitle)})</p>
        <p style="font-size: 14px; color: var(--white-muted); margin: 5px 0;">Tổng tiền: <strong style="color: var(--accent-gold); font-size: 16px;">${formattedTotal} VNĐ</strong></p>
        ${ticketsHTML}
        ${!isCancelled ? `
          <button type="button" class="btn btn-outline" style="margin-top: 16px; font-size: 13px;" onclick="openPrintModal('${safeCode}', '${safeCustName}', '${safeCustPhone}', '${formattedTotal}')">
            🖨️ In Vé Điện Tử & Xem Mã QR
          </button>
        ` : ''}
      </div>
    `;

    showToast(`✅ Đã tải thành công vé ${safeCode}!`, 'success');
  } catch (err) {
    console.error('Lỗi tra cứu vé:', err);
    outputElem.innerHTML = `
      <div style="background: rgba(239, 68, 68, 0.12); border: 1px solid var(--danger-red); padding: 16px; border-radius: var(--radius-md); text-align: left; margin-top: 15px;">
        <p style="color: var(--danger-red); font-size: 14px; margin: 0;">⚠️ Không thể kết nối tới máy chủ tra cứu vé. Vui lòng kiểm tra lại đường truyền!</p>
      </div>
    `;
    showToast('⚠️ Lỗi kết nối máy chủ tra cứu vé!', 'error');
  }
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
  toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;

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
    const safeS = escapeHtml(s);
    html += `<span class="recent-chip" onclick="applyRecentSearch('${safeS}')">${safeS}</span>`;
  });
  container.innerHTML = html;
}

function applyRecentSearch(searchStr) {
  const parts = searchStr.split(' ➔ ');
  if (parts.length === 2) {
    const originSelect = document.getElementById('origin-select');
    const destSelect = document.getElementById('dest-select');
    if (originSelect && destSelect) {
      const setSelectByCode = (select, code) => {
        for (let i = 0; i < select.options.length; i++) {
          if (select.options[i].getAttribute('data-code') === code) {
            select.selectedIndex = i;
            break;
          }
        }
      };
      setSelectByCode(originSelect, parts[0]);
      setSelectByCode(destSelect, parts[1]);
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
      const optionHTML = `<option value="${station.id}" data-code="${station.code}">${station.name} (${station.province})</option>`;
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

  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');
  const originId = originSelect.value;
  const destId = destSelect.value;
  
  const originCode = originSelect.options[originSelect.selectedIndex]?.getAttribute('data-code');
  const destCode = destSelect.options[destSelect.selectedIndex]?.getAttribute('data-code');

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
    const searchPayload = {
      originStationId: Number(originId),
      destinationStationId: Number(destId),
      departureDate: document.getElementById('depart-date-select')?.value || new Date().toISOString().split('T')[0]
    };
    
    const response = await fetch(`${API_BASE}/trips/search`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(searchPayload)
    });
    
    const data = await response.json();
    currentSearchResults = data.result || [];

    const filterWrapper = document.getElementById('filter-pills-wrapper');
    if (filterWrapper) {
      filterWrapper.style.display = currentSearchResults.length > 0 ? 'flex' : 'none';
    }

    if (currentSearchResults.length === 0) {
      renderSearchResults([]);
      showToast('ℹ️ Không có chuyến tàu nào vào ngày đã chọn. Vui lòng chọn ngày khác!', 'info');
    } else {
      showToast(`🎉 Tìm thấy ${currentSearchResults.length} chuyến tàu phù hợp!`, 'success');
      const sortType = document.getElementById('sort-trips-select')?.value || 'PRICE_LOW';
      sortSearchResults(sortType);
    }

    setTimeout(() => {
      document.getElementById('search-results-section')?.scrollIntoView({ behavior: 'smooth' });
    }, 150);
  } catch (error) {
    console.error('Lỗi khi tìm kiếm chuyến tàu:', error);
    showToast('❌ Lỗi kết nối máy chủ khi tìm chuyến tàu', 'error');
  }
}

function sortSearchResults(sortType) {
  if (!currentSearchResults || currentSearchResults.length === 0) {
    renderSearchResults([]);
    return;
  }

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

  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');
  const originName = originSelect && originSelect.selectedIndex > 0 ? originSelect.options[originSelect.selectedIndex].text.split(' (')[0] : 'Ga đi';
  const destName = destSelect && destSelect.selectedIndex > 0 ? destSelect.options[destSelect.selectedIndex].text.split(' (')[0] : 'Ga đến';
  const departDate = document.getElementById('depart-date-select')?.value || '';

  if (!trips || trips.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="text-align: center; padding: 40px 20px; background: var(--surface-dark, #1e2230); border-radius: 12px; margin-top: 20px; border: 1px dashed rgba(255,255,255,0.15);">
        <span class="empty-icon" style="font-size: 3rem; display: block; margin-bottom: 12px;">🚫</span>
        <h4 style="margin: 0 0 8px; font-size: 1.25rem; color: #fff;">Không tìm thấy chuyến tàu nào</h4>
        <p style="color: var(--gray-text, #94a3b8); margin: 0 0 12px;">Không có chuyến tàu nào từ <b>${escapeHtml(originName)}</b> đến <b>${escapeHtml(destName)}</b> vào ngày <b>${escapeHtml(departDate)}</b>.</p>
        <p style="color: #ff6b35; font-size: 0.95rem; margin: 0;">💡 Gợi ý: Hãy thử chọn các ngày có chuyến tàu hoạt động như hôm nay hoặc những ngày sắp tới.</p>
      </div>
    `;
    return;
  }

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
    const currentTripId = trip.tripId || trip.id;

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
            <button type="button" class="btn btn-primary" onclick="openBookingModal(${currentTripId}, ${trip.basePrice || 750000}, ${trip.originStationId || null}, ${trip.destinationStationId || null})">
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
      const errorMsg = data.message || 'Tài khoản hoặc mật khẩu không chính xác!';
      showToast(`❌ ${errorMsg}`, 'error');
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
  if (token && isTokenExpired(token)) {
    console.warn('⚠️ Token đăng nhập đã hết hạn, tự động làm sạch phiên làm việc.');
    localStorage.removeItem('vnr_jwt_token');
    localStorage.removeItem('vnr_username');
    localStorage.removeItem('vnr_user_profile');
  }

  const validToken = localStorage.getItem('vnr_jwt_token');
  const username = localStorage.getItem('vnr_username') || 'User';

  const authButtonContainer = document.querySelector('.auth-buttons');
  if (!authButtonContainer) return;

  const themeBtnHTML = `<button type="button" id="theme-toggle-btn" class="btn-theme-toggle" title="Đổi Chế độ Sáng/Tối">🌙</button>`;

  if (validToken) {
    authButtonContainer.innerHTML = `
      ${themeBtnHTML}
      <span class="user-badge">👤 ${escapeHtml(username)}</span>
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
function openBookingModal(tripId, basePrice = 750000, originId = null, destId = null) {
  const token = localStorage.getItem('vnr_jwt_token');
  if (token && isTokenExpired(token)) {
    console.warn('⚠️ Token hết hạn, chuyển sang chế độ đặt vé khách vãng lai.');
    localStorage.removeItem('vnr_jwt_token');
    localStorage.removeItem('vnr_username');
    localStorage.removeItem('vnr_user_profile');
    checkLoginState();
  }

  currentBaseSeatPrice = basePrice;
  selectedSeatIds = [];

  const passengerCountSelect = document.getElementById('passenger-count-select');
  maxSeatsAllowed = parseInt(passengerCountSelect ? passengerCountSelect.value : '1');

  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');

  const finalOriginId = originId || (originSelect ? originSelect.value : null) || 1;
  const finalDestId = destId || (destSelect ? destSelect.value : null) || 3;

  document.getElementById('booking-trip-id').value = tripId;
  const originInput = document.getElementById('booking-origin-id');
  if (originInput) originInput.value = finalOriginId;
  const destInput = document.getElementById('booking-dest-id');
  if (destInput) destInput.value = finalDestId;

  document.getElementById('selected-seat-id').value = '';
  document.getElementById('selected-seat-display').innerText = 'Chưa chọn';
  document.getElementById('total-price-display').innerText = '0 VNĐ';

  const profileStr = localStorage.getItem('vnr_user_profile');
  const nameInput = document.getElementById('customer-name');
  const phoneInput = document.getElementById('customer-phone');
  const emailInput = document.getElementById('customer-email');
  const idCardInput = document.getElementById('passenger-idcard');

  if (profileStr) {
    try {
      const profile = JSON.parse(profileStr);
      if (nameInput) nameInput.value = profile.fullName || profile.username || '';
      if (phoneInput) phoneInput.value = profile.phone || '0987654321';
      if (emailInput) emailInput.value = profile.email || `${profile.username}@gmail.com`;
      if (idCardInput && !idCardInput.value) idCardInput.value = '001200001234';
    } catch (e) {
      console.warn('Cannot parse profile:', e);
    }
  } else {
    // Tự động điền dữ liệu mẫu giúp test nhanh không cần gõ nhiều
    if (nameInput && !nameInput.value) nameInput.value = 'Nguyễn Văn An';
    if (phoneInput && !phoneInput.value) phoneInput.value = '0987654321';
    if (emailInput && !emailInput.value) emailInput.value = 'khachhang@vnr.vn';
    if (idCardInput && !idCardInput.value) idCardInput.value = '001200001234';
  }

  document.getElementById('booking-modal').style.display = 'flex';
  startHoldTimer();
  loadCarriageSeatMap(tripId, 1);
}

// Render Sơ đồ Ghế Tàu 4 Hàng (kết nối trực tiếp API Backend)
async function loadCarriageSeatMap(tripId, carriageId) {
  const seatContainer = document.getElementById('seat-grid-container');
  if (!seatContainer) return;

  selectedSeatIds = [];
  document.getElementById('selected-seat-id').value = '';
  const displayElem = document.getElementById('selected-seat-display');
  if (displayElem) displayElem.innerText = 'Chưa chọn';

  const originInput = document.getElementById('booking-origin-id');
  const destInput = document.getElementById('booking-dest-id');
  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');
  const startId = (originInput && originInput.value) ? originInput.value : (originSelect ? originSelect.value : 1);
  const endId = (destInput && destInput.value) ? destInput.value : (destSelect ? destSelect.value : 3);

  let seatsData = [];
  if (tripId && startId && endId) {
    try {
      const res = await fetch(`${API_BASE}/trips/${tripId}/carriages/${carriageId}/seat-map?originStationId=${startId}&destStationId=${endId}`);
      if (res.ok) {
        const json = await res.json();
        if (json.result && json.result.seats) {
          seatsData = json.result.seats;
        }
      }
    } catch (err) {
      console.warn('Could not fetch real seat map, using fallback', err);
    }
  }

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

  if (seatsData.length > 0) {
    seatsData.sort((a, b) => a.seatNumber - b.seatNumber);
    seatsData.forEach((seat) => {
      const isBooked = (seat.status === 'BOOKED');
      const statusClass = isBooked ? 'booked' : 'available';
      const seatBtn = `<button type="button" class="seat-btn ${statusClass}" data-seat-id="${seat.seatId}" data-seat-num="${seat.seatNumber}" ${isBooked ? 'disabled' : ''} onclick="selectSeat(this, ${seat.seatId})">${seat.seatNumber}</button>`;

      const mod = seat.seatNumber % 4;
      if (mod === 1) row1.innerHTML += seatBtn;
      else if (mod === 2) row2.innerHTML += seatBtn;
      else if (mod === 3) row3.innerHTML += seatBtn;
      else row4.innerHTML += seatBtn;
    });
  } else {
    for (let i = 1; i <= 28; i++) {
      const isBooked = (i % 7 === 0 || i % 11 === 0);
      const statusClass = isBooked ? 'booked' : 'available';
      const seatBtn = `<button type="button" class="seat-btn ${statusClass}" data-seat-id="${i}" data-seat-num="${i}" ${isBooked ? 'disabled' : ''} onclick="selectSeat(this, ${i})">${i}</button>`;

      const mod = i % 4;
      if (mod === 1) row1.innerHTML += seatBtn;
      else if (mod === 2) row2.innerHTML += seatBtn;
      else if (mod === 3) row3.innerHTML += seatBtn;
      else row4.innerHTML += seatBtn;
    }
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
      return (num % 4 === 1 || num % 4 === 0);
    });
  } else if (type === 'aisle') {
    pool = pool.filter((s) => {
      const num = parseInt(s.getAttribute('data-seat-num'));
      return (num % 4 === 2 || num % 4 === 3);
    });
  }

  if (pool.length === 0) pool = Array.from(availableSeats);

  for (let i = 0; i < maxSeatsAllowed && i < pool.length; i++) {
    const seatBtn = pool[i];
    const seatId = parseInt(seatBtn.getAttribute('data-seat-id'));
    selectedSeatIds.push(seatId);
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
// 16. TẠO ĐƠN ĐẶT VÉ & THANH TOÁN (VNPAY / VIETQR / MOMO)
// ==========================================
async function handleCreateBooking(event) {
  event.preventDefault();

  const token = localStorage.getItem('vnr_jwt_token');

  if (!selectedSeatIds || selectedSeatIds.length === 0) {
    const rawVal = document.getElementById('selected-seat-id')?.value;
    if (rawVal) {
      selectedSeatIds = rawVal.split(',').map((s) => parseInt(s.trim())).filter(Boolean);
    }
  }

  if (!selectedSeatIds || selectedSeatIds.length === 0) {
    showToast('⚠️ Vui lòng chọn ít nhất 1 ghế tàu trên sơ đồ trước khi Xác Nhận Đặt Vé!', 'error');
    return;
  }

  const tripIdVal = document.getElementById('booking-trip-id')?.value;
  const name = document.getElementById('customer-name')?.value?.trim();
  const phone = document.getElementById('customer-phone')?.value?.trim();
  const email = document.getElementById('customer-email')?.value?.trim();
  const idCard = document.getElementById('passenger-idcard')?.value?.trim();

  if (!name) {
    showToast('⚠️ Vui lòng nhập Họ và Tên hành khách!', 'error');
    return;
  }
  if (!phone || !/^[0-9]{10}$/.test(phone)) {
    showToast('⚠️ Số điện thoại phải gồm đúng 10 chữ số!', 'error');
    return;
  }
  if (!email || !email.includes('@')) {
    showToast('⚠️ Địa chỉ Email không hợp lệ!', 'error');
    return;
  }
  if (!idCard) {
    showToast('⚠️ Vui lòng nhập Số CCCD / Passport của hành khách!', 'error');
    return;
  }

  const originInput = document.getElementById('booking-origin-id');
  const destInput = document.getElementById('booking-dest-id');
  const originSelect = document.getElementById('origin-select');
  const destSelect = document.getElementById('dest-select');

  const startId = (originInput && originInput.value) ? originInput.value : (originSelect ? originSelect.value : 1);
  const endId = (destInput && destInput.value) ? destInput.value : (destSelect ? destSelect.value : 3);
  const tripId = Number(tripIdVal || 1);

  if (!startId || !endId || Number(startId) === 0 || Number(endId) === 0 || Number(startId) === Number(endId)) {
    showToast('⚠️ Hành trình ga đi và ga đến không hợp lệ! Vui lòng chọn lại chuyến.', 'error');
    return;
  }

  const bookingData = {
    customerName: name,
    customerPhone: phone,
    customerEmail: email,
    tripId: tripId,
    startStationId: Number(startId),
    endStationId: Number(endId),
    tickets: selectedSeatIds.map((sId) => ({
      seatId: Number(sId),
      passengerName: name,
      passengerIdCard: idCard,
    })),
  };

  showToast('⏳ Đang xử lý tạo đơn đặt vé...', 'info');

  try {
    const headers = {
      'Content-Type': 'application/json',
    };
    if (token && !isTokenExpired(token)) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    let response = await fetch(`${API_BASE}/bookings`, {
      method: 'POST',
      headers: headers,
      body: JSON.stringify(bookingData),
    });

    let data;
    if (response.status === 401) {
      console.warn('⚠️ Token hết hạn hoặc không hợp lệ (401), tự động dọn dẹp và thử lại đơn vé...');
      localStorage.removeItem('vnr_jwt_token');
      localStorage.removeItem('vnr_username');
      localStorage.removeItem('vnr_user_profile');
      checkLoginState();
      
      response = await fetch(`${API_BASE}/bookings`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bookingData),
      });
    }

    data = await response.json();

    if (response.ok && data.result) {
      let booking = data.result;
      const bookingCode = booking.bookingCode || 'VNR-OK';
      stopHoldTimer();
      document.getElementById('booking-modal').style.display = 'none';

      const payRadio = document.querySelector('input[name="paymentMethod"]:checked');
      const paymentMethod = payRadio ? payRadio.value : 'CASH';

      if (paymentMethod === 'CASH') {
        showToast(`💵 Đang xác nhận thanh toán tiền mặt cho mã vé ${bookingCode}...`, 'info');
        try {
          const cashRes = await fetch(`${API_BASE}/payments/cash-pay/${encodeURIComponent(bookingCode)}`, {
            method: 'POST'
          });
          const cashJson = await cashRes.json();
          if (cashRes.ok && cashJson.result) {
            booking = cashJson.result;
          }
        } catch (cashErr) {
          console.warn('Lỗi gọi cash-pay:', cashErr);
        }

        showToast(`🎉 Đặt vé & Thanh toán tiền mặt THÀNH CÔNG! Mã vé: ${bookingCode}`, 'success');

        const formattedAmount = booking.totalAmount
          ? Number(booking.totalAmount).toLocaleString('vi-VN')
          : '0';

        setTimeout(() => {
          if (typeof openPrintModal === 'function') {
            openPrintModal(bookingCode, name, phone, formattedAmount);
          }
        }, 400);

        document.getElementById('booking-form').reset();
        selectedSeatIds = [];
        if (typeof loadMyBookings === 'function') {
          loadMyBookings();
        }
        return;
      } else if (paymentMethod === 'VNPAY_SIMULATOR') {
        showToast(`🎉 Đặt vé thành công! Đang chuyển sang cổng VNPAY Test...`, 'info');
        setTimeout(() => {
          window.location.href = `/vnpay-sandbox.html?bookingCode=${bookingCode}&amount=${booking.totalAmount || 0}`;
        }, 400);
        return;
      } else if (paymentMethod === 'VNPAY') {
        showToast(`🎉 Đặt vé thành công! Đang chuyển hướng sang cổng VNPAY...`, 'success');
        try {
          const payRes = await fetch(`${API_BASE}/payments/vnpay-pay/${bookingCode}`);
          const payJson = await payRes.json();
          if (payRes.ok && payJson.result && payJson.result.paymentUrl) {
            window.location.href = payJson.result.paymentUrl;
            return;
          }
        } catch (payErr) {
          console.warn('Lỗi khi mở cổng VNPAY:', payErr);
        }
        showToast(`⚠️ Không thể mở tự động link VNPAY. Mã đơn vé: ${bookingCode}`, 'info');
      } else {
        // VIETQR / MOMO
        openQrPaymentModal(bookingCode, booking.totalAmount || 0, paymentMethod);
      }

      document.getElementById('booking-form').reset();
      selectedSeatIds = [];
      if (typeof loadMyBookings === 'function') {
        loadMyBookings();
      }
    } else {
      showToast('❌ Đặt vé thất bại: ' + (data.message || 'Dữ liệu không hợp lệ!'), 'error');
    }
  } catch (error) {
    console.error('❌ Lỗi khi đặt vé:', error);
    showToast('❌ Lỗi kết nối máy chủ Spring Boot!', 'error');
  }
}

// Mở Modal Thanh Toán QR (VietQR Net hoặc Ví MoMo)
function openQrPaymentModal(bookingCode, totalAmount, method) {
  const modal = document.getElementById('qr-payment-modal');
  if (!modal) return;

  const titleElem = document.getElementById('qr-modal-title');
  const codeElem = document.getElementById('qr-booking-code');
  const amountElem = document.getElementById('qr-amount');
  const memoElem = document.getElementById('qr-memo');
  const imgElem = document.getElementById('qr-payment-image');

  const formattedAmount = Number(totalAmount).toLocaleString('vi-VN');

  if (codeElem) codeElem.innerText = bookingCode;
  if (amountElem) amountElem.innerText = `${formattedAmount} VNĐ`;
  if (memoElem) memoElem.innerText = `Thanh toan ve tau ${bookingCode}`;

  if (method === 'MOMO') {
    if (titleElem) titleElem.innerText = '📱 Quét Mã Ví MoMo';
    if (imgElem) {
      imgElem.src = `https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${encodeURIComponent('2|99|0987654321|VNR EXPRESS|admin@trainbooking.com|0|0|' + totalAmount + '|' + bookingCode + '|transfer_p2p')}`;
    }
  } else {
    // VIETQR
    if (titleElem) titleElem.innerText = '🏦 Quét Mã VietQR Net (MB Bank)';
    if (imgElem) {
      imgElem.src = `https://img.vietqr.io/image/MB-0987654321-compact2.png?amount=${totalAmount}&addInfo=${encodeURIComponent(bookingCode)}&accountName=VNR%20EXPRESS`;
    }
  }

  modal.style.display = 'flex';

  const closeBtn = document.getElementById('close-qr-modal-btn');
  if (closeBtn) {
    closeBtn.onclick = () => {
      modal.style.display = 'none';
      if (typeof loadMyBookings === 'function') loadMyBookings();
    };
  }

  const finishBtn = document.getElementById('btn-finish-qr-payment');
  if (finishBtn) {
    finishBtn.onclick = async () => {
      modal.style.display = 'none';
      showToast(`🎉 Cảm ơn bạn! Đơn vé ${bookingCode} đã thanh toán thành công.`, 'success');
      try {
        await fetch(`/api/v1/payments/simulate-success/${bookingCode}`);
      } catch (e) {
        console.warn('Lỗi đồng bộ trạng thái thanh toán:', e);
      }
      if (typeof openPrintModal === 'function') {
        openPrintModal(bookingCode, 'Hành khách VNR', '', formattedAmount);
      }
      if (typeof loadMyBookings === 'function') loadMyBookings();
    };
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

      const safeBookingCode = escapeHtml(item.bookingCode);
      const safeCustomerName = escapeHtml(item.customerName);
      const safeCustomerPhone = escapeHtml(item.customerPhone);
      const safeCustomerEmail = escapeHtml(item.customerEmail);

      const isCancelled = item.status === 'CANCELLED';
      const statusClass = isCancelled ? 'booking-card cancelled' : 'booking-card';
      const badgeClass = isCancelled ? 'booking-status-badge cancelled' : 'booking-status-badge';
      const statusText = isCancelled ? 'ĐÃ HỦY' : 'THÀNH CÔNG';

      const cancelBtnHTML = isCancelled
        ? ''
        : `<button type="button" class="btn btn-outline btn-danger" onclick="openCancelModal('${safeBookingCode}')">
            🗑️ Hủy Vé
          </button>`;

      const printBtnHTML = isCancelled
        ? ''
        : `<button type="button" class="btn btn-outline" onclick="openPrintModal('${safeBookingCode}', '${safeCustomerName}', '${safeCustomerPhone}', '${formattedTotal}')">
            🖨️ In Vé QR
          </button>`;

      html += `
        <article class="${statusClass}">
          <div>
            <div class="booking-header-info">
              <span class="booking-code-badge">Mã vé: ${safeBookingCode}</span>
              <span class="${badgeClass}">${statusText}</span>
            </div>
            <p class="passenger-info">Hành khách: <strong>${safeCustomerName}</strong> (${safeCustomerPhone})</p>
            <p class="passenger-email">Email: ${safeCustomerEmail}</p>
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
async function openPrintModal(code, name, phone, price) {
  if (code) document.getElementById('print-ticket-code').innerText = code;
  if (name) document.getElementById('print-customer-name').innerText = name;
  if (phone) document.getElementById('print-customer-phone').innerText = phone;
  if (price) document.getElementById('print-total-price').innerText = `${price} VNĐ`;

  if (code) {
    try {
      const headers = {};
      const token = localStorage.getItem('vnr_jwt_token');
      if (token) headers['Authorization'] = `Bearer ${token}`;
      const res = await fetch(`${API_BASE}/bookings/${encodeURIComponent(code)}`, { headers });
      const data = await res.json();
      if (res.ok && data.result) {
        const b = data.result;
        if (b.customerName) document.getElementById('print-customer-name').innerText = b.customerName;
        if (b.customerPhone) document.getElementById('print-customer-phone').innerText = b.customerPhone;
        if (b.trainName || b.tripCode) document.getElementById('print-train-code').innerText = `${b.tripCode || ''} ${b.trainName || ''}`.trim();
        if (b.startStationName && b.endStationName) {
          const routeElem = document.getElementById('print-route-name');
          if (routeElem) routeElem.innerText = `${b.startStationName} ➔ ${b.endStationName}`;
        }
        if (b.tickets && b.tickets.length > 0) {
          const seatDescs = b.tickets.map(t => `Ghế ${t.seatNumber} (Toa ${t.carriageNumber || 1})`).join(', ');
          document.getElementById('print-seat-number').innerText = seatDescs;
        }
        if (b.totalAmount) {
          document.getElementById('print-total-price').innerText = `${Number(b.totalAmount).toLocaleString('vi-VN')} VNĐ`;
        }
        name = b.customerName || name;
      }
    } catch (e) {
      console.warn('Lỗi tải chi tiết vé:', e);
    }
  }

  const qrImg = document.getElementById('print-qr-image');
  if (qrImg) {
    qrImg.src = `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=VNR_CHECKIN_TICKET_${code}_PASSENGER_${encodeURIComponent(name || 'KHACH')}`;
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
