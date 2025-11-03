"use client";

import { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Search, SlidersHorizontal, X } from "lucide-react";

export default function Header() {
  const router = useRouter();

  const [query, setQuery] = useState("");
  const [tag, setTag] = useState("");
  const [category, setCategory] = useState("");

  const [showModal, setShowModal] = useState(false);

  const tags = ["태그1", "태그2", "태그3", "태그4", "태그5", "태그6"];
  const categories = ["Travel", "Food", "Daily", "Friends", "Memories"];

  const handleSearch = () => {
    const params = new URLSearchParams();

    if (query) params.append("search", query);
    if (tag) params.append("tags", tag);
    if (category) params.append("category", category);

    router.push(`/files?${params.toString()}`);
    setShowModal(false);
  };

  return (
    <>
      {/* Header */}
      <header className="flex justify-between items-center w-full pb-10 px-8 pt-6">
        {/* Logo */}
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

        {/* Search */}
        <div className="flex items-center w-1/2 gap-2">
          <div className="flex items-center flex-grow bg-white border rounded-full px-4 py-2">
            <input
              type="text"
              placeholder="검색어"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") handleSearch();
              }}
              className="flex-grow outline-none text-tertiary placeholder:text-tertiary/50"
            />

            {/* 상세 검색 버튼 */}
            <button
              type="button"
              onClick={() => setShowModal(true)}
              className="mr-2 text-tertiary hover:text-black transition"
              title="상세 검색"
            >
              <SlidersHorizontal className="w-5 h-5" />
            </button>

            {/* 돋보기 검색 */}
            <button
              type="button"
              onClick={handleSearch}
              className="text-tertiary hover:text-black transition"
              title="검색"
            >
              <Search className="w-5 h-5" />
            </button>
          </div>

          <button className="w-10 h-10 bg-black text-white border rounded-full font-semibold flex items-center justify-center">
            AI
          </button>
        </div>

        {/* Profile */}
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

      {/* 상세 검색 모달 */}
      {showModal && (
        <div className="fixed inset-0 bg-black/30 z-50 flex justify-center items-start pt-20 animate-fadeIn">
          <div className="bg-white w-[90%] max-w-2xl rounded-xl p-6 shadow-lg animate-slideDown">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold">상세 검색</h2>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-500 hover:text-black"
              >
                <X className="w-5 h-5" />
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
                  className="border rounded-lg px-3 py-2 outline-none"
                  placeholder="검색어 입력"
                />
              </div>

              {/* 태그 */}
              <div className="flex flex-col">
                <label className="text-sm mb-1 font-medium">태그</label>
                <select
                  value={tag}
                  onChange={(e) => setTag(e.target.value)}
                  className="border rounded-lg px-3 py-2 outline-none"
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
                  className="border rounded-lg px-3 py-2 outline-none"
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
                onClick={handleSearch}
                className="mt-2 bg-black text-white rounded-lg py-2 font-semibold"
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
