console.log('Content script 로드됨');

// Background script로부터 메시지 수신
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  console.log('Content script 메시지 수신:', request);
  
  if (request.action === "startAreaSelection") {
    startAreaSelection();
    sendResponse({ success: true });
  }
  
  return true;
});

// 영역 선택 기능 (향후 구현용)
function startAreaSelection() {
  console.log('영역 선택 모드 시작');
  
  // 오버레이 생성
  const overlay = document.createElement('div');
  overlay.id = 'capture-overlay';
  overlay.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.3);
    cursor: crosshair;
    z-index: 999999;
  `;
  
  const selectionBox = document.createElement('div');
  selectionBox.id = 'selection-box';
  selectionBox.style.cssText = `
    position: fixed;
    border: 2px dashed #4285f4;
    background: rgba(66, 133, 244, 0.1);
    z-index: 1000000;
    display: none;
  `;
  
  document.body.appendChild(overlay);
  document.body.appendChild(selectionBox);
  
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
  overlay.addEventListener('mouseup', (e) => {
    if (!isSelecting) return;
    
    const rect = selectionBox.getBoundingClientRect();
    
    console.log('선택 영역:', {
      x: rect.left,
      y: rect.top,
      width: rect.width,
      height: rect.height
    });
    
    // Background에 메시지 전송
    chrome.runtime.sendMessage({
      action: 'captureArea',
      area: {
        x: rect.left,
        y: rect.top,
        width: rect.width,
        height: rect.height
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
    document.removeEventListener('keydown', handleEscape);
  }
}