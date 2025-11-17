"use client";

import { Metadata } from "next";
import type { ReactNode } from "react";
import Header from "../(pola)/home/components/Header";
import HomeSidebar from "../(pola)/home/components/HomeSidebar";

export const metadata: Metadata = {
  title: "POLA",
  description: "나만의 스크랩북, POLA",
};

export default function HomeLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex flex-col h-screen bg-[#FFFEF8] overflow-hidden">
      <div className="shrink-0">
        <Header />
      </div>
      <div className="flex flex-1 overflow-hidden">
        <div className="shrink-0 h-full">
          <HomeSidebar />
        </div>
        <main className="flex-1 overflow-y-auto">{children}</main>
      </div>

      {/* Footer */}
      <footer className="border-t border-[#D6CFBC] py-2 bg-[#F7F4EC] text-center text-sm text-[#4C3D25]">
        <small>
          © 2025 POLA. All rights reserved. | 버전 1.0.0 |
          <a className="underline ml-1" href="mailto:starforce.mozzi@gmail.com">
            문의하기
          </a>{" "}
          |
          <a
            className="underline ml-1"
            href="https://k13d204.p.ssafy.io/privacy-policy"
            target="_blank"
          >
            개인정보처리방침
          </a>
        </small>
      </footer>
    </div>
  );
}
