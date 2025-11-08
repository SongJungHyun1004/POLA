console.log('Content script 로드됨');

// Background script로부터 메시지 수신
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  console.log('Content script 메시지 수신:', request);
  
  if (request.action === "startAreaSelection") {
    startAreaSelection();
    sendResponse({ success: true });
  } else if (request.action === "cropImage") {
    // 이미지 크롭 처리
    cropImage(request.imageData, request.area)
      .then(croppedImage => {
        sendResponse({ success: true, croppedImage });
      })
      .catch(error => {
        console.error('크롭 실패:', error);
        sendResponse({ success: false, error: error.message });
      });
    return true; // 비동기 응답을 위해 필요
  }
  
  return true;
});

// 영역 선택 UI
function startAreaSelection() {
  console.log('영역 선택 모드 시작');
  
  // 기존 오버레이가 있으면 제거
  const existingOverlay = document.getElementById('capture-overlay');
  if (existingOverlay) {
    existingOverlay.remove();
  }
  
  // 반투명 오버레이 생성
  const overlay = document.createElement('div');
  overlay.id = 'capture-overlay';
  overlay.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
    cursor: crosshair;
    z-index: 2147483647;
  `;
  
  // 선택 박스
  const selectionBox = document.createElement('div');
  selectionBox.id = 'selection-box';
  selectionBox.style.cssText = `
    position: fixed;
    border: 2px solid #4285f4;
    background: rgba(66, 133, 244, 0.1);
    z-index: 2147483648;
    display: none;
    box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.3);
  `;
  
  // 안내 텍스트
  const guideText = document.createElement('div');
  guideText.style.cssText = `
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 16px 24px;
    border-radius: 8px;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    font-size: 14px;
    z-index: 2147483649;
    pointer-events: none;
  `;
  guideText.textContent = '마우스를 드래그하여 캡처할 영역을 선택하세요 (ESC로 취소)';
  
  document.body.appendChild(overlay);
  document.body.appendChild(selectionBox);
  document.body.appendChild(guideText);
  
  // 2초 후 안내 텍스트 제거
  setTimeout(() => {
    guideText.remove();
  }, 2000);
  
  let startX, startY, isSelecting = false;
  
  // 마우스 다운
  overlay.addEventListener('mousedown', (e) => {
    isSelecting = true;
    startX = e.clientX;
    startY = e.clientY;
    
    selectionBox.style.left = startX + 'px';
    selectionBox.style.top = startY + 'px';
    selectionBox.style.width = '0px';
    selectionBox.style.height = '0px';
    selectionBox.style.display = 'block';
    
    // 안내 텍스트가 아직 있으면 제거
    guideText.remove();
  });
  
  // 마우스 이동
  overlay.addEventListener('mousemove', (e) => {
    if (!isSelecting) return;
    
    const currentX = e.clientX;
    const currentY = e.clientY;
    
    const width = Math.abs(currentX - startX);
    const height = Math.abs(currentY - startY);
    const left = Math.min(startX, currentX);
    const top = Math.min(startY, currentY);
    
    selectionBox.style.left = left + 'px';
    selectionBox.style.top = top + 'px';
    selectionBox.style.width = width + 'px';
    selectionBox.style.height = height + 'px';
  });
  
  // 마우스 업
  overlay.addEventListener('mouseup', async (e) => {
    if (!isSelecting) return;
    
    const rect = selectionBox.getBoundingClientRect();
    
    // 너무 작은 영역은 무시
    if (rect.width < 10 || rect.height < 10) {
      console.log('선택 영역이 너무 작습니다');
      cleanup();
      return;
    }
    
    console.log('선택 영역:', {
      x: rect.left,
      y: rect.top,
      width: rect.width,
      height: rect.height
    });
    
    // devicePixelRatio 고려 (레티나 디스플레이 등)
    const dpr = window.devicePixelRatio || 1;
    
    // Background에 메시지 전송
    chrome.runtime.sendMessage({
      action: 'captureArea',
      area: {
        x: rect.left * dpr,
        y: rect.top * dpr,
        width: rect.width * dpr,
        height: rect.height * dpr,
        dpr: dpr
      }
    });
    
    // UI 정리
    cleanup();
  });
  
  // ESC 키로 취소
  const handleEscape = (e) => {
    if (e.key === 'Escape') {
      console.log('영역 선택 취소됨');
      cleanup();
    }
  };
  
  document.addEventListener('keydown', handleEscape);
  
  function cleanup() {
    overlay.remove();
    selectionBox.remove();
    guideText.remove();
    document.removeEventListener('keydown', handleEscape);
    isSelecting = false;
  }
}

// 이미지 크롭 함수
async function cropImage(imageDataUrl, area) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    
    img.onload = () => {
      try {
        // Canvas 생성
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        
        // 크롭할 영역 크기로 캔버스 설정
        canvas.width = area.width;
        canvas.height = area.height;
        
        // 이미지의 선택 영역만 그리기
        ctx.drawImage(
          img,
          area.x, area.y,           // 소스 x, y
          area.width, area.height,  // 소스 width, height
          0, 0,                     // 대상 x, y
          area.width, area.height   // 대상 width, height
        );
        
        // Canvas를 Base64로 변환
        const croppedImageData = canvas.toDataURL('image/png');
        
        console.log('이미지 크롭 완료');
        console.log('원본 크기:', img.width, 'x', img.height);
        console.log('크롭 영역:', area.width, 'x', area.height);
        
        resolve(croppedImageData);
        
      } catch (error) {
        reject(error);
      }
    };
    
    img.onerror = () => {
      reject(new Error('이미지 로드 실패'));
    };
    
    img.src = imageDataUrl;
  });
}