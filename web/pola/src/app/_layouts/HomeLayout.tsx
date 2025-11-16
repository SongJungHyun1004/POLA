import { Metadata } from "next";
import type { ReactNode } from "react";
import Header from "../home/components/Header";
import HomeSidebar from "../home/components/HomeSidebar";

export const metadata: Metadata = {
  title: "POLA",
  description: "나만의 스크랩북, POLA",
};

export default function HomeLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex flex-col h-screen bg-[#FFFEF8]">
      {/* 상단 */}
      <Header />

      {/* 사이드바 + 메인영역 */}
      <div className="flex flex-1">
        {/* 좌측 사이드바 */}
        <HomeSidebar />

        {/* 우측 컨텐츠 */}
        <main className="flex-1 overflow-y-auto">{children}</main>
      </div>
    </div>
  );
}
