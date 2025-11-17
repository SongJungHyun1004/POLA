console.log('Content script 로드됨');

// Background script로부터 메시지 수신
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  console.log('Content script 메시지 수신:', request);

  // Ping 응답 (content script 로드 확인용)
  if (request.action === "ping") {
    sendResponse({ pong: true });
    return true;
  }

  if (request.action === "startAreaSelection") {
    startAreaSelection();
    sendResponse({ success: true });
    return true;
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
    background: transparent;
    cursor: crosshair;
    z-index: 2147483647;
  `;

  // 선택 박스
  const selectionBox = document.createElement('div');
  selectionBox.id = 'selection-box';
  selectionBox.style.cssText = `
    position: fixed;
    border: 2px solid #B0804C;
    background: transparent;
    z-index: 2147483648;
    display: none;
    box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.3);
  `;

  // 안내 텍스트 (초기)
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
  guideText.textContent = '마우스를 드래그하여 캡처할 영역을 선택하세요';

  // ESC 안내 (상단 고정)
  const escGuide = document.createElement('div');
  escGuide.id = 'esc-guide';
  escGuide.style.cssText = `
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(0, 0, 0, 0.85);
    color: white;
    padding: 12px 20px;
    border-radius: 6px;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    font-size: 13px;
    z-index: 2147483649;
    pointer-events: none;
    display: flex;
    align-items: center;
    gap: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  `;
  escGuide.innerHTML = `
    <span style="background: rgba(255, 255, 255, 0.2); padding: 4px 8px; border-radius: 4px; font-weight: 600;">ESC</span>
    <span>취소</span>
  `;

  document.body.appendChild(overlay);
  document.body.appendChild(selectionBox);
  document.body.appendChild(guideText);
  document.body.appendChild(escGuide);

  // 2초 후 중앙 안내 텍스트만 제거 (ESC 안내는 유지)
  setTimeout(() => {
    if (guideText.parentNode) {
      guideText.remove();
    }
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

    // 중앙 안내 텍스트만 제거 (ESC 안내는 유지)
    if (guideText.parentNode) {
      guideText.remove();
    }
  });

  // 마우스 이동
  const handleMouseMove = (e) => {
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
  };

  // overlay 대신 document에 등록
  document.addEventListener('mousemove', handleMouseMove);

  // 마우스 업
  const handleMouseUp = async (e) => {
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

    const dpr = window.devicePixelRatio || 1;

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

    cleanup();
  };

  overlay.addEventListener('mouseup', handleMouseUp);
  document.addEventListener('mouseup', handleMouseUp);

  // ESC 키로 취소
  const handleEscape = (e) => {
    if (e.key === 'Escape') {
      console.log('영역 선택 취소됨');
      cleanup();
    }
  };

  document.addEventListener('keydown', handleEscape);

  function cleanup() {
    if (overlay.parentNode) overlay.remove();
    if (selectionBox.parentNode) selectionBox.remove();
    if (guideText.parentNode) guideText.remove();
    if (escGuide.parentNode) escGuide.remove();
    document.removeEventListener('keydown', handleEscape);
    document.removeEventListener('mousemove', handleMouseMove);
    document.removeEventListener('mouseup', handleMouseUp);
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
        const borderWidth = 2; // 테두리 두께
        const adjustedX = area.x + borderWidth;
        const adjustedY = area.y + borderWidth;
        const adjustedWidth = area.width - (borderWidth * 2);
        const adjustedHeight = area.height - (borderWidth * 2);

        // Canvas 크기도 테두리 제외한 크기로
        canvas.width = adjustedWidth;
        canvas.height = adjustedHeight;

        ctx.drawImage(
          img,
          adjustedX, adjustedY,           // 소스 x, y (테두리 제외)
          adjustedWidth, adjustedHeight,  // 소스 width, height (테두리 제외)
          0, 0,                           // 대상 x, y
          adjustedWidth, adjustedHeight   // 대상 width, height
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

// ========================================
// 이미지 드래그앤드롭 업로드 기능
// ========================================

let dropZoneDialog = null;
let draggedImageSrc = null;
let draggedText = null;
let dragType = null;
let dragStartedInCenter = false;


// 드래그 종료 시
// 이미지 및 텍스트 드래그 이벤트 리스너
document.addEventListener('dragstart', (e) => {
  console.log('🎯 dragstart 이벤트:', e.target.tagName);

  // 1. 이미지 드래그 감지
  if (e.target.tagName === 'IMG') {
    dragType = 'image';
    draggedImageSrc = e.target.src;
    draggedText = null;

    console.log('📸 이미지 드래그 시작:', draggedImageSrc);

    // 이미지와 드롭존(중앙 위치)이 겹치는지 확인
    const imgRect = e.target.getBoundingClientRect();
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;

    // 드롭존의 예상 크기와 위치 (중앙)
    const dropZoneWidth = 400;
    const dropZoneHeight = 300;
    const dropZoneLeft = (windowWidth - dropZoneWidth) / 2;
    const dropZoneRight = (windowWidth + dropZoneWidth) / 2;
    const dropZoneTop = (windowHeight - dropZoneHeight) / 2;
    const dropZoneBottom = (windowHeight + dropZoneHeight) / 2;

    // 이미지와 드롭존이 겹치는지 확인 (사각형 충돌 감지)
    const isOverlapping = !(
      imgRect.right < dropZoneLeft ||
      imgRect.left > dropZoneRight ||
      imgRect.bottom < dropZoneTop ||
      imgRect.top > dropZoneBottom
    );

    dragStartedInCenter = isOverlapping;

    console.log('🔍 겹침 감지:', {
      이미지위치: {
        left: Math.round(imgRect.left),
        right: Math.round(imgRect.right),
        top: Math.round(imgRect.top),
        bottom: Math.round(imgRect.bottom)
      },
      겹침여부: isOverlapping,
      드롭존위치: isOverlapping ? '오른쪽' : '중앙'
    });

    // 드롭존 다이얼로그 표시
    showDropZoneDialog('image');
  }

  // 2. 텍스트 선택 드래그 감지
  const selectedText = window.getSelection().toString().trim();
  if (selectedText && !draggedImageSrc) {
    dragType = 'text';
    draggedText = selectedText;
    draggedImageSrc = null;

    console.log('📝 텍스트 드래그 시작:', draggedText.substring(0, 50) + '...');

    // 텍스트 선택 영역 확인
    const selection = window.getSelection();
    if (selection.rangeCount > 0) {
      const range = selection.getRangeAt(0);
      const rect = range.getBoundingClientRect();

      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;

      const dropZoneWidth = 400;
      const dropZoneHeight = 300;
      const dropZoneLeft = (windowWidth - dropZoneWidth) / 2;
      const dropZoneRight = (windowWidth + dropZoneWidth) / 2;
      const dropZoneTop = (windowHeight - dropZoneHeight) / 2;
      const dropZoneBottom = (windowHeight + dropZoneHeight) / 2;

      const isOverlapping = !(
        rect.right < dropZoneLeft ||
        rect.left > dropZoneRight ||
        rect.bottom < dropZoneTop ||
        rect.top > dropZoneBottom
      );

      dragStartedInCenter = isOverlapping;

      console.log('🔍 텍스트 선택 영역 겹침:', isOverlapping);
    } else {
      dragStartedInCenter = false;
    }

    // 드롭존 다이얼로그 표시
    showDropZoneDialog('text');
  }
}, true);

// 드래그 종료 시
document.addEventListener('dragend', (e) => {
  console.log('🔚 dragend 이벤트 발생', e.target.tagName);

  if (e.target.tagName === 'IMG' || draggedText) {
    console.log('✅ 드래그 종료 - 드롭존 제거 중...');

    setTimeout(() => {
      console.log('⏰ 타임아웃 실행 - 드롭존 숨기기');
      hideDropZoneDialog();
      draggedImageSrc = null;
      draggedText = null;
      dragType = null;
      dragStartedInCenter = false;
    }, 100);
  }
}, true);

/**
 * 드롭존 다이얼로그 표시
 */
function showDropZoneDialog(type) {
  // 기존 다이얼로그가 있으면 제거
  if (dropZoneDialog) {
    hideDropZoneDialog();
  }

  // 다이얼로그 컨테이너
  dropZoneDialog = document.createElement('div');
  dropZoneDialog.id = 'pola-dropzone-dialog';
  dropZoneDialog.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.7);
    z-index: 2147483647;
    display: flex;
    align-items: center;
    justify-content: center;
    pointer-events: none;
  `;

  // 드롭존 박스
  const dropZone = document.createElement('div');
  dropZone.id = 'pola-dropzone';
  dropZone.style.cssText = `
    width: 400px;
    height: 300px;
    background: white;
    border: 3px dashed #B0804C;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16px;
    pointer-events: auto;
    transition: all 0.3s ease;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  `;

  // 아이콘
  const icon = document.createElement('div');
  if (type === 'text') {
    icon.innerHTML = `
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#B0804C" stroke-width="2">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
        <polyline points="14 2 14 8 20 8"></polyline>
        <line x1="16" y1="13" x2="8" y2="13"></line>
        <line x1="16" y1="17" x2="8" y2="17"></line>
        <polyline points="10 9 9 9 8 9"></polyline>
      </svg>
    `;
  } else {
    icon.innerHTML = `
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#B0804C" stroke-width="2">
        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
        <polyline points="17 8 12 3 7 8"></polyline>
        <line x1="12" y1="3" x2="12" y2="15"></line>
      </svg>
    `;
  }

  // 텍스트
  const text = document.createElement('div');
  text.style.cssText = `
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    font-size: 18px;
    font-weight: 600;
    color: #333;
  `;
  text.textContent = type === 'text' ? 'POLA에 텍스트 저장하기' : 'POLA에 이미지 저장하기';

  const subText = document.createElement('div');
  subText.style.cssText = `
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    font-size: 14px;
    color: #666;
  `;
  subText.textContent = type === 'text' ? '여기에 텍스트를 드롭하세요' : '여기에 이미지를 드롭하세요';

  const formatInfo = document.createElement('div');
  formatInfo.style.cssText = `
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    font-size: 12px;
    color: #999;
    margin-top: 8px;
  `;
  formatInfo.textContent = type === 'text' ? '선택한 텍스트 저장' : '지원 형식: PNG, JPEG, WebP';

  dropZone.appendChild(icon);
  dropZone.appendChild(text);
  dropZone.appendChild(subText);
  dropZone.appendChild(formatInfo);
  dropZoneDialog.appendChild(dropZone);
  document.body.appendChild(dropZoneDialog);

  // 🎯 드래그 시작 위치에 따라 드롭존 위치 결정
  if (dragStartedInCenter) {
    // 이미지가 원래 중앙에 있었다면 → 드롭존을 오른쪽으로
    dropZoneDialog.style.justifyContent = 'flex-end';
    dropZoneDialog.style.paddingRight = '40px';
    console.log('🔄 드롭존 → 오른쪽 고정 (이미지가 중앙에서 시작)');
  } else {
    // 이미지가 원래 사이드에 있었다면 → 드롭존을 중앙에
    dropZoneDialog.style.justifyContent = 'center';
    dropZoneDialog.style.paddingRight = '0';
    console.log('🔄 드롭존 → 중앙 고정 (이미지가 사이드에서 시작)');
  }

  // dragover 이벤트는 필요 없음 (고정 위치이므로)

  // 드롭존 이벤트
  dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    e.stopPropagation();
    dropZone.style.background = '#FFF8F0';
    dropZone.style.borderColor = '#8B6340';
    dropZone.style.transform = 'scale(1.05)';
  });

  dropZone.addEventListener('dragleave', (e) => {
    e.preventDefault();
    e.stopPropagation();
    dropZone.style.background = 'white';
    dropZone.style.borderColor = '#B0804C';
    dropZone.style.transform = 'scale(1)';
  });

  dropZone.addEventListener('drop', async (e) => {
    e.preventDefault();
    e.stopPropagation();

    console.log('드롭됨! 타입:', dragType);

    // 로딩 상태로 변경
    text.textContent = '업로드 중...';
    subText.textContent = '잠시만 기다려주세요';
    icon.innerHTML = `
      <div style="width: 64px; height: 64px; border: 4px solid #f3f3f3; border-top: 4px solid #B0804C; border-radius: 50%; animation: spin 1s linear infinite;"></div>
      <style>
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      </style>
    `;

    // 백그라운드로 업로드 요청
    try {
      if (!chrome.runtime?.id) {
        throw new Error('확장 프로그램이 다시 로드되었습니다. 페이지를 새로고침해주세요.');
      }

      let message;
      if (dragType === 'text') {
        console.log('📝 텍스트 업로드 요청:', draggedText?.substring(0, 50) + '...');
        message = {
          action: 'uploadTextFromDrag',
          text: draggedText,
          pageUrl: window.location.href,
          pageTitle: document.title
        };
      } else {
        console.log('📸 이미지 업로드 요청:', draggedImageSrc);
        message = {
          action: 'uploadImageFromDrag',
          imageUrl: draggedImageSrc,
          pageUrl: window.location.href,
          pageTitle: document.title
        };
      }

      chrome.runtime.sendMessage(message, (response) => {
        if (chrome.runtime.lastError) {
          console.error('Runtime 에러:', chrome.runtime.lastError);
          text.textContent = '❌ 업로드 실패';
          subText.textContent = '확장 프로그램을 확인해주세요';
          icon.innerHTML = `
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#f44336" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
          `;

          setTimeout(() => {
            hideDropZoneDialog();
          }, 3000);
          return;
        }

        if (response && response.success) {
          // 성공
          text.textContent = '✅ 업로드 완료!';
          subText.textContent = 'POLA에서 확인하세요';
          icon.innerHTML = `
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#4CAF50" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
          `;

          setTimeout(() => {
            hideDropZoneDialog();
          }, 2000);
        } else {
          // 실패
          text.textContent = '❌ 업로드 실패';
          subText.textContent = response?.error || '다시 시도해주세요';
          icon.innerHTML = `
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#f44336" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
          `;

          setTimeout(() => {
            hideDropZoneDialog();
          }, 2000);
        }
      });
    } catch (error) {
      console.error('업로드 오류:', error);
      text.textContent = '❌ 업로드 실패';
      subText.textContent = error.message;
      icon.innerHTML = `
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#f44336" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
      `;

      setTimeout(() => {
        hideDropZoneDialog();
      }, 3000);
    }
  });

  // ESC 키로 취소
  const handleEscape = (e) => {
    if (e.key === 'Escape') {
      console.log('드롭존 취소됨');
      hideDropZoneDialog();
    }
  };

  document.addEventListener('keydown', handleEscape);

  // cleanup 함수 저장
  dropZone._cleanup = () => {
    document.removeEventListener('keydown', handleEscape);
  };
}

/**
 * 드롭존 다이얼로그 숨기기
 */
function hideDropZoneDialog() {
  if (dropZoneDialog && dropZoneDialog.parentNode) {
    const dropZone = document.getElementById('pola-dropzone');
    if (dropZone && dropZone._cleanup) {
      dropZone._cleanup();
    }
    dropZoneDialog.remove();
    dropZoneDialog = null;
  }
}