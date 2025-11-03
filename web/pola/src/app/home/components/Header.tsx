"use client";

import { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Search, SlidersHorizontal, Send } from "lucide-react";

export default function Header() {
  const router = useRouter();

  const [query, setQuery] = useState("");
  const [aiQuery, setAiQuery] = useState("");
  const [aiMode, setAiMode] = useState(false);

  const [tag, setTag] = useState("");
  const [category, setCategory] = useState("");
  const [showModal, setShowModal] = useState(false);

  const tags = ["태그1", "태그2", "태그3", "태그4", "태그5", "태그6"];
  const categories = ["Travel", "Food", "Daily", "Friends", "Memories"];

  const doNormalSearch = () => {
    const params = new URLSearchParams();
    if (query) params.append("search", query);
    if (tag) params.append("tags", tag);
    if (category) params.append("category", category);
    router.push(`/files?${params.toString()}`);
  };

  const doAISearch = () => {
    if (!aiQuery) return;
    router.push(`/files?nlp=${encodeURIComponent(aiQuery)}`);
    setAiMode(false);
  };

  return (
    <>
      <header className="flex justify-between items-center w-full pb-10 px-8 pt-6">
        {/* Logo */}
        <Link href="/home">
          <Image
            src="/images/POLA_logo_2.png"
            alt="pola logo"
            width={140}
            height={40}
            className="object-contain cursor-pointer"
            priority
          />
        </Link>

        {/* Search Section */}
        <div className="flex items-center w-1/2 gap-3">
          {/* Normal Search Box OR Icon */}
          <div
            className={`transition-all duration-300 flex items-center bg-white border rounded-full 
          ${
            aiMode
              ? "w-10 h-10 justify-center p-0 border"
              : "flex-grow px-4 py-2 border"
          }`}
          >
            {!aiMode && (
              <>
                <input
                  type="text"
                  placeholder="검색어"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && doNormalSearch()}
                  className="flex-grow outline-none text-tertiary placeholder:text-tertiary/50"
                />

                <button
                  type="button"
                  onClick={() => setShowModal(true)}
                  className="mr-2 text-tertiary hover:text-black"
                >
                  <SlidersHorizontal className="w-5 h-5" />
                </button>
              </>
            )}

            <button
              type="button"
              onClick={() => {
                if (aiMode) setAiMode(false);
                else doNormalSearch();
              }}
              className="text-tertiary hover:text-black transition"
            >
              <Search className="w-5 h-5" />
            </button>
          </div>

          {/* AI Search Capsule OR AI Button */}
          <div
            className={`bg-white border rounded-full flex items-center transition-all duration-300 overflow-hidden 
          ${aiMode ? "flex-grow px-4 py-2" : "w-10 h-10 justify-center"}`}
          >
            {aiMode ? (
              <>
                <input
                  type="text"
                  placeholder="AI를 통한 자연어 검색"
                  value={aiQuery}
                  onChange={(e) => setAiQuery(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && doAISearch()}
                  className="flex-grow outline-none placeholder:text-tertiary/50 animate-fade-slide-in"
                />
                <button
                  onClick={doAISearch}
                  className="text-black hover:text-gray-800"
                >
                  <Send className="w-5 h-5" />
                </button>
              </>
            ) : (
              <button
                type="button"
                onClick={() => setAiMode(true)}
                className="w-full h-full flex items-center justify-center font-semibold text-black"
              >
                AI
              </button>
            )}
          </div>
        </div>

        {/* Profile */}
        <div className="flex items-center gap-3">
          <span className="font-medium">username</span>
          <div className="relative w-10 h-10 bg-white rounded-full border overflow-hidden">
            <Image
              src="/images/POLA_logo_1.png"
              alt="profile"
              fill
              className="object-cover"
            />
          </div>
        </div>
      </header>

      {showModal && (
        <div className="fixed inset-0 bg-black/30 z-50 flex justify-center items-start pt-20">
          <div className="bg-white w-[90%] max-w-2xl rounded-xl p-6 shadow-lg animate-fade-slide-in">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold">상세 검색</h2>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-500 hover:text-black"
              >
                ✕
              </button>
            </div>

            <div className="flex flex-col gap-4 text-tertiary">
              {/* 검색어 */}
              <div className="flex flex-col">
                <label className="text-sm mb-1 font-medium">검색어</label>
                <input
                  type="text"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  className="h-12 px-3 py-2 rounded-lg border outline-none"
                  placeholder="검색어 입력"
                />
              </div>

              {/* 태그 */}
              <div className="flex flex-col">
                <label className="text-sm mb-1 font-medium">태그</label>
                <select
                  value={tag}
                  onChange={(e) => setTag(e.target.value)}
                  className="h-12 px-3 py-2 rounded-lg border outline-none"
                >
                  <option value="">전체</option>
                  {tags.map((t) => (
                    <option key={t} value={t}>
                      {t}
                    </option>
                  ))}
                </select>
              </div>

              {/* 카테고리 */}
              <div className="flex flex-col">
                <label className="text-sm mb-1 font-medium">카테고리</label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  className="h-12 px-3 py-2 rounded-lg border outline-none"
                >
                  <option value="">전체</option>
                  {categories.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>

              <button
                type="button"
                onClick={() => {
                  doNormalSearch();
                  setShowModal(false);
                }}
                className="mt-2 bg-black text-white rounded-lg py-2 text-center font-semibold"
              >
                검색하기
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
