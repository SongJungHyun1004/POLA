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
    </div>
  );
}
