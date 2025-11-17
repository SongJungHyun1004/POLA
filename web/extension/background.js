importScripts('config.js');
importScripts('auth.js');
importScripts('apiClient.js');

const API_BASE_URL = CONFIG.API_BASE_URL;

// 확장 프로그램 설치 시 실행
chrome.runtime.onInstalled.addListener(async () => {
  console.log('확장 프로그램이 설치되었습니다.');

  // 컨텍스트 메뉴 생성
  createContextMenus();

  // 자동 로그인 시도
  const loginResult = await autoLogin();

  if (loginResult.isAuthenticated) {
    console.log('자동 로그인 성공:', loginResult.user);
  } else if (loginResult.needLogin) {
    console.log('로그인이 필요합니다');
  }
});

// 확장 프로그램 시작 시 (브라우저 재시작 등)
chrome.runtime.onStartup.addListener(async () => {
  console.log('확장 프로그램 시작됨');

  // 자동 로그인 시도
  const loginResult = await autoLogin();

  if (loginResult.isAuthenticated) {
    console.log('자동 로그인 성공:', loginResult.user);
  } else if (loginResult.needLogin) {
    console.log('로그인이 필요합니다');
  }
});

// 컨텍스트 메뉴 생성
function createContextMenus() {
  chrome.contextMenus.create({
    id: "captureScreen",
    title: "📸 영역 선택하여 캡처하기",
    contexts: ["page", "image", "link", "video"]
  });

  chrome.contextMenus.create({
    id: "uploadImage",
    title: "🖼️ 이미지를 POLA에 저장하기",
    contexts: ["image"]
  });

  chrome.contextMenus.create({
    id: "copyText",
    title: "📝 선택한 텍스트 가져오기",
    contexts: ["selection"]
  });

  console.log('컨텍스트 메뉴가 생성되었습니다.');
}

// 컨텍스트 메뉴 클릭 이벤트
chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  console.log('메뉴 클릭됨:', info.menuItemId);

  // 로그인 상태 확인
  const authStatus = await checkAuthStatus();

  if (!authStatus.isAuthenticated) {
    showNotification(
      '로그인 필요',
      'POLA에 로그인한 후 사용해주세요.'
    );
    return;
  }

  if (info.menuItemId === "captureScreen") {
    await startAreaCaptureWithInjection(tab);
  } else if (info.menuItemId === "uploadImage") {
    await handleImageUpload(info, tab);
  } else if (info.menuItemId === "copyText") {
    await handleTextCapture(info, tab);
  }
});

// ============================================
// 메시지 리스너
// ============================================
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  console.log('=====================================');
  console.log('📩 Background 메시지 리스너 실행됨');
  console.log('Action:', request.action);
  console.log('Request 전체:', request);
  console.log('Sender:', sender);
  console.log('=====================================');

  // 로그인 요청
  if (request.action === 'login') {
    console.log('📥 로그인 요청 수신 - handleLogin 호출');

    handleLogin()
      .then(result => {
        console.log('✅ handleLogin 성공:', result);
        console.log('📤 Popup으로 응답 전송:', { success: true, user: result.user });
        sendResponse({ success: true, user: result.user });
      })
      .catch(error => {
        console.error('❌ handleLogin 실패:', error);
        console.log('📤 Popup으로 에러 응답 전송:', { success: false, error: error.message });
        sendResponse({ success: false, error: error.message });
      });

    console.log('⏳ 비동기 응답 대기 중... (return true)');
    return true; // 비동기 응답을 위해 필수!
  }

  // 로그아웃 요청
  if (request.action === 'logout') {
    handleLogout().then(sendResponse).catch(error => {
      sendResponse({ success: false, error: error.message });
    });
    return true;
  }

  // 인증 상태 확인
  if (request.action === 'checkAuth') {
    checkAuthStatus().then(sendResponse);
    return true;
  }

  // 영역 캡처 완료
  if (request.action === 'captureArea') {
    handleAreaCapture(request.area, sender.tab);
    sendResponse({ success: true });
  }

  // 이미지 업로드
  if (request.action === 'uploadImage') {
    uploadImage(request.imageData, request.metadata)
      .then(result => {
        sendResponse({ success: true, data: result });
      })
      .catch(error => {
        console.error('이미지 업로드 실패:', error);
        sendResponse({ success: false, error: error.message });
      });
    return true; // 비동기 응답
  }

  // 드래그앤드롭 이미지 업로드
  if (request.action === 'uploadImageFromDrag') {
    handleDragDropImageUpload(request, sendResponse);
    return true; // 비동기 응답
  }

  return true;
});

