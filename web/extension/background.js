importScripts('config.js');

const API_BASE_URL = CONFIG.API_BASE_URL;

// 확장 프로그램 설치 시 실행
chrome.runtime.onInstalled.addListener(() => {
    console.log('확장 프로그램이 설치되었습니다.');
    createContextMenus();
});

// 컨텍스트 메뉴 생성
function createContextMenus() {
    chrome.contextMenus.create({
        id: "captureScreen",
        title: "📸 영역 선택하여 캡처하기",
        contexts: ["page", "image", "link", "video"]
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
            'Pola에 로그인한 후 사용해주세요.'
        );
        return;
    }

    if (info.menuItemId === "captureScreen") {
        await startAreaCaptureWithInjection(tab);
    } else if (info.menuItemId === "copyText") {
        await handleTextCapture(info, tab);
    }
});

// ============================================
// 메시지 리스너
// ============================================
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  console.log('메시지 수신:', request);

  // 로그인 요청
  if (request.action === 'login') {
    handleLogin().then(sendResponse).catch(error => {
      sendResponse({ success: false, error: error.message });
    });
    return true;
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

  return true;
});

/**
 * 구글 로그인 처리 (디버깅 강화)
 */
async function handleLogin() {
  try {
    console.log('=== 로그인 시작 ===');
    console.log('API_BASE_URL:', API_BASE_URL);
    
    // 1. Chrome Identity API로 구글 토큰 획득
    console.log('1. Chrome Identity API 호출 중...');
    const googleToken = await new Promise((resolve, reject) => {
      chrome.identity.getAuthToken({ interactive: true }, (token) => {
        if (chrome.runtime.lastError) {
          console.error('Chrome Identity 오류:', chrome.runtime.lastError);
          reject(chrome.runtime.lastError);
        } else {
          console.log('✅ 구글 토큰 획득 성공');
          console.log('토큰 길이:', token?.length);
          resolve(token);
        }
      });
    });
    
    // 2. 구글 사용자 정보 가져오기
    console.log('2. 구글 사용자 정보 요청 중...');
    const userInfoResponse = await fetch('https://www.googleapis.com/oauth2/v2/userinfo', {
      headers: {
        'Authorization': `Bearer ${googleToken}`
      }
    });
    
    if (!userInfoResponse.ok) {
      console.error('❌ 사용자 정보 가져오기 실패:', userInfoResponse.status);
      throw new Error(`사용자 정보 가져오기 실패: ${userInfoResponse.status}`);
    }
    
    const userInfo = await userInfoResponse.json();
    console.log('✅ 사용자 정보 획득:', {
      id: userInfo.id,
      email: userInfo.email,
      name: userInfo.name
    });
    
    // 3. 백엔드에 구글 토큰 전송하여 JWT 토큰 받기
    console.log('3. 백엔드 인증 요청 중...');
    const authUrl = `${API_BASE_URL}oauth/token`;
    console.log('요청 URL:', authUrl);
    console.log('요청 Body:', { idToken: '(토큰 길이: ' + googleToken.length + ')' });
    
    const authResponse = await fetch(authUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Client-Type': 'WEB'
      },
      body: JSON.stringify({ idToken: googleToken })
    });
    
    console.log('백엔드 응답 상태:', authResponse.status);
    console.log('백엔드 응답 헤더:', Object.fromEntries(authResponse.headers.entries()));
    
    // 응답 본문 읽기 (에러 디버깅용)
    const responseText = await authResponse.text();
    console.log('백엔드 응답 본문:', responseText);
    
    if (!authResponse.ok) {
      console.error('❌ 백엔드 인증 실패');
      console.error('상태 코드:', authResponse.status);
      console.error('응답 내용:', responseText);
      
      // 상세 오류 메시지
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
    
    if (!accessToken || !refreshToken) {
      console.error('❌ 토큰 누락:', {
        hasAccessToken: !!accessToken,
        hasRefreshToken: !!refreshToken,
        authData: authData
      });
      throw new Error('서버 응답에 토큰이 없습니다');
    }
    
    console.log('✅ 토큰 추출 성공');
    
    // 4. JWT 토큰 및 사용자 정보 저장
    console.log('4. 토큰 저장 중...');
    await chrome.storage.local.set({
      accessToken: accessToken,
      refreshToken: refreshToken,
      user: {
        id: userInfo.id,
        email: userInfo.email,
        display_name: userInfo.name,
        profile_image_url: userInfo.picture
      },
      loginTime: Date.now()
    });
    
    console.log('✅ 로그인 완료!');
    showNotification('로그인 성공', `${userInfo.name}님 환영합니다!`);
    
    return { 
      success: true, 
      user: {
        email: userInfo.email,
        name: userInfo.name,
        picture: userInfo.picture
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
    // Chrome Identity 캐시 제거
    const result = await chrome.storage.local.get(['accessToken']);
    if (result.accessToken) {
      await new Promise((resolve) => {
        chrome.identity.removeCachedAuthToken({ token: result.accessToken }, resolve);
      });
    }
    
    // 저장된 모든 인증 정보 삭제
    await chrome.storage.local.remove(['accessToken', 'refreshToken', 'user', 'loginTime']);
    
    console.log('로그아웃 완료');
    showNotification('로그아웃', '로그아웃되었습니다.');
    
    return { success: true };
    
  } catch (error) {
    console.error('로그아웃 실패:', error);
    throw error;
  }
}

/**
 * 인증 상태 확인
 */
async function checkAuthStatus() {
  const result = await chrome.storage.local.get(['accessToken', 'user']);
  return {
    isAuthenticated: !!result.accessToken,
    user: result.user || null
  };
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
            const response = await chrome.tabs.sendMessage(tab.id, {
                action: "ping"
            });
            isContentScriptLoaded = response?.pong === true;
        } catch (e) {
            // Content script 없음
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
            await new Promise(resolve => setTimeout(resolve, 100));
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

        showNotification(
            '텍스트 복사 완료!',
            `"${selectedText.substring(0, 30)}${selectedText.length > 30 ? '...' : ''}"`
        );

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

            showNotification('캡처 완료!', '선택한 영역이 캡처되었습니다.');
        }

    } catch (error) {
        console.error('영역 캡처 실패:', error);
        showNotification('캡처 실패', error.message);
    }
}