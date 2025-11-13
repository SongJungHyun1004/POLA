// apiClient.js - 크롬 확장 프로그램용 API 클라이언트


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
    let response = await fetch(CONFIG.API_BASE_URL + url, {
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
        response = await fetch(CONFIG.API_BASE_URL + url, {
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
  const response = await fetch(CONFIG.API_BASE_URL + '/oauth/reissue', {
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
/**
 * 이미지 업로드 API (S3 직접 업로드 방식)
 */
async function uploadImage(imageData, metadata = {}) {
  try {
    console.log('이미지 업로드 시작...');
    
    // Base64를 Blob으로 변환
    const blob = base64ToBlob(imageData);
    const fileSize = blob.size;
    
    console.log('이미지 Blob 생성 완료, 크기:', fileSize, 'bytes');
    
    // 토큰 가져오기
    const tokens = await getStoredTokens();
    
    if (!tokens.accessToken) {
      throw new Error('로그인이 필요합니다.');
    }
    
    // 1단계: S3 Presigned URL 생성
    console.log('1단계: S3 업로드 URL 생성 중...');
    const timestamp = Date.now();
    const fileName = metadata.title || `upload_${timestamp}.png`;
    
    const presignedResponse = await fetch(
      `${CONFIG.API_BASE_URL}s3/presigned/upload?fileName=${encodeURIComponent(fileName)}`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${tokens.accessToken}`
        }
      }
    );
    
    if (!presignedResponse.ok) {
      const errorText = await presignedResponse.text();
      console.error('Presigned URL 생성 실패:', errorText);
      throw new Error('업로드 URL 생성 실패');
    }
    
    const presignedData = await presignedResponse.json();
    const uploadUrl = presignedData.data.url;
    const fileKey = presignedData.data.key;
    
    console.log('✅ 1단계 완료 - Upload URL 획득');
    
    // 2단계: S3에 직접 업로드
    console.log('2단계: S3에 이미지 업로드 중...');
    
    const s3UploadResponse = await fetch(uploadUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': 'image/png'
      },
      body: blob
    });
    
    if (!s3UploadResponse.ok) {
      console.error('S3 업로드 실패:', s3UploadResponse.status);
      throw new Error('S3 업로드 실패');
    }
    
    console.log('✅ 2단계 완료 - S3 업로드 성공');
    
    // 3단계: DB에 파일 메타데이터 저장
    console.log('3단계: 파일 정보 저장 중...');
    
    const originUrl = uploadUrl.split('?')[0];
    
    const completeResponse = await fetch(`${CONFIG.API_BASE_URL}files/complete`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${tokens.accessToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        key: fileKey,
        type: 'image/png',
        fileSize: fileSize,
        originUrl: originUrl,
        platform: 'WEB'
      })
    });
    
    if (!completeResponse.ok) {
      const errorText = await completeResponse.text();
      console.error('파일 등록 실패:', errorText);
      throw new Error('파일 등록 실패');
    }
    
    const completeData = await completeResponse.json();
    console.log('✅ 3단계 완료 - 파일 등록 성공');
    
    // 4단계: 파일 분류 (백그라운드)
    triggerPostProcessInBackground(completeData.data.id, tokens.accessToken);
    
    return completeData;
    
  } catch (error) {
    console.error('이미지 업로드 오류:', error);
    throw error;
  }
}

/**
 * 파일 분류 백그라운드 처리
 */
async function triggerPostProcessInBackground(fileId, accessToken) {
  try {
    console.log(`파일 분류 시작 (File ID: ${fileId})...`);
    
    const response = await fetch(
      `${CONFIG.API_BASE_URL}files/${fileId}/post-process`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`
        }
      }
    );
    
    if (response.ok) {
      console.log('✅ 파일 분류 성공');
    } else {
      console.warn('⚠️ 파일 분류 실패');
    }
  } catch (error) {
    console.error('⚠️ 파일 분류 오류:', error);
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