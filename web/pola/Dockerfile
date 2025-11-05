  FROM node:20-alpine AS builder
  WORKDIR /app
  RUN corepack enable
  COPY package.json yarn.lock .yarnrc.yml ./
  RUN yarn install --immutable

  ARG NEXT_PUBLIC_API_URL
  ARG NEXT_PUBLIC_GOOGLE_CLIENT_ID
  ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
  ENV NEXT_PUBLIC_GOOGLE_CLIENT_ID=${NEXT_PUBLIC_GOOGLE_CLIENT_ID}

  COPY . .
  RUN yarn build

  FROM node:20-alpine
  WORKDIR /app
  ENV NODE_ENV=production
  RUN corepack enable
  COPY --from=builder /app/package.json /app/yarn.lock /app/.yarnrc.yml ./
  COPY --from=builder /app/.next ./.next
  COPY --from=builder /app/public ./public
  RUN yarn workspaces focus --all --production
  EXPOSE 3000
  CMD ["yarn", "start"]