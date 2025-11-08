document.addEventListener('DOMContentLoaded', () => {
  console.log('팝업 로드됨');
  
  const testBtn = document.getElementById('testBtn');
  
  testBtn.addEventListener('click', () => {
    console.log('테스트 버튼 클릭됨');
    
    // Background script에 메시지 전송
    chrome.runtime.sendMessage({
      action: 'test'
    }, (response) => {
      console.log('응답:', response);
    });
    
    // 알림 표시
    chrome.notifications.create({
      type: 'basic',
      iconUrl: 'icons/icon48.png',
      title: '테스트 알림',
      message: '확장 프로그램이 정상적으로 동작하고 있습니다!'
    });
  });
});
