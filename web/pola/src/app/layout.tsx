import "./globals.css";
import { Metadata } from "next";
import type { ReactNode } from "react";

export const metadata: Metadata = {
  title: "POLA",
  description: "나만의 스크랩북, POLA",
  icons: {
    icon: [
      { url: "/images/POLA_favicon.png", type: "image/png", sizes: "32x32" },
      { url: "/images/POLA_favicon.png", type: "image/png", sizes: "192x192" },
    ],
    shortcut: "/images/POLA_favicon.png",
    apple: "/images/POLA_favicon.png",
  },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ko">
      <head>
        <link rel="icon" type="image/png" href="/images/POLA_favicon.png?v=2" />
      </head>
      <body className="min-h-screen bg-[#FFFEF8]">{children}</body>
    </html>
  );
}
