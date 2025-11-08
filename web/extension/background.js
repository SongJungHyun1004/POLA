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

    if (info.menuItemId === "captureScreen") {
        // Content script 주입 및 영역 선택 시작
        await startAreaCaptureWithInjection(tab);
    } else if (info.menuItemId === "copyText") {
        handleTextCapture(info, tab);
    }
});

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

// Content script로부터 메시지 수신
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    console.log('메시지 수신:', request);

    if (request.action === 'captureArea') {
        // 영역 선택 완료 - 캡처 실행
        handleAreaCapture(request.area, sender.tab);
        sendResponse({ success: true });
    }

    return true;
});

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

            // ========== 실제 이미지 확인 ==========
            console.log('Base64 이미지 데이터:');
            console.log(response.croppedImage);

            showNotification('캡처 완료!', '선택한 영역이 캡처되었습니다.');

            // TODO: 나중에 백엔드로 전송
            // await sendToBackend({ 
            //   type: 'screenshot', 
            //   data: response.croppedImage,
            //   area: area,
            //   ... 
            // });
        }

    } catch (error) {
        console.error('영역 캡처 실패:', error);
        showNotification('캡처 실패', error.message);
    }
}