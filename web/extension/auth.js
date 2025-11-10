// auth.js - 크롬 확장 프로그램용 인증 서비스

// 설정 로드
importScripts('config.js');

const AUTH_CONFIG = {
  apiBaseUrl: CONFIG.API_BASE_URL,
};

/**
 * Chrome Identity API를 사용한 구글 로그인
 */
async function signInWithGoogle() {
  try {
    console.log('구글 로그인 시작...');
    
    // 1. Chrome Identity API로 구글 액세스 토큰 획득
    const token = await new Promise((resolve, reject) => {
      chrome.identity.getAuthToken({ interactive: true }, (token) => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve(token);
        }
      });
    });
    
    console.log('구글 토큰 획득 완료');
    
    // 2. 구글 토큰으로 사용자 정보 가져오기
    const userInfo = await getUserInfo(token);
    console.log('사용자 정보:', userInfo);
    
    // 3. 백엔드에 구글 토큰 전송하여 JWT 토큰 받기
    const response = await fetch(`${AUTH_CONFIG.apiBaseUrl}/oauth/token`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ 
        idToken: token 
      })
    });
    
    if (!response.ok) {
      throw new Error('백엔드 인증 실패');
    }
    
    const data = await response.json();
    
    // 4. JWT 토큰 저장
    await saveTokens({
      accessToken: data.data?.accessToken || data.accessToken,
      refreshToken: data.data?.refreshToken || data.refreshToken,
      user: {
        id: userInfo.id,
        email: userInfo.email,
        display_name: userInfo.name,
        profile_image_url: userInfo.picture
      }
    });
    
    console.log('로그인 성공!');
    return { success: true, user: userInfo };
    
  } catch (error) {
    console.error('로그인 실패:', error);
    throw error;
  }
}

/**
 * 구글 사용자 정보 가져오기
 */
async function getUserInfo(token) {
  const response = await fetch('https://www.googleapis.com/oauth2/v2/userinfo', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (!response.ok) {
    throw new Error('사용자 정보 가져오기 실패');
  }
  
  return await response.json();
}

/**
 * 토큰 저장 (chrome.storage 사용)
 */
async function saveTokens({ accessToken, refreshToken, user }) {
  return new Promise((resolve, reject) => {
    chrome.storage.local.set({
      accessToken,
      refreshToken,
      user,
      loginTime: Date.now()
    }, () => {
      if (chrome.runtime.lastError) {
        reject(chrome.runtime.lastError);
      } else {
        resolve();
      }
    });
  });
}

/**
 * 저장된 토큰 가져오기
 */
async function getTokens() {
  return new Promise((resolve, reject) => {
    chrome.storage.local.get(['accessToken', 'refreshToken', 'user'], (result) => {
      if (chrome.runtime.lastError) {
        reject(chrome.runtime.lastError);
      } else {
        resolve(result);
      }
    });
  });
}

/**
 * 로그아웃
 */
async function signOut() {
  try {
    // 1. Chrome Identity 토큰 제거
    const { accessToken } = await getTokens();
    
    if (accessToken) {
      await new Promise((resolve) => {
        chrome.identity.removeCachedAuthToken({ token: accessToken }, () => {
          resolve();
        });
      });
    }
    
    // 2. 저장된 모든 인증 정보 삭제
    await new Promise((resolve, reject) => {
      chrome.storage.local.remove(['accessToken', 'refreshToken', 'user', 'loginTime'], () => {
        if (chrome.runtime.lastError) {
          reject(chrome.runtime.lastError);
        } else {
          resolve();
        }
      });
    });
    
    console.log('로그아웃 완료');
    return { success: true };
    
  } catch (error) {
    console.error('로그아웃 실패:', error);
    throw error;
  }
}

/**
 * 로그인 상태 확인
 */
async function checkAuthStatus() {
  try {
    const { accessToken, user } = await getTokens();
    return {
      isAuthenticated: !!accessToken,
      user: user || null
    };
  } catch (error) {
    console.error('인증 상태 확인 실패:', error);
    return {
      isAuthenticated: false,
      user: null
    };
  }
}

/**
 * 토큰 갱신
 */
async function refreshAccessToken() {
  try {
    const { refreshToken } = await getTokens();
    
    if (!refreshToken) {
      throw new Error('Refresh token이 없습니다');
    }
    
    const response = await fetch(`${AUTH_CONFIG.apiBaseUrl}/oauth/reissue`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${refreshToken}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      // Refresh 실패 시 로그아웃
      await signOut();
      throw new Error('토큰 갱신 실패');
    }
    
    const data = await response.json();
    const newAccessToken = data.data?.accessToken || data.accessToken;
    const newRefreshToken = data.data?.refreshToken || data.refreshToken;
    
    // 새 토큰 저장
    const { user } = await getTokens();
    await saveTokens({
      accessToken: newAccessToken,
      refreshToken: newRefreshToken,
      user
    });
    
    return newAccessToken;
    
  } catch (error) {
    console.error('토큰 갱신 실패:', error);
    throw error;
  }
}