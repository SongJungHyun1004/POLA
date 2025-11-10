// popup.js - 팝업 UI 컨트롤러

document.addEventListener('DOMContentLoaded', async () => {
  console.log('팝업 로드됨');
  
  // 요소 참조
  const loginSection = document.getElementById('login-section');
  const authenticatedSection = document.getElementById('authenticated-section');
  const loading = document.getElementById('loading');
  const loginBtn = document.getElementById('loginBtn');
  const logoutBtn = document.getElementById('logoutBtn');
  
  // 초기화
  await checkAuthAndUpdateUI();
  
  // 이벤트 리스너 등록
  loginBtn.addEventListener('click', handleLogin);
  logoutBtn.addEventListener('click', handleLogout);
  
  // 통계 업데이트 (선택사항)
  updateStats();
});

/**
 * 인증 상태 확인 및 UI 업데이트
 */
async function checkAuthAndUpdateUI() {
  try {
    // Background script에 인증 상태 확인 요청
    const response = await chrome.runtime.sendMessage({ action: 'checkAuth' });
    
    console.log('인증 상태:', response);
    
    const loginSection = document.getElementById('login-section');
    const authenticatedSection = document.getElementById('authenticated-section');
    
    if (response.isAuthenticated && response.user) {
      // 로그인 상태 - 사용자 정보 표시
      loginSection.style.display = 'none';
      authenticatedSection.style.display = 'block';
      
      // 사용자 정보 업데이트
      updateUserInfo(response.user);
    } else {
      // 로그아웃 상태 - 로그인 화면 표시
      loginSection.style.display = 'block';
      authenticatedSection.style.display = 'none';
    }
    
  } catch (error) {
    console.error('인증 상태 확인 실패:', error);
  }
}

/**
 * 사용자 정보 UI 업데이트
 */
function updateUserInfo(user) {
  const userName = document.getElementById('userName');
  const userEmail = document.getElementById('userEmail');
  const userAvatar = document.getElementById('userAvatar');
  
  if (userName) userName.textContent = user.display_name || user.name || '사용자';
  if (userEmail) userEmail.textContent = user.email || '';
  if (userAvatar && user.profile_image_url) {
    userAvatar.src = user.profile_image_url || user.picture || '';
  }
}

/**
 * 로그인 처리
 */
async function handleLogin() {
  const loginSection = document.getElementById('login-section');
  const loading = document.getElementById('loading');
  
  try {
    // 로딩 표시
    loginSection.style.display = 'none';
    loading.style.display = 'block';
    
    console.log('로그인 요청 중...');
    
    // Background script에 로그인 요청
    const response = await chrome.runtime.sendMessage({ action: 'login' });
    
    console.log('로그인 응답:', response);
    
    if (response.success) {
      // 로그인 성공 - UI 업데이트
      await checkAuthAndUpdateUI();
    } else {
      throw new Error(response.error || '로그인 실패');
    }
    
  } catch (error) {
    console.error('로그인 오류:', error);
    alert('로그인에 실패했습니다: ' + error.message);
    
    // 로그인 화면으로 복귀
    loginSection.style.display = 'block';
    
  } finally {
    // 로딩 숨김
    loading.style.display = 'none';
  }
}

/**
 * 로그아웃 처리
 */
async function handleLogout() {
  try {
    const confirmLogout = confirm('로그아웃하시겠습니까?');
    if (!confirmLogout) return;
    
    console.log('로그아웃 요청 중...');
    
    // Background script에 로그아웃 요청
    const response = await chrome.runtime.sendMessage({ action: 'logout' });
    
    console.log('로그아웃 응답:', response);
    
    if (response.success) {
      // 로그아웃 성공 - UI 업데이트
      await checkAuthAndUpdateUI();
    } else {
      throw new Error(response.error || '로그아웃 실패');
    }
    
  } catch (error) {
    console.error('로그아웃 오류:', error);
    alert('로그아웃에 실패했습니다: ' + error.message);
  }
}

/**
 * 통계 업데이트 (선택사항)
 */
async function updateStats() {
  try {
    // chrome.storage에서 통계 가져오기
    const stats = await chrome.storage.local.get(['captureCount', 'textCount']);
    
    const captureCountEl = document.getElementById('captureCount');
    const textCountEl = document.getElementById('textCount');
    
    if (captureCountEl) {
      captureCountEl.textContent = stats.captureCount || 0;
    }
    if (textCountEl) {
      textCountEl.textContent = stats.textCount || 0;
    }
    
  } catch (error) {
    console.error('통계 업데이트 실패:', error);
  }
}

/**
 * storage 변경 감지하여 UI 자동 업데이트
 */
chrome.storage.onChanged.addListener((changes, areaName) => {
  if (areaName === 'local') {
    // 인증 정보 변경 시 UI 업데이트
    if (changes.accessToken || changes.user) {
      checkAuthAndUpdateUI();
    }
    
    // 통계 변경 시 업데이트
    if (changes.captureCount || changes.textCount) {
      updateStats();
    }
  }
});