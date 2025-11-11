"use client";

import { useState, useRef, useEffect } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  Search,
  SlidersHorizontal,
  Send,
  BarChart3,
  PersonStanding,
  Upload,
  FileText,
  LogOut,
  X,
} from "lucide-react";
import useAuthStore from "@/store/useAuthStore";

export default function Header() {
  const { user } = useAuthStore();
  const router = useRouter();

  const [query, setQuery] = useState("");
  const [aiQuery, setAiQuery] = useState("");
  const [aiMode, setAiMode] = useState(false);
  const [tag, setTag] = useState("");
  const [category, setCategory] = useState("");
  const [showModal, setShowModal] = useState(false); // 🔹 상세 검색 모달
  const [showProfileModal, setShowProfileModal] = useState(false); // 🔹 프로필 모달
  const [showUploadModal, setShowUploadModal] = useState(false); // 🔹 업로드 모달

  const profileRef = useRef<HTMLDivElement>(null);

  const tags = ["태그1", "태그2", "태그3", "태그4"];
  const categories = ["Travel", "Food", "Daily", "Friends"];

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

  /** 외부 클릭 시 프로필 모달 닫기 */
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        profileRef.current &&
        !profileRef.current.contains(e.target as Node)
      ) {
        setShowProfileModal(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  if (!user) {
    return (
      <header className="flex justify-between items-center w-full pb-10 px-8 pt-6">
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

        <button
          onClick={() => router.push("/")}
          className="px-4 py-2 bg-black text-white rounded-full"
        >
          로그인
        </button>
      </header>
    );
  }

  return (
    <>
      {/* 헤더 */}
      <header className="flex justify-between items-center w-full pb-10 px-8 pt-6">
        {/* 로고 */}
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

        {/* 검색 섹션 */}
        <div className="flex items-center w-1/2 gap-3">
          <div
            className={`transition-all duration-300 flex items-center bg-white border rounded-full ${
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

          <div
            className={`bg-white border rounded-full flex items-center transition-all duration-300 overflow-hidden ${
              aiMode ? "flex-grow px-4 py-2" : "w-10 h-10 justify-center"
            }`}
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

        {/* 프로필 영역 */}
        <div ref={profileRef} className="relative flex items-center gap-3">
          {/* 🔹 클릭 범위를 유저네임 + 이미지 전체로 확장 */}
          <button
            onClick={() => setShowProfileModal((prev) => !prev)}
            className="flex items-center gap-3 cursor-pointer hover:opacity-80 transition"
          >
            <span className="font-medium">{user.display_name}</span>
            <div className="relative w-10 h-10 bg-white rounded-full border overflow-hidden">
              <img
                src={user.profile_image_url || "/images/default_profile.png"}
                alt="profile"
                className="object-cover"
              />
            </div>
          </button>

          {/* 프로필 모달 */}
          {showProfileModal && (
            <div className="absolute top-[calc(100%+10px)] right-0 bg-white border rounded-2xl shadow-lg w-64 z-50 animate-fade-slide-in">
              <div className="p-4 border-b text-center font-semibold text-[#4C3D25]">
                @{user.display_name}
              </div>

              <div className="p-4 space-y-3 text-[#4C3D25]">
                <div>
                  <p className="text-sm font-semibold mb-2">나의 활동</p>
                  <MenuItem icon={<BarChart3 />} text="내 통계" />
                  <MenuItem icon={<PersonStanding />} text="내 타입" />
                </div>

                <hr />

                <div>
                  <p className="text-sm font-semibold mb-2">기타</p>
                  <MenuItem
                    icon={<Upload />}
                    text="업로드"
                    onClick={() => {
                      setShowUploadModal(true);
                      setShowProfileModal(false);
                    }}
                  />
                  <MenuItem icon={<FileText />} text="이용약관" />
                </div>

                <hr />

                <button
                  onClick={() => alert("로그아웃 되었습니다.")}
                  className="flex items-center justify-center gap-2 text-red-500 hover:text-red-600 w-full font-semibold"
                >
                  <LogOut className="w-4 h-4" />
                  로그아웃
                </button>
              </div>
            </div>
          )}
        </div>
      </header>

      {/* 상세 검색 모달 */}
      {showModal && (
        <div className="fixed inset-0 bg-black/30 z-40 flex justify-center items-start pt-20">
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

      {/* 업로드 모달 */}
      {showUploadModal && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex justify-center items-center">
          <div className="bg-white w-[90%] max-w-md rounded-2xl p-6 shadow-xl animate-fade-slide-in">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold text-[#4C3D25]">
                파일 업로드
              </h3>
              <button
                onClick={() => setShowUploadModal(false)}
                className="text-gray-500 hover:text-black"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="flex flex-col items-center justify-center border-2 border-dashed border-[#D2C9B0] rounded-xl p-8 text-[#7A6A48]">
              <Upload className="w-10 h-10 mb-3" />
              <p className="font-medium mb-1">
                여기로 파일을 드래그하거나 클릭하세요
              </p>
              <p className="text-sm text-gray-500">
                이미지, 문서 등을 업로드할 수 있습니다.
              </p>
            </div>

            <button
              className="mt-6 w-full bg-black text-white py-2 rounded-lg font-semibold hover:bg-gray-900"
              onClick={() => {
                alert("업로드 완료!");
                setShowUploadModal(false);
              }}
            >
              업로드 완료
            </button>
          </div>
        </div>
      )}
    </>
  );
}

function MenuItem({
  icon,
  text,
  onClick,
}: {
  icon: React.ReactNode;
  text: string;
  onClick?: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className="flex justify-between items-center w-full hover:bg-[#F7F4EC] px-3 py-2 rounded-lg transition-colors"
    >
      <div className="flex items-center gap-2">
        {icon}
        <span className="text-sm">{text}</span>
      </div>
      <span className="text-gray-400">›</span>
    </button>
  );
}
