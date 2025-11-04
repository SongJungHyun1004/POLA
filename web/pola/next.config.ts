import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactCompiler: true,
  output: "standalone",
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
