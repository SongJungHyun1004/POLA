import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  // 환경변수 명시적 선언 (빌드 시 주입)
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
    NEXT_PUBLIC_GOOGLE_CLIENT_ID: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID,
    NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE:
      process.env.NEXT_PUBLIC_GOOGLE_CLIENT_SOURCE,
    NEXT_PUBLIC_POLA_API_BASE_URL: process.env.NEXT_PUBLIC_POLA_API_BASE_URL,
  },
  images: {
    unoptimized: true,
    remotePatterns: [
      {
        protocol: "https",
        hostname: "developers.google.com",
        pathname: "/**",
      },
      {
        protocol: "https",
        hostname: "pola-storage-bucket.s3.ap-northeast-2.amazonaws.com",
        pathname: "/**",
      },
    ],
  },
};

export default nextConfig;