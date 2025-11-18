// popup.js - 팝업 UI 컨트롤러
console.log('🟢 popup.js 파일 로드됨!');

// 즉시 실행되는 테스트 코드
(function () {
  console.log('🟢 popup.js 즉시 실행 함수 실행됨!');
  console.log('현재 URL:', window.location.href);
  console.log('document.readyState:', document.readyState);
})();
document.addEventListener('DOMContentLoaded', async () => {
  console.log('===========================================');
  console.log('🚀 popup.js 로드 시작');
  console.log('===========================================');

  // 요소 참조
  const loginSection = document.getElementById('login-section');
  const authenticatedSection = document.getElementById('authenticated-section');
  const loading = document.getElementById('loading');
  const loginBtn = document.getElementById('loginBtn');
  const logoutBtn = document.getElementById('logoutBtn');

  console.log('📋 DOM 요소 확인:');
  console.log('  - loginSection:', !!loginSection);
  console.log('  - authenticatedSection:', !!authenticatedSection);
  console.log('  - loading:', !!loading);
  console.log('  - loginBtn:', !!loginBtn);
  console.log('  - logoutBtn:', !!logoutBtn);

  // 초기화
  console.log('🔄 초기 인증 상태 확인 시작...');
  await checkAuthAndUpdateUI();

  // 포커스 얻을 때마다 재확인
  window.addEventListener('focus', async () => {
    console.log('🔄 팝업 포커스 - 인증 상태 재확인');
    await checkAuthAndUpdateUI();
  });

  // 이벤트 리스너 등록
  console.log('🎯 이벤트 리스너 등록 중...');

  if (loginBtn) {
    loginBtn.addEventListener('click', handleLogin);
    console.log('  ✅ loginBtn 클릭 이벤트 등록');
  } else {
    console.error('  ❌ loginBtn을 찾을 수 없음!');
  }

  if (logoutBtn) {
    logoutBtn.addEventListener('click', handleLogout);
    console.log('  ✅ logoutBtn 클릭 이벤트 등록');
  } else {
    console.error('  ❌ logoutBtn을 찾을 수 없음!');
  }

  // 나머지 이벤트 리스너...
  const dropZone = document.getElementById('dropZone');
  const fileInput = document.getElementById('fileInput');

  if (dropZone && fileInput) {
    dropZone.addEventListener('click', () => fileInput.click());
    dropZone.addEventListener('dragover', handleDragOver);
    dropZone.addEventListener('dragleave', handleDragLeave);
    dropZone.addEventListener('drop', handleDrop);
    fileInput.addEventListener('change', handleFileSelect);
    console.log('  ✅ 드롭존 이벤트 등록');
  }

  console.log('===========================================');
  console.log('✅ popup.js 초기화 완료');
  console.log('===========================================');
});

/**
 * 인증 상태 확인 및 UI 업데이트
 */
async function checkAuthAndUpdateUI() {
  console.log('===========================================');
  console.log('🔍 checkAuthAndUpdateUI 시작');
  console.log('===========================================');

  try {
    const loginSection = document.getElementById('login-section');
    const authenticatedSection = document.getElementById('authenticated-section');

    // ⭐ 1. 먼저 로컬 storage 직접 확인
    const storageData = await new Promise((resolve) => {
      chrome.storage.local.get(['accessToken', 'refreshToken', 'user'], (result) => {
        resolve(result);
      });
    });

    console.log('📦 Storage 직접 확인 결과:');
    console.log('  - accessToken:', storageData.accessToken ?
      `존재 (길이: ${storageData.accessToken.length})` : '❌ 없음');
    console.log('  - refreshToken:', storageData.refreshToken ?
      `존재 (길이: ${storageData.refreshToken.length})` : '❌ 없음');
    console.log('  - user:', storageData.user ?
      `존재 (${storageData.user.email})` : '❌ 없음');

    // ⭐ 2. 토큰이 하나라도 없으면 즉시 로그아웃 UI
    if (!storageData.accessToken || !storageData.refreshToken) {
      console.log('❌ 토큰 부족 - 로그아웃 UI 표시');
      console.log('  → loginSection.display = block');
      console.log('  → authenticatedSection.display = none');

      loginSection.style.display = 'block';
      authenticatedSection.style.display = 'none';

      console.log('===========================================');
      console.log('✅ 로그아웃 UI 표시 완료');
      console.log('===========================================');
      return;
    }

    // ⭐ 3. 토큰이 있으면 Background 확인
    console.log('✅ 토큰 존재 - Background에 인증 상태 요청');

    const response = await chrome.runtime.sendMessage({ action: 'checkAuth' });

    console.log('📨 Background 응답:', response);
    console.log('  - isAuthenticated:', response.isAuthenticated);
    console.log('  - user:', response.user);

    if (response.isAuthenticated && response.user) {
      // 로그인 상태
      console.log('✅ 로그인 상태 - 인증된 UI 표시');
      console.log('  → loginSection.display = none');
      console.log('  → authenticatedSection.display = block');

      loginSection.style.display = 'none';
      authenticatedSection.style.display = 'block';
      updateUserInfo(response.user);

      console.log('===========================================');
      console.log('✅ 로그인 UI 표시 완료');
      console.log('===========================================');
    } else {
      // 로그아웃 상태
      console.log('❌ 로그아웃 상태 - 로그인 UI 표시');
      console.log('  → loginSection.display = block');
      console.log('  → authenticatedSection.display = none');

      loginSection.style.display = 'block';
      authenticatedSection.style.display = 'none';

      console.log('===========================================');
      console.log('✅ 로그아웃 UI 표시 완료');
      console.log('===========================================');
    }

  } catch (error) {
    console.error('❌ 인증 상태 확인 중 에러:', error);
    console.error('에러 스택:', error.stack);

    // 에러 시 안전하게 로그아웃 UI
    const loginSection = document.getElementById('login-section');
    const authenticatedSection = document.getElementById('authenticated-section');

    console.log('⚠️ 에러로 인한 로그아웃 UI 표시');
    loginSection.style.display = 'block';
    authenticatedSection.style.display = 'none';

    console.log('===========================================');
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
  console.log('===========================================');
  console.log('🔐 handleLogin 시작');
  console.log('===========================================');

  const loginSection = document.getElementById('login-section');
  const loading = document.getElementById('loading');

  try {
    // 로딩 표시
    console.log('⏳ 로딩 화면 표시');
    loginSection.style.display = 'none';
    loading.style.display = 'block';

    console.log('📤 Background로 로그인 요청 전송...');

    // Background script에 로그인 요청
    const response = await chrome.runtime.sendMessage({ action: 'login' });

    console.log('📨 Background 응답 수신:', response);

    if (response.success) {
      console.log('✅ 로그인 성공!');

      // 잠시 대기 (토큰이 storage에 저장되는 시간)
      console.log('⏳ Storage 저장 대기 (500ms)...');
      await new Promise(resolve => setTimeout(resolve, 500));

      console.log('🔄 UI 업데이트 시작...');
      // 로그인 성공 - UI 업데이트
      await checkAuthAndUpdateUI();

      console.log('✅ UI 업데이트 완료');
      console.log('===========================================');
    } else {
      throw new Error(response.error || '로그인 실패');
    }

  } catch (error) {
    console.error('❌ 로그인 오류:', error);
    console.error('에러 스택:', error.stack);
    alert('로그인에 실패했습니다: ' + error.message);

    // 로그인 화면으로 복귀
    console.log('🔙 로그인 화면 복귀');
    loginSection.style.display = 'block';

  } finally {
    // 로딩 숨김
    console.log('⏳ 로딩 화면 숨김');
    loading.style.display = 'none';
    console.log('===========================================');
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
  const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp'];
  if (!allowedTypes.includes(file.type)) {
    showUploadStatus('PNG, JPEG, WebP 파일만 업로드 가능합니다.', 'error');
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
  console.log('===========================================');
  console.log('🔔 Storage 변경 감지됨!');
  console.log('===========================================');
  console.log('Area:', areaName);
  console.log('변경된 키:', Object.keys(changes));

  if (areaName === 'local') {
    // 각 변경사항 상세 출력
    for (const [key, { oldValue, newValue }] of Object.entries(changes)) {
      console.log(`📝 ${key}:`);
      console.log('  - oldValue:', oldValue ? '있음' : '없음');
      console.log('  - newValue:', newValue ? '있음' : '없음');
    }

    // 인증 관련 정보 변경 시 즉시 UI 업데이트
    if (changes.accessToken || changes.refreshToken || changes.user) {
      console.log('✅ 인증 관련 변경 감지 - UI 업데이트 시작');

      // accessToken이 추가되었는지 확인
      if (changes.accessToken && changes.accessToken.newValue) {
        console.log('✅ Access Token 추가됨 - 로그인 상태');
      }

      // accessToken이 삭제되었는지 확인
      if (changes.accessToken && !changes.accessToken.newValue) {
        console.log('❌ Access Token 삭제됨 - 로그아웃 상태');
      }

      // refreshToken이 삭제되었는지 확인
      if (changes.refreshToken && !changes.refreshToken.newValue) {
        console.log('❌ Refresh Token 삭제됨 - 로그아웃 상태');
      }

      checkAuthAndUpdateUI();
      console.log('===========================================');
    } else {
      console.log('⏭️ 인증 관련 변경 아님 - 무시');
      console.log('===========================================');
    }
  }
});