
  # ---------------------------------
  # Stage 1: Build
  # ---------------------------------
  FROM node:20-alpine AS builder

  WORKDIR /app

  # Corepack 활성화 및 Yarn 설정
  RUN corepack enable && corepack prepare yarn@stable --activate

  # 의존성 파일 복사 및 설치
  COPY package.json yarn.lock ./
  RUN yarn install --frozen-lockfile

  # 소스 복사 및 빌드
  COPY . .
  RUN yarn build

  # ---------------------------------
  # Stage 2: Production
  # ---------------------------------
  FROM node:20-alpine

  WORKDIR /app

  # Corepack 활성화 및 Yarn 설정
  RUN corepack enable && corepack prepare yarn@stable --activate

  # 운영용 의존성 설치
  COPY --from=builder /app/package.json /app/yarn.lock ./
  RUN yarn install --production --frozen-lockfile

  # Next.js 빌드 결과물 및 설정 파일 복사
  COPY --from=builder /app/.next ./.next
  COPY --from=builder /app/public ./public
  COPY --from=builder /app/next.config.ts ./

  EXPOSE 3000

  CMD ["yarn", "start"]
