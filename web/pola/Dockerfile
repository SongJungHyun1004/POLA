
  # ---------------------------------
  # Stage 1: Build
  # ---------------------------------
  FROM node:20-alpine AS builder

  WORKDIR /app

  # Corepack 활성화 및 Yarn 설정
  RUN corepack enable

  # 의존성 파일 복사 및 설치
  COPY package.json yarn.lock .yarnrc.yml ./
  RUN yarn install --immutable

  # 소스 복사 및 빌드
  COPY . .
  RUN yarn build

  # ---------------------------------
  # Stage 2: Production
  # ---------------------------------
  FROM node:20-alpine

  WORKDIR /app

  # 환경 변수 설정
  ENV NODE_ENV=production

  # Next.js standalone 빌드 결과물 복사
  COPY --from=builder /app/.next/standalone ./
  COPY --from=builder /app/.next/static ./.next/static
  COPY --from=builder /app/public ./public

  EXPOSE 3000

  # standalone 모드에서는 server.js를 직접 실행
  CMD ["node", "server.js"]
