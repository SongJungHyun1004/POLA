"use client";

import { useState } from "react";
import Image from "next/image";
import Link from "next/link";

export default function Header() {
  const [query, setQuery] = useState("");

  return (
    <header className="flex justify-between items-center w-full pb-12 px-8 pt-6">
      {/* 왼쪽 - 로고 */}
      <Link href="/home" className="flex items-center">
        <Image
          src="/images/POLA_logo_2.png"
          alt="pola logo"
          width={140}
          height={40}
          className="object-contain cursor-pointer"
          priority
        />
      </Link>

      {/* 중앙 - 검색창 + AI 버튼 */}
      <div className="flex items-center w-1/2 gap-2">
        <div className="flex items-center flex-grow bg-white border rounded-full px-4 py-2">
          <input
            type="text"
            placeholder="검색어"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="flex-grow outline-none text-tertiary placeholder:text-tertiary/50"
          />
          <span className="material-symbols-outlined text-tertiary cursor-pointer">
            search
          </span>
        </div>

        <button className="w-10 h-10 bg-black text-white border rounded-full font-semibold flex items-center justify-center">
          AI
        </button>
      </div>

      {/* 오른쪽 - 프로필 */}
      <div className="flex items-center gap-3">
        <span className="font-medium">username</span>
        <div className="relative w-10 h-10 bg-white rounded-full border border-tertiary overflow-hidden">
          <Image
            src="/images/POLA_logo_1.png"
            alt="user profile image"
            fill
            className="object-cover"
          />
        </div>
      </div>
    </header>
  );
}