/**
 * 구글 로그인 처리 (ID Token 방식)
 */
async function handleLogin() {
  try {
    console.log('=== 로그인 시작 ===');
    console.log('API_BASE_URL:', API_BASE_URL);

    // manifest.json에서 client_id 가져오기
    const manifest = chrome.runtime.getManifest();
    const clientId = manifest.oauth2.client_id;

    if (!clientId) {
      throw new Error('manifest.json에 oauth2.client_id가 설정되지 않았습니다.');
    }

    console.log('Client ID:', clientId);

    // 1. OAuth2 인증 플로우로 ID Token 가져오기
    console.log('1. Google OAuth2 인증 플로우 시작...');
    const redirectUrl = chrome.identity.getRedirectURL();
    console.log('Redirect URL:', redirectUrl);

    // nonce 생성 (보안을 위해)
    const nonce = Math.random().toString(36).substring(2, 15);

    const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?` +
      `client_id=${encodeURIComponent(clientId)}&` +
      `response_type=id_token&` +
      `redirect_uri=${encodeURIComponent(redirectUrl)}&` +
      `scope=${encodeURIComponent('openid email profile')}&` +
      `nonce=${nonce}`;

    console.log('Auth URL 생성 완료');

    const responseUrl = await new Promise((resolve, reject) => {
      chrome.identity.launchWebAuthFlow(
        {
          url: authUrl,
          interactive: true
        },
        (callbackUrl) => {
          if (chrome.runtime.lastError) {
            console.error('launchWebAuthFlow 오류:', chrome.runtime.lastError);
            reject(chrome.runtime.lastError);
          } else {
            console.log('✅ OAuth2 플로우 완료');
            console.log('Callback URL:', callbackUrl);
            resolve(callbackUrl);
          }
        }
      );
    });

    // 2. URL에서 ID Token 추출
    console.log('2. ID Token 추출 중...');
    const url = new URL(responseUrl);
    const hash = url.hash.substring(1); // # 제거
    const params = new URLSearchParams(hash);
    const idToken = params.get('id_token');

    if (!idToken) {
      console.error('응답 URL:', responseUrl);
      console.error('Hash:', hash);
      console.error('Params:', Object.fromEntries(params));
      throw new Error('ID Token을 가져올 수 없습니다');
    }

    console.log('✅ ID Token 획득 성공');
    console.log('ID Token 길이:', idToken.length);
    console.log('ID Token 시작:', idToken.substring(0, 50) + '...');

    // 3. ID Token에서 사용자 정보 디코딩 (JWT 디코딩)
    console.log('3. 사용자 정보 디코딩 중...');
    const payload = JSON.parse(atob(idToken.split('.')[1]));
    console.log('✅ 사용자 정보:', {
      email: payload.email,
      name: payload.name,
      picture: payload.picture
    });

    // 4. 백엔드에 ID Token 전송
    console.log('4. 백엔드 인증 요청 중...');
    const authUrl2 = `${API_BASE_URL}oauth/token`;
    console.log('요청 URL:', authUrl2);

    const authResponse = await fetch(authUrl2, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Client-Type': 'APP'
      },
      body: JSON.stringify({ idToken: idToken })
    });

    console.log('백엔드 응답 상태:', authResponse.status);

    // 응답 본문 읽기
    const responseText = await authResponse.text();
    console.log('백엔드 응답 본문:', responseText);

    if (!authResponse.ok) {
      console.error('❌ 백엔드 인증 실패');

      let errorMessage = '백엔드 인증 실패';
      try {
        const errorData = JSON.parse(responseText);
        errorMessage = errorData.message || errorData.error || errorMessage;
      } catch (e) {
        errorMessage = `${errorMessage} (${authResponse.status})`;
      }

      throw new Error(errorMessage);
    }

    // JSON 파싱
    const authData = JSON.parse(responseText);
    console.log('✅ 백엔드 인증 성공');
    console.log('응답 데이터 구조:', Object.keys(authData));

    // 토큰 추출
    const accessToken = authData.data?.accessToken || authData.accessToken;
    const refreshToken = authData.data?.refreshToken || authData.refreshToken;

    console.log('토큰 추출 결과:');
    console.log('  - accessToken:', accessToken ? '있음' : '❌ 없음');
    console.log('  - refreshToken:', refreshToken ? '있음' : '❌ 없음');

    if (!accessToken) {
      console.error('❌ Access Token 누락:', authData);
      throw new Error('서버 응답에 Access Token이 없습니다');
    }

    if (!refreshToken) {
      console.error('❌ Refresh Token 누락:', authData);
      throw new Error('서버 응답에 Refresh Token이 없습니다');
    }

    console.log('✅ 토큰 추출 성공');

    // 5. 백엔드 Access Token 검증 및 사용자 정보 가져오기
    console.log('5. Access Token 검증 중...');
    const verifyResponse = await fetch(`${API_BASE_URL}oauth/verify`, {
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    });

    if (!verifyResponse.ok) {
      throw new Error('Access Token 검증 실패');
    }

    const verifyData = await verifyResponse.json();
    console.log('✅ Token 검증 성공:', verifyData);

    // 6. Access Token 및 사용자 정보 저장
    console.log('6. 토큰 저장 중...');
    console.log('  - accessToken:', accessToken ? '있음' : '없음');
    console.log('  - refreshToken:', refreshToken ? '있음' : '없음');

    await chrome.storage.local.set({
      accessToken: accessToken,
      refreshToken: refreshToken,  // ⭐ refreshToken 추가!
      user: {
        id: verifyData.data.userId,
        email: verifyData.data.email || payload.email,
        display_name: payload.name,
        profile_image_url: payload.picture
      },
      loginTime: Date.now()
    });

    console.log('✅ 토큰 저장 완료');

    console.log('✅ 로그인 완료!');
    showNotification('로그인 성공', `${payload.name}님 환영합니다!`);

    return {
      success: true,
      user: {
        email: payload.email,
        name: payload.name,
        picture: payload.picture
      }
    };

  } catch (error) {
    console.error('=== 로그인 실패 ===');
    console.error('오류 타입:', error.constructor.name);
    console.error('오류 메시지:', error.message);
    console.error('오류 스택:', error.stack);

    showNotification('로그인 실패', error.message);
    throw error;
  }
}

/**
 * 로그아웃 처리
 */
async function handleLogout() {
  try {
    // 저장된 모든 인증 정보 삭제
    await chrome.storage.local.remove(['accessToken', 'user', 'loginTime']);

    // Chrome Identity 캐시 제거
    await chrome.identity.clearAllCachedAuthTokens();

    console.log('로그아웃 완료');
    showNotification('로그아웃', '로그아웃되었습니다.');

    return { success: true };

  } catch (error) {
    console.error('로그아웃 실패:', error);
    throw error;
  }
}

/**
 * 인증 상태 확인 (토큰 검증 포함)
 */
async function checkAuthStatus() {
  console.log('===========================================');
  console.log('🔍 Background: checkAuthStatus 호출됨');
  console.log('===========================================');

  try {
    const result = await chrome.storage.local.get(['accessToken', 'refreshToken', 'user']);

    console.log('📦 Background Storage 확인:');
    console.log('  - accessToken:', result.accessToken ? '있음' : '❌ 없음');
    console.log('  - refreshToken:', result.refreshToken ? '있음' : '❌ 없음');
    console.log('  - user:', result.user ? '있음' : '❌ 없음');

    // ⭐ accessToken이나 refreshToken이 없으면 즉시 로그아웃 상태 반환
    if (!result.accessToken || !result.refreshToken) {
      console.log('❌ 토큰 부족 - 로그아웃 상태 반환');
      console.log('===========================================');

      return {
        isAuthenticated: false,
        user: null
      };
    }

    console.log('✅ 토큰 존재 - 인증된 상태 반환');
    console.log('===========================================');

    return {
      isAuthenticated: true,
      user: result.user
    };

  } catch (error) {
    console.error('❌ 인증 상태 확인 실패:', error);
    console.log('===========================================');

    return {
      isAuthenticated: false,
      user: null
    };
  }
}
// Content script 주입 후 영역 선택 시작
async function startAreaCaptureWithInjection(tab) {
  try {
    // 1. 페이지 URL 확인 (제한된 페이지 체크)
    if (isRestrictedUrl(tab.url)) {
      showNotification(
        '캡처 불가',
        '이 페이지에서는 캡처를 사용할 수 없습니다.'
      );
      return;
    }

    // 2. Content script가 이미 로드되어 있는지 확인
    let isContentScriptLoaded = false;
    try {
      const response = await Promise.race([
        chrome.tabs.sendMessage(tab.id, { action: "ping" }),
        new Promise((_, reject) =>
          setTimeout(() => reject(new Error('timeout')), 300)
        )
      ]);
      isContentScriptLoaded = response?.pong === true;
    } catch (e) {
      isContentScriptLoaded = false;
    }

    // 3. Content script가 없으면 주입
    if (!isContentScriptLoaded) {
      console.log('Content script 주입 중...');
      await chrome.scripting.executeScript({
        target: { tabId: tab.id },
        files: ['content.js']
      });

      // 주입 후 잠시 대기
      await new Promise(resolve => setTimeout(resolve, 50));
    }

    // 4. 영역 선택 시작 요청
    await chrome.tabs.sendMessage(tab.id, {
      action: "startAreaSelection"
    });

  } catch (error) {
    console.error('영역 캡처 시작 실패:', error);
    showNotification('오류', '캡처를 시작할 수 없습니다: ' + error.message);
  }
}

// 제한된 URL 체크
function isRestrictedUrl(url) {
  const restrictedPatterns = [
    /^chrome:\/\//,
    /^chrome-extension:\/\//,
    /^edge:\/\//,
    /^about:/,
    /^data:/,
    /^file:\/\//,
    /chrome\.google\.com\/webstore/,
    /microsoftedge\.microsoft\.com/
  ];

  return restrictedPatterns.some(pattern => pattern.test(url));
}

// 텍스트 캡처 처리
async function handleTextCapture(info, tab) {
  try {
    const selectedText = info.selectionText;

    console.log('텍스트 캡처 완료!');
    console.log('선택된 텍스트:', selectedText);
    console.log('페이지 정보:', {
      url: tab.url,
      title: tab.title,
      timestamp: new Date().toISOString()
    });

    try {
      showNotification('저장 중...', '텍스트를 POLA에 저장하고 있습니다.');

      // 텍스트를 Blob으로 변환
      const textBlob = new Blob([selectedText], { type: 'text/plain; charset=utf-8' });
      const fileSize = textBlob.size;

      console.log('텍스트 Blob 생성 완료, 크기:', fileSize, 'bytes');

      // 토큰 가져오기
      const { accessToken } = await chrome.storage.local.get(['accessToken']);

      if (!accessToken) {
        throw new Error('로그인이 필요합니다.');
      }

      // 1단계: S3 Presigned URL 생성
      console.log('1단계: S3 업로드 URL 생성 중...');
      const timestamp = Date.now();
      const fileName = `text_${timestamp}.txt`;

      const presignedResponse = await apiRequest(
        `s3/presigned/upload?fileName=${encodeURIComponent(fileName)}`,
        {
          method: 'GET'
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

      console.log('✅ 1단계 완료 - Upload URL:', uploadUrl.substring(0, 100) + '...');
      console.log('✅ File Key:', fileKey);

      // 2단계: S3에 직접 업로드
      console.log('2단계: S3에 텍스트 업로드 중...');

      const s3UploadResponse = await fetch(uploadUrl, {
        method: 'PUT',
        headers: {
          'Content-Type': 'text/plain; charset=utf-8'
        },
        body: textBlob
      });

      if (!s3UploadResponse.ok) {
        console.error('S3 업로드 실패:', s3UploadResponse.status, s3UploadResponse.statusText);
        throw new Error('S3 업로드 실패');
      }

      console.log('✅ 2단계 완료 - S3 업로드 성공');

      // 3단계: DB에 파일 메타데이터 저장
      console.log('3단계: 파일 정보 저장 중...');

      // originUrl 추출 (? 앞부분까지)
      const originUrl = uploadUrl.split('?')[0];

      const completeResponse = await fetch(`${API_BASE_URL}files/complete`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          key: fileKey,
          type: 'text/plain',
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
      console.log('✅ 3단계 완료 - 파일 등록 성공:', completeData);

      // 업로드 성공!
      const preview = selectedText.length > 30
        ? selectedText.substring(0, 30) + '...'
        : selectedText;

      showNotification(
        '✨ 저장 완료!',
        `"${preview}" 가 POLA에 저장되었습니다.`
      );

      console.log('🎉 전체 업로드 플로우 완료!');
      console.log('파일 ID:', completeData.data.id);
      console.log('저장 URL:', completeData.data.originUrl);

      // 4단계: 파일 분류 (백그라운드에서 실행)
      triggerPostProcess(completeData.data.id, accessToken);

    } catch (uploadError) {
      console.error('❌ 저장 실패:', uploadError);
      showNotification(
        '저장 실패',
        uploadError.message || '텍스트 저장 중 오류가 발생했습니다.'
      );
    }

  } catch (error) {
    console.error('텍스트 저장 실패:', error);
    showNotification('저장 실패', error.message);
  }
}

// 알림 표시
function showNotification(title, message) {
  chrome.notifications.create({
    type: 'basic',
    iconUrl: 'icons/icon48.png',
    title: title,
    message: message,
    priority: 2
  });
}

// 선택한 영역 캡처 처리
async function handleAreaCapture(area, tab) {
  try {
    console.log('선택 영역 캡처 시작:', area);

    // 1. 전체 화면 캡처
    const fullScreenshot = await chrome.tabs.captureVisibleTab(
      tab.windowId,
      { format: 'png' }
    );

    console.log('전체 화면 캡처 완료');

    // 2. Content script에 이미지 크롭 요청
    const response = await chrome.tabs.sendMessage(tab.id, {
      action: 'cropImage',
      imageData: fullScreenshot,
      area: area
    });

    if (response.success) {
      console.log('영역 캡처 완료!');
      console.log('크롭된 이미지 데이터 길이:', response.croppedImage.length);
      console.log('페이지 정보:', {
        url: tab.url,
        title: tab.title,
        area: area,
        timestamp: new Date().toISOString()
      });

      try {
        showNotification('업로드 중...', '이미지를 POLA에 업로드하고 있습니다.');

        // Base64를 Blob으로 변환
        const base64Data = response.croppedImage.split(',')[1];
        const byteCharacters = atob(base64Data);
        const byteNumbers = new Array(byteCharacters.length);

        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }

        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: 'image/png' });
        const fileSize = blob.size;

        console.log('이미지 Blob 생성 완료, 크기:', fileSize, 'bytes');

        // 토큰 가져오기
        const { accessToken } = await chrome.storage.local.get(['accessToken']);

        if (!accessToken) {
          throw new Error('로그인이 필요합니다.');
        }

        // 1단계: S3 Presigned URL 생성
        console.log('1단계: S3 업로드 URL 생성 중...');
        const timestamp = Date.now();
        const fileName = `capture_${timestamp}.png`;

        const presignedResponse = await fetch(
          `${API_BASE_URL}s3/presigned/upload?fileName=${encodeURIComponent(fileName)}`,
          {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${accessToken}`
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

        console.log('✅ 1단계 완료 - Upload URL:', uploadUrl.substring(0, 100) + '...');
        console.log('✅ File Key:', fileKey);

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
          console.error('S3 업로드 실패:', s3UploadResponse.status, s3UploadResponse.statusText);
          throw new Error('S3 업로드 실패');
        }

        console.log('✅ 2단계 완료 - S3 업로드 성공');

        // 3단계: DB에 파일 메타데이터 저장
        console.log('3단계: 파일 정보 저장 중...');

        // originUrl 추출 (? 앞부분까지)
        const originUrl = uploadUrl.split('?')[0];

        const completeResponse = await fetch(`${API_BASE_URL}files/complete`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
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
        console.log('✅ 3단계 완료 - 파일 등록 성공:', completeData);

        // 업로드 성공!
        showNotification(
          '✨ 업로드 완료!',
          '이미지가 POLA에 성공적으로 저장되었습니다.'
        );

        console.log('🎉 전체 업로드 플로우 완료!');
        console.log('파일 ID:', completeData.data.id);
        console.log('저장 URL:', completeData.data.originUrl);

        // 4단계: 파일 분류 (백그라운드에서 실행)
        triggerPostProcess(completeData.data.id, accessToken);

      } catch (uploadError) {
        console.error('❌ 업로드 실패:', uploadError);
        showNotification(
          '업로드 실패',
          uploadError.message || '이미지 업로드 중 오류가 발생했습니다.'
        );
      }
    }

  } catch (error) {
    console.error('영역 캡처 실패:', error);
    showNotification('캡처 실패', error.message);
  }
}

/**
 * 파일 분류 처리 (백그라운드 실행)
 */
async function triggerPostProcess(fileId, accessToken) {
  try {
    console.log(`4단계: 파일 분류 시작 (File ID: ${fileId})...`);

    const postProcessResponse = await fetch(
      `${API_BASE_URL}files/${fileId}/post-process`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`
        }
      }
    );

    if (postProcessResponse.ok) {
      const result = await postProcessResponse.json();
      console.log('✅ 4단계 완료 - 파일 분류 성공:', result);
    } else {
      console.warn('⚠️ 파일 분류 실패:', postProcessResponse.status);
    }
  } catch (error) {
    // 분류 실패는 사용자에게 알리지 않음 (백그라운드 작업)
    console.error('⚠️ 파일 분류 오류:', error);
  }
}

/**
 * 이미지 업로드 처리
 */
async function handleImageUpload(info, tab) {
  try {
    const imageUrl = info.srcUrl;

    if (!imageUrl) {
      throw new Error('이미지 URL을 찾을 수 없습니다.');
    }

    console.log('이미지 업로드 시작:', imageUrl);
    showNotification('업로드 중...', '이미지를 POLA에 업로드하고 있습니다.');

    // 1. 이미지 다운로드
    console.log('1단계: 이미지 다운로드 중...');
    const imageResponse = await fetch(imageUrl);

    if (!imageResponse.ok) {
      throw new Error('이미지를 가져올 수 없습니다.');
    }

    const blob = await imageResponse.blob();
    const fileSize = blob.size;

    // 이미지 타입 확인
    const contentType = blob.type || 'image/png';

    console.log('✅ 이미지 다운로드 완료, 크기:', fileSize, 'bytes, 타입:', contentType);

    // ⚠️ 이미지 타입 검증 (PNG, JPEG만 허용)
    const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg'];
    const blobType = contentType.toLowerCase();

    console.log('🔍 타입 검증 중...');
    console.log('Content Type (소문자):', blobType);
    console.log('허용된 타입:', allowedTypes);
    console.log('검증 결과:', allowedTypes.includes(blobType));

    if (!allowedTypes.includes(blobType)) {
      const displayType = contentType.split('/')[1]?.toUpperCase() || '알 수 없음';
      const errorMessage = `지원하지 않는 이미지 형식입니다.\n현재 형식: ${displayType}\n지원 형식: PNG, JPEG만 가능합니다.`;

      console.warn('⚠️ 지원하지 않는 이미지 타입:', contentType);
      console.warn('업로드 차단됨');

      showNotification(
        'POLA - 이미지 형식 오류',
        errorMessage
      );

      return; // 함수 종료
    }

    console.log('✅ 이미지 타입 검증 통과:', contentType);
    // 토큰 가져오기
    const { accessToken } = await chrome.storage.local.get(['accessToken']);

    if (!accessToken) {
      throw new Error('로그인이 필요합니다.');
    }

    // 2단계: S3 Presigned URL 생성
    console.log('2단계: S3 업로드 URL 생성 중...');
    const timestamp = Date.now();
    const extension = contentType.split('/')[1] || 'png';
    const fileName = `image_${timestamp}.${extension}`;

    const presignedResponse = await fetch(
      `${API_BASE_URL}s3/presigned/upload?fileName=${encodeURIComponent(fileName)}`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`
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

    console.log('✅ 2단계 완료 - Upload URL 획득');

    // 3단계: S3에 직접 업로드
    console.log('3단계: S3에 이미지 업로드 중...');

    const s3UploadResponse = await fetch(uploadUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': contentType
      },
      body: blob
    });

    if (!s3UploadResponse.ok) {
      console.error('S3 업로드 실패:', s3UploadResponse.status, s3UploadResponse.statusText);
      throw new Error('S3 업로드 실패');
    }

    console.log('✅ 3단계 완료 - S3 업로드 성공');

    // 4단계: DB에 파일 메타데이터 저장
    console.log('4단계: 파일 정보 저장 중...');

    const originUrl = uploadUrl.split('?')[0];

    const completeResponse = await fetch(`${API_BASE_URL}files/complete`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        key: fileKey,
        type: contentType,
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
    console.log('✅ 4단계 완료 - 파일 등록 성공:', completeData);

    // 업로드 성공!
    showNotification(
      '✨ 업로드 완료!',
      '이미지가 POLA에 성공적으로 저장되었습니다.'
    );

    console.log('🎉 전체 업로드 플로우 완료!');
    console.log('파일 ID:', completeData.data.id);
    console.log('저장 URL:', completeData.data.originUrl);

    // 5단계: 파일 분류 (백그라운드에서 실행)
    triggerPostProcess(completeData.data.id, accessToken);

  } catch (error) {
    console.error('이미지 업로드 실패:', error);
    showNotification(
      '업로드 실패',
      error.message || '이미지 업로드 중 오류가 발생했습니다.'
    );
  }
}

/**
 * 드래그앤드롭 이미지 업로드 처리
 */
async function handleDragDropImageUpload(request, sendResponse) {
  try {
    console.log('드래그앤드롭 이미지 업로드 시작:', request.imageUrl);

    // 1. 이미지 URL을 Base64로 변환
    const response = await fetch(request.imageUrl);
    const blob = await response.blob();

    // 🔍 파일 타입 확인
    console.log('=== 이미지 정보 ===');
    console.log('파일 타입:', blob.type);
    console.log('파일 크기:', blob.size, 'bytes');
    console.log('원본 URL:', request.imageUrl);
    console.log('==================');

    // ⚠️ 이미지 타입 검증 (PNG, JPEG만 허용)
    const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg'];

    if (!allowedTypes.includes(blob.type.toLowerCase())) {
      const displayType = blob.type.split('/')[1]?.toUpperCase() || '알 수 없음';
      const errorMessage = `지원하지 않는 이미지 형식입니다.\n현재 형식: ${displayType}\n지원 형식: PNG, JPEG만 가능합니다.`;

      console.warn('⚠️ 지원하지 않는 이미지 타입:', blob.type);

      chrome.notifications.create({
        type: 'basic',
        iconUrl: 'icons/icon128.png',
        title: 'POLA - 이미지 형식 오류',
        message: errorMessage,
        priority: 2
      });

      sendResponse({
        success: false,
        error: errorMessage
      });
      return;
    }

    console.log('✅ 이미지 타입 검증 통과:', blob.type);

    const base64 = await new Promise((resolve) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        // Base64 데이터의 MIME 타입도 확인
        const mimeType = reader.result.split(';')[0].split(':')[1];
        console.log('Base64 MIME Type:', mimeType);
        resolve(reader.result);
      };
      reader.readAsDataURL(blob);
    });

    console.log('이미지 Base64 변환 완료');

    // 2. 이미지 업로드
    const uploadResult = await uploadImage(base64, {
      title: request.pageTitle || '드래그 업로드',
      url: request.pageUrl
    });

    console.log('✅ 드래그앤드롭 업로드 성공:', uploadResult);

    chrome.notifications.create({
      type: 'basic',
      iconUrl: 'icons/icon128.png',
      title: 'POLA - 이미지 저장 완료',
      message: '드래그한 이미지가 성공적으로 저장되었습니다.',
      priority: 2
    });

    sendResponse({
      success: true,
      data: uploadResult
    });

  } catch (error) {
    console.error('❌ 드래그앤드롭 업로드 실패:', error);

    chrome.notifications.create({
      type: 'basic',
      iconUrl: 'icons/icon128.png',
      title: 'POLA - 이미지 저장 실패',
      message: error.message || '이미지 저장 중 오류가 발생했습니다.',
      priority: 2
    });

    sendResponse({
      success: false,
      error: error.message
    });
  }
}