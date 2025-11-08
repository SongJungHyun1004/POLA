// 확장 프로그램 설치 시 실행
chrome.runtime.onInstalled.addListener(() => {
  console.log('확장 프로그램이 설치되었습니다.');
  
  // 컨텍스트 메뉴 생성
  createContextMenus();
});

// 컨텍스트 메뉴 생성
function createContextMenus() {
  // 1. 화면 캡처 메뉴
  chrome.contextMenus.create({
    id: "captureScreen",
    title: "📸 이 영역 캡처하기",
    contexts: ["page", "image", "link", "video"]
  });
  
  // 2. 텍스트 복사 메뉴 (텍스트 선택 시에만 표시)
  chrome.contextMenus.create({
    id: "copyText",
    title: "📝 선택한 텍스트 가져오기",
    contexts: ["selection"]
  });
  
  console.log('컨텍스트 메뉴가 생성되었습니다.');
}

// 컨텍스트 메뉴 클릭 이벤트
chrome.contextMenus.onClicked.addListener((info, tab) => {
  console.log('메뉴 클릭됨:', info.menuItemId);
  
  if (info.menuItemId === "captureScreen") {
    handleScreenCapture(tab);
  } else if (info.menuItemId === "copyText") {
    handleTextCapture(info, tab);
  }
});

// 화면 캡처 처리
async function handleScreenCapture(tab) {
  try {
    console.log('화면 캡처 시작...');
    
    // 현재 탭의 보이는 영역 캡처
    const screenshot = await chrome.tabs.captureVisibleTab(
      tab.windowId,
      { format: 'png' }
    );
    
    console.log('캡처 완료!');
    console.log('이미지 데이터 길이:', screenshot.length);
    console.log('페이지 정보:', {
      url: tab.url,
      title: tab.title,
      timestamp: new Date().toISOString()
    });
    
    // 알림 표시
    showNotification('캡처 완료!', '화면이 성공적으로 캡처되었습니다.');
    
    // TODO: 나중에 백엔드로 전송
    // await sendToBackend({ type: 'screenshot', data: screenshot, ... });
    
  } catch (error) {
    console.error('캡처 실패:', error);
    showNotification('캡처 실패', error.message);
  }
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
    
    // 알림 표시
    showNotification(
      '텍스트 복사 완료!', 
      `"${selectedText.substring(0, 30)}${selectedText.length > 30 ? '...' : ''}"`
    );
    
    // TODO: 나중에 백엔드로 전송
    // await sendToBackend({ type: 'text', data: selectedText, ... });
    
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

// Content script로부터 메시지 수신
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  console.log('메시지 수신:', request);
  
  if (request.action === 'captureArea') {
    console.log('선택 영역 정보:', request.area);
    // TODO: 영역 캡처 처리
  }
  
  sendResponse({ success: true });
});