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

  const dropZone = document.getElementById('dropZone');
  const fileInput = document.getElementById('fileInput');

  dropZone.addEventListener('click', () => fileInput.click());
  dropZone.addEventListener('dragover', handleDragOver);
  dropZone.addEventListener('dragleave', handleDragLeave);
  dropZone.addEventListener('drop', handleDrop);
  fileInput.addEventListener('change', handleFileSelect);
  // 통계 업데이트 (선택사항)
  // updateStats();
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
 * 드래그 오버 처리
 */
function handleDragOver(e) {
  e.preventDefault();
  e.stopPropagation();
  e.currentTarget.classList.add('drag-over');
}

/**
 * 드래그 떠남 처리
 */
function handleDragLeave(e) {
  e.preventDefault();
  e.stopPropagation();
  e.currentTarget.classList.remove('drag-over');
}

/**
 * 드롭 처리
 */
function handleDrop(e) {
  e.preventDefault();
  e.stopPropagation();
  e.currentTarget.classList.remove('drag-over');
  
  const files = e.dataTransfer.files;
  if (files.length > 0) {
    handleImageUpload(files[0]);
  }
}

/**
 * 파일 선택 처리
 */
function handleFileSelect(e) {
  const files = e.target.files;
  if (files.length > 0) {
    handleImageUpload(files[0]);
  }
}

/**
 * 이미지 업로드 처리
 */
async function handleImageUpload(file) {
  const uploadStatus = document.getElementById('uploadStatus');
  
  // 이미지 파일 확인
  if (!file.type.startsWith('image/')) {
    showUploadStatus('이미지 파일만 업로드 가능합니다.', 'error');
    return;
  }
  
  // 파일 크기 확인 (10MB 제한)
  if (file.size > 10 * 1024 * 1024) {
    showUploadStatus('파일 크기는 10MB 이하여야 합니다.', 'error');
    return;
  }
  
  try {
    showUploadStatus('업로드 중...', 'uploading');
    
    // 파일을 Base64로 변환
    const base64 = await fileToBase64(file);
    
    // Background script로 업로드 요청
    const response = await chrome.runtime.sendMessage({
      action: 'uploadImage',
      imageData: base64,
      metadata: {
        title: file.name,
        url: await getCurrentTabUrl()
      }
    });
    
    if (response.success) {
      showUploadStatus('업로드 완료!', 'success');
      // 3초 후 상태 메시지 숨김
      setTimeout(() => {
        uploadStatus.style.display = 'none';
      }, 3000);
    } else {
      throw new Error(response.error || '업로드 실패');
    }
    
  } catch (error) {
    console.error('업로드 오류:', error);
    showUploadStatus('업로드 실패: ' + error.message, 'error');
  }
}

/**
 * 업로드 상태 표시
 */
function showUploadStatus(message, type) {
  const uploadStatus = document.getElementById('uploadStatus');
  uploadStatus.textContent = message;
  uploadStatus.className = type;
  uploadStatus.style.display = 'block';
}

/**
 * 파일을 Base64로 변환
 */
function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

/**
 * 현재 탭 URL 가져오기
 */
async function getCurrentTabUrl() {
  try {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    return tab?.url || '';
  } catch {
    return '';
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
      // updateStats();
    }
  }
});