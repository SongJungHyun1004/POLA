// build.js - manifest.json과 config.js 자동 생성
const fs = require('fs');
const path = require('path');

// config.js에서 설정 로드
const CONFIG = require('./config.js');

// manifest.template.json 읽기
const manifestTemplate = JSON.parse(
  fs.readFileSync('manifest.template.json', 'utf8')
);

// 템플릿에 실제 값 주입
manifestTemplate.oauth2.client_id = CONFIG.GOOGLE_CLIENT_ID;
manifestTemplate.host_permissions = [`${CONFIG.API_BASE_URL}*`];

// manifest.json 생성
fs.writeFileSync(
  'manifest.json',
  JSON.stringify(manifestTemplate, null, 2)
);

console.log('manifest.json 생성 완료');
console.log('빌드 완료!');