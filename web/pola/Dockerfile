  FROM node:20-alpine AS builder
  WORKDIR /app
  RUN corepack enable && corepack prepare yarn@4.1.0 --activate
  COPY package.json yarn.lock .yarnrc.yml ./
  RUN yarn install --immutable

  # ============================================================================
  # 🔥 환경변수 자동 주입 설정 
  # ============================================================================
  # Jenkinsfile에서 Secret의 모든 NEXT_PUBLIC_ 변수를 --build-arg로 전달합니다.
  # 새 환경변수 추가 시: 이 섹션에 ARG와 ENV를 추가하세요!
  # ============================================================================

  ARG NEXT_PUBLIC_API_URL
  ARG NEXT_PUBLIC_GOOGLE_CLIENT_ID
  ARG NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE
  ARG NEXT_PUBLIC_POLA_API_BASE_URL
  ARG NEXT_PUBLIC_SHARE_KEY

  # ARG를 ENV로 export (Next.js 빌드 시 필요)
  ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
  ENV NEXT_PUBLIC_GOOGLE_CLIENT_ID=$NEXT_PUBLIC_GOOGLE_CLIENT_ID
  ENV NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE=$NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE
  ENV NEXT_PUBLIC_POLA_API_BASE_URL=$NEXT_PUBLIC_POLA_API_BASE_URL
  ENV NEXT_PUBLIC_SHARE_KEY=$NEXT_PUBLIC_SHARE_KEY

  # ============================================================================

  COPY . .

  # 환경변수 확인 (디버깅용)
  RUN echo "=== Environment Variables ==="
  RUN echo "NEXT_PUBLIC_API_URL: ${NEXT_PUBLIC_API_URL}"
  RUN echo "NEXT_PUBLIC_GOOGLE_CLIENT_ID: ${NEXT_PUBLIC_GOOGLE_CLIENT_ID}"
  RUN echo "NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE: ${NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE}"
  RUN echo "NEXT_PUBLIC_POLA_API_BASE_URL: ${NEXT_PUBLIC_POLA_API_BASE_URL}"
  RUN echo "============================"

  RUN yarn build

  FROM node:20-alpine
  WORKDIR /app
  ENV NODE_ENV=production
  RUN corepack enable && corepack prepare yarn@4.1.0 --activate
  COPY --from=builder /app/package.json /app/yarn.lock /app/.yarnrc.yml ./
  COPY --from=builder /app/.next ./.next
  COPY --from=builder /app/public ./public
  RUN yarn install --frozen-lockfile
  EXPOSE 3000
  CMD ["yarn", "start"]