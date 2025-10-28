"use client";

import { useState } from "react";

export default function Header() {
  const [query, setQuery] = useState("");

  return (
    <header className="flex justify-between items-center w-full mb-8">
      {/* 왼쪽 - 프로필 */}
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 bg-temp rounded-full border border-tertiary"></div>
        <span className="font-medium">username</span>
      </div>

      {/* 중앙 - 검색창 */}
      <div className="flex items-center w-1/2 bg-white border rounded-full px-4 py-2">
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

      {/* 오른쪽 - AI 버튼 */}
      <button className="w-10 h-10 rounded-full bg-primary text-white font-semibold flex items-center justify-center">
        AI
      </button>
    </header>
  );
}
