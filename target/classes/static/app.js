// Đường dẫn gốc của Backend Spring Boot

const API_BASE = 'http://localhost:8080/api/v1';

// Lắng nghe sự kiện khi trang web tải xong toàn bộ cấu trúc HTML
document.addEventListener('DOMContentLoaded', () => {
  console.log('⚡ Trang web đã tải xong. Bắt đầu nạp dữ liệu Ga tàu...');
  loadStations();
});

async function loadStations(params) {
  try {
    // 1. Gửi request HTTP GET sang Spring Boot
    const response = await fetch(`${API_BASE}/stations`);
    const data = await response.json();
    // Spring Boot trả về response dạng: { code: 1000, result: [ {id, code, name, province}, ... ] }
    const stationList = data.result || data || [];

    console.log('✅ Đã nhận danh sách Ga tàu từ Backend:', stationList);

    // 2. Bắt 2 ô chọn Ga Đi và Ga Đến trong HTML bằng ID
    const originSelect = document.getElementById('orgin-select');
    const destSelect = document.getElementById('dest-select');

    if (!originSelect || !destSelect) return;
    originSelect.innerHTML = '<option value="">--Chọn Ga Khởi Hành--</option>';
    destSelect.innerHTML = '<option value="">--Chọn Ga Đến--</option>';

    stationList.forEach((station) => {
      const optionHTML = `<option value="${station.code}">${station.name} (${station.province}) </option>`;
      originSelect.innerHTML += optionHTML;
      destSelect.innerHTML += optionHTML;
    });
  } catch (error) {
    console.error('❌ Lỗi khi kết nối với Backend Spring Boot:', error);
  }
}
