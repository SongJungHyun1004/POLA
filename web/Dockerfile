# ---------------------------------
# Stage 1: Build (빌드 단계)
# ---------------------------------
FROM node:18-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# package.json과 package-lock.json 먼저 복사
COPY package*.json ./

# 의존성 설치 (node_modules 생성)
RUN npm install

# 나머지 소스코드 전체 복사
COPY . .

# 프로덕션 빌드 실행 (e.g., /app/build 또는 /app/.next 폴더 생성)
RUN npm run build

# ---------------------------------
# Stage 2: Production (실행 단계)
# ---------------------------------
# node 18-alpine 이미지를 다시 사용 (빌드 도구 불필요)
FROM node:18-alpine

WORKDIR /app

# 빌드 단계에서 설치한 '운영용' 의존성만 복사
# (Next.js의 경우, package*.json을 복사하고 --production으로 재설치)
COPY --from=builder /app/package*.json ./
RUN npm install --production

# Next.js의 경우 .next와 public 폴더 복사
# (React의 경우, build 폴더와 package.json, server.js 등을 복사)
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

# Nginx가 프록시할 3000번 포트 개방
EXPOSE 3000

# "npm run start" 명령어로 서버 실행
CMD ["npm", "run", "start"]
