import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
  // 환경변수 명시적 선언 (빌드 시 주입)
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
    NEXT_PUBLIC_GOOGLE_CLIENT_ID: process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID,
    NEXT_PUBLIC_SHARE_KEY: process.env.NEXT_PUBLIC_SHARE_KEY,
  },
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "developers.google.com",
        pathname: "/**",
      },
    ],
  },
};

export default nextConfig;