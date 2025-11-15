import { Metadata } from "next";
import type { ReactNode } from "react";
import Header from "../home/components/Header";

export const metadata: Metadata = {
  title: "POLA",
  description: "나만의 스크랩북, POLA",
};

export default function HomeLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex flex-col h-screen bg-[#FFFEF8]">
      <Header />
      <main className="flex-1 overflow-auto">{children}</main>
    </div>
  );
}
