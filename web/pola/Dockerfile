
  # ---------------------------------
  # Stage 1: Build
  # ---------------------------------
  FROM node:18-alpine AS builder

  WORKDIR /app

  # Yarn 의존성 설치
  COPY package.json yarn.lock ./
  RUN yarn install --frozen-lockfile

  # 소스 복사 및 빌드
  COPY . .
  RUN yarn build

  # ---------------------------------
  # Stage 2: Production
  # ---------------------------------
  FROM node:18-alpine

  WORKDIR /app

  # 운영용 의존성 설치
  COPY --from=builder /app/package.json /app/yarn.lock ./
  RUN yarn install --production --frozen-lockfile

  # Next.js 빌드 결과물 복사
  COPY --from=builder /app/.next ./.next
  COPY --from=builder /app/public ./public

  EXPOSE 3000

  CMD ["yarn", "start"]
