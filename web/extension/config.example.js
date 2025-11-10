// config.example.js - 설정 템플릿
const CONFIG = {
  API_BASE_URL: "YOUR_API_BASE_URL_HERE",
  GOOGLE_CLIENT_ID: "YOUR_GOOGLE_CLIENT_ID_HERE"
};

if (typeof module !== 'undefined' && module.exports) {
  module.exports = CONFIG;
}