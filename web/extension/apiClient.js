// apiClient.js - 크롬 확장 프로그램용 API 클라이언트

// 설정 로드
importScripts('config.js');

const API_BASE_URL = CONFIG.API_BASE_URL;
/**
 * API 요청 함수 (자동 토큰 갱신 포함)
 */
async function apiRequest(url, options = {}) {
  try {
    // 1. 저장된 토큰 가져오기
    const tokens = await getStoredTokens();
    
    // 2. 헤더 설정
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    };
    
    if (tokens.accessToken) {
      headers['Authorization'] = `Bearer ${tokens.accessToken}`;
    }
    
    // 3. API 요청
    let response = await fetch(API_BASE_URL + url, {
      ...options,
      headers
    });
    
    // 4. 401 에러 시 토큰 갱신 후 재시도
    if (response.status === 401 && tokens.refreshToken) {
      console.log('Access token 만료, 갱신 중...');
      
      try {
        const newAccessToken = await refreshToken(tokens.refreshToken);
        
        // 새 토큰으로 재시도
        headers['Authorization'] = `Bearer ${newAccessToken}`;
        response = await fetch(API_BASE_URL + url, {
          ...options,
          headers
        });
        
      } catch (refreshError) {
        console.error('토큰 갱신 실패:', refreshError);
        // 로그아웃 처리
        await clearAuth();
        throw new Error('인증이 만료되었습니다. 다시 로그인해주세요.');
      }
    }
    
    // 5. 응답 처리
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `API 요청 실패: ${response.status}`);
    }
    
    return response;
    
  } catch (error) {
    console.error('API 요청 오류:', error);
    throw error;
  }
}

/**
 * chrome.storage에서 토큰 가져오기
 */
function getStoredTokens() {
  return new Promise((resolve) => {
    chrome.storage.local.get(['accessToken', 'refreshToken'], (result) => {
      resolve({
        accessToken: result.accessToken || '',
        refreshToken: result.refreshToken || ''
      });
    });
  });
}

/**
 * 토큰 갱신
 */
async function refreshToken(refreshToken) {
  const response = await fetch(API_BASE_URL + '/oauth/reissue', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${refreshToken}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (!response.ok) {
    throw new Error('토큰 갱신 실패');
  }
  
  const data = await response.json();
  const newAccessToken = data.data?.accessToken || data.accessToken;
  const newRefreshToken = data.data?.refreshToken || data.refreshToken;
  
  // 새 토큰 저장
  await new Promise((resolve) => {
    chrome.storage.local.set({
      accessToken: newAccessToken,
      refreshToken: newRefreshToken
    }, resolve);
  });
  
  return newAccessToken;
}

/**
 * 인증 정보 초기화
 */
function clearAuth() {
  return new Promise((resolve) => {
    chrome.storage.local.remove(['accessToken', 'refreshToken', 'user'], resolve);
  });
}

/**
 * 이미지 업로드 API
 */
async function uploadImage(imageData, metadata = {}) {
  try {
    // Base64를 Blob으로 변환
    const blob = base64ToBlob(imageData);
    
    // FormData 생성
    const formData = new FormData();
    formData.append('image', blob, 'capture.png');
    
    // 메타데이터 추가
    if (metadata.title) formData.append('title', metadata.title);
    if (metadata.description) formData.append('description', metadata.description);
    if (metadata.url) formData.append('sourceUrl', metadata.url);
    
    // 토큰 가져오기
    const tokens = await getStoredTokens();
    
    // 업로드 요청
    const response = await fetch(API_BASE_URL + '/api/images/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${tokens.accessToken}`
        // Content-Type은 FormData가 자동 설정
      },
      body: formData
    });
    
    if (!response.ok) {
      throw new Error('이미지 업로드 실패');
    }
    
    return await response.json();
    
  } catch (error) {
    console.error('이미지 업로드 오류:', error);
    throw error;
  }
}

/**
 * Base64를 Blob으로 변환
 */
function base64ToBlob(base64) {
  // data:image/png;base64, 제거
  const base64Data = base64.split(',')[1];
  const byteCharacters = atob(base64Data);
  const byteNumbers = new Array(byteCharacters.length);
  
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  
  const byteArray = new Uint8Array(byteNumbers);
  return new Blob([byteArray], { type: 'image/png' });
}

/**
 * 텍스트 저장 API
 */
async function saveText(text, metadata = {}) {
  try {
    const response = await apiRequest('/api/texts', {
      method: 'POST',
      body: JSON.stringify({
        content: text,
        title: metadata.title || '캡처된 텍스트',
        sourceUrl: metadata.url,
        timestamp: new Date().toISOString()
      })
    });
    
    return await response.json();
    
  } catch (error) {
    console.error('텍스트 저장 오류:', error);
    throw error;
  }
}