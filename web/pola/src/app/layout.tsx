import "./globals.css";
import { Metadata } from "next";
import type { ReactNode } from "react";

export const metadata: Metadata = {
  title: "POLA",
  description: "나만의 스크랩북, POLA",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ko">
      <body className="min-h-screen bg-[#FFFEF8]">{children}</body>
    </html>
  );
}
