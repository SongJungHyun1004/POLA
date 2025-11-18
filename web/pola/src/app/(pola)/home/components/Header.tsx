"use client";

import { useState, useRef, useEffect, KeyboardEvent, MouseEvent } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  Search,
  ChevronDown,
  FolderHeart,
  PersonStanding,
  Upload,
  FileText,
  Puzzle,
  LogOut,
  X,
} from "lucide-react";

import useAuthStore from "@/store/useAuthStore";
import { authService } from "@/services/authService";
import { uploadService } from "@/services/uploadService";
import { fetchTagSuggestions } from "@/services/fileService";

type SearchMode = "INTEGRATED" | "TAG";

const SEARCH_HISTORY_KEY = "pola_search_history";

export default function Header() {
  const { user } = useAuthStore();
  const router = useRouter();

  const [query, setQuery] = useState("");

  /** 검색 모드 */
  const [searchMode, setSearchMode] = useState<SearchMode>("INTEGRATED");
  const [modeDropdownOpen, setModeDropdownOpen] = useState(false);

  /** 자동완성 */
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [highlightIndex, setHighlightIndex] = useState(-1);

  /** 프로필 / 업로드 */
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadedFile, setUploadedFile] = useState(false);

  const profileRef = useRef<HTMLDivElement>(null);
  const modeDropdownRef = useRef<HTMLDivElement>(null);
  const searchWrapperRef = useRef<HTMLDivElement>(null);

  /* -------------------- 최근 검색어 -------------------- */
  const getSearchHistory = () => {
    if (typeof window === "undefined") return [];
    try {
      return JSON.parse(localStorage.getItem(SEARCH_HISTORY_KEY) || "[]");
    } catch {
      return [];
    }
  };

  const saveSearchHistory = (term: string) => {
    if (!term.trim()) return;
    const prev = getSearchHistory().filter((t: string) => t !== term);
    const next = [term, ...prev].slice(0, 20);
    localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(next));
  };

  /* -------------------- 자동완성 -------------------- */
  useEffect(() => {
    if (!query.trim()) {
      setSuggestions([]);
      setShowSuggestions(false);
      setHighlightIndex(-1);
      return;
    }

    async function load() {
      if (searchMode === "TAG") {
        try {
          const tags = await fetchTagSuggestions(query);
          setSuggestions(tags);
          setShowSuggestions(tags.length > 0);
          setHighlightIndex(-1);
        } catch {
          setSuggestions([]);
          setShowSuggestions(false);
        }
      } else {
        const history = getSearchHistory().filter((t: string) =>
          t.includes(query)
        );
        setSuggestions(history);
        setShowSuggestions(history.length > 0);
        setHighlightIndex(-1);
      }
    }

    load();
  }, [query, searchMode]);

  /* -------------------- 방향키 네비게이션 -------------------- */
  const handleSearchInputKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (!showSuggestions || suggestions.length === 0) {
      if (e.key === "Enter") {
        e.preventDefault();
        doSearch();
      }
      return;
    }

    if (e.key === "ArrowDown") {
      e.preventDefault();
      setHighlightIndex((prev) =>
        prev < suggestions.length - 1 ? prev + 1 : 0
      );
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setHighlightIndex((prev) =>
        prev > 0 ? prev - 1 : suggestions.length - 1
      );
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (highlightIndex >= 0) {
        const value = suggestions[highlightIndex];
        setQuery(value);
        setShowSuggestions(false);
        setHighlightIndex(-1);
        return;
      }
      doSearch();
    } else if (e.key === "Escape") {
      setShowSuggestions(false);
      setHighlightIndex(-1);
    }
  };

  /* -------------------- 검색 실행 -------------------- */
  const doSearch = () => {
    if (!query.trim()) return;

    if (searchMode === "INTEGRATED") {
      saveSearchHistory(query);
      router.push(`/files?search=${encodeURIComponent(query)}`);
    } else {
      router.push(`/files?tag=${encodeURIComponent(query)}`);
    }

    setShowSuggestions(false);
  };

  /* -------------------- 외부 클릭 처리 -------------------- */
  useEffect(() => {
    const handleClick = (e: MouseEvent | globalThis.MouseEvent) => {
      const target = e.target as Node;

      if (profileRef.current && !profileRef.current.contains(target)) {
        setShowProfileModal(false);
      }
      if (
        modeDropdownRef.current &&
        !modeDropdownRef.current.contains(target)
      ) {
        setModeDropdownOpen(false);
      }
      if (
        searchWrapperRef.current &&
        !searchWrapperRef.current.contains(target)
      ) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  /* -------------------- 업로드 -------------------- */
  async function handleUploadProcess(file: File) {
    try {
      setUploading(true);
      setUploadedFile(false);

      const { url, key } = await uploadService.getPresignedUploadUrl(file.name);

      await uploadService.uploadToS3(url, file);

      const originUrl = url.split("?")[0];
      const completeData = await uploadService.completeUpload({
        key,
        type: file.type,
        fileSize: file.size,
        originUrl,
        platform: "WEB",
      });

      uploadService.postProcess(completeData.id);
      setUploadedFile(true);
    } catch (err) {
      console.error(err);
      alert("파일 업로드 중 오류가 발생했습니다.");
    } finally {
      setUploading(false);
    }
  }

  /* -------------------- 로그인 전 헤더 -------------------- */
  if (!user) {
    return (
      <header className="flex justify-between items-center w-full pb-10 px-12 pt-6">
        <Link href="/home">
          <Image
            src="/images/POLA_logo_2.png"
            alt="pola logo"
            width={140}
            height={40}
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

  const modeLabel = searchMode === "INTEGRATED" ? "통합 검색" : "태그 검색";

  /* -------------------- 로그인 후 헤더 -------------------- */
  return (
    <>
      <header className="relative flex justify-between items-center w-full pb-10 px-12 pt-6 bg-[#FFFEF8]">
        {/* 로고 */}
        <Link href="/home">
          <Image
            src="/images/POLA_logo_2.png"
            alt="pola logo"
            width={140}
            height={40}
            priority
          />
        </Link>

        {/* -------------------- 검색 영역 -------------------- */}
        <div
          ref={searchWrapperRef}
          className="relative flex items-center w-1/2 gap-3"
        >
          {/* 검색창 */}
          <div className="flex items-center flex-grow bg-white border rounded-full px-4 py-2">
            {/* 검색 모드 */}
            <div ref={modeDropdownRef} className="relative">
              <button
                onClick={() => setModeDropdownOpen((p) => !p)}
                className="flex items-center gap-1 font-semibold text-[#4C3D25]"
              >
                {modeLabel}
                <ChevronDown className="w-4 h-4" />
              </button>

              {modeDropdownOpen && (
                <div className="absolute top-[120%] left-0 bg-white border rounded-xl shadow-lg z-50 py-1 w-32">
                  <button
                    onClick={() => {
                      setSearchMode("INTEGRATED");
                      setQuery("");
                      setSuggestions([]);
                      setModeDropdownOpen(false);
                    }}
                    className="block w-full text-left px-3 py-2 hover:bg-gray-100"
                  >
                    통합 검색
                  </button>
                  <button
                    onClick={() => {
                      setSearchMode("TAG");
                      setQuery("");
                      setSuggestions([]);
                      setModeDropdownOpen(false);
                    }}
                    className="block w-full text-left px-3 py-2 hover:bg-gray-100"
                  >
                    태그 검색
                  </button>
                </div>
              )}
            </div>

            {/* 검색 입력 */}
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={handleSearchInputKeyDown}
              onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
              placeholder="검색어 입력"
              className="flex-grow ml-3 outline-none"
            />

            {/* 검색 버튼 */}
            <button onClick={doSearch} className="hover:text-black">
              <Search className="w-5 h-5 text-tertiary" />
            </button>
          </div>

          {/* -------------------- AI 검색 버튼 + 툴팁 -------------------- */}
          <div className="relative group">
            <button
              onClick={() => router.push("/ragsearch")}
              className="w-10 h-10 flex items-center justify-center rounded-full hover:scale-110 transition-transform"
            >
              <Image
                src="/images/POLA_chatbot.png"
                alt="AI 검색"
                width={40}
                height={40}
                className="object-contain rounded-full"
              />
            </button>

            {/* Tooltip */}
            <div
              className="
    absolute top-1/2 -translate-y-1/2 left-[115%]
    bg-white text-[#4C3D25] text-sm font-medium
    px-4 py-2 rounded-2xl shadow-lg border border-[#E5E2DA]
    whitespace-nowrap
    opacity-0 group-hover:opacity-100
    pointer-events-none
    transition-all duration-200
    z-50
  "
            >
              AI 도우미 상담포아가 검색을 도와줘요
            </div>
          </div>

          {/* 자동완성 */}
          {showSuggestions && suggestions.length > 0 && (
            <div className="absolute left-0 right-0 top-[100%] mt-2 bg-white border rounded-xl shadow-lg z-40 p-3 max-h-80 overflow-y-auto">
              {suggestions.map((s, idx) => (
                <button
                  key={`${s}-${idx}`}
                  onMouseDown={(e) => {
                    e.preventDefault();
                    setQuery(s);
                    setShowSuggestions(false);
                  }}
                  className={`w-full text-left px-3 py-2 rounded-lg ${
                    highlightIndex === idx ? "bg-gray-200" : "hover:bg-gray-100"
                  }`}
                >
                  {searchMode === "TAG" ? `#${s}` : s}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* -------------------- 프로필 -------------------- */}
        <div ref={profileRef} className="relative flex items-center gap-3">
          <button
            onClick={() => setShowProfileModal((p) => !p)}
            className="flex items-center gap-3 hover:opacity-80"
          >
            <span>{user.display_name}</span>
            <div className="relative w-10 h-10 bg-white rounded-full border overflow-hidden">
              <img
                src={user.profile_image_url || "/images/default_profile.png"}
                alt="profile"
                className="w-full h-full object-cover"
              />
            </div>
          </button>

          {/* 프로필 모달 */}
          {showProfileModal && (
            <div className="absolute top-[calc(100%+10px)] right-0 bg-white border rounded-2xl shadow-lg w-64 z-50">
              <div className="p-4 border-b text-center font-semibold text-[#4C3D25]">
                @{user.display_name}
              </div>
              <div className="p-4 space-y-3">
                <MenuItem
                  icon={<FolderHeart />}
                  text="내 카테고리"
                  onClick={() => {
                    router.push("/my/categories");
                    setShowProfileModal(false);
                  }}
                />
                <MenuItem
                  icon={<Upload />}
                  text="업로드"
                  onClick={() => {
                    setShowUploadModal(true);
                    setShowProfileModal(false);
                  }}
                />
                <MenuItem
                  icon={<PersonStanding />}
                  text="내 타입"
                  onClick={() => {
                    router.push("/my/type");
                    setShowProfileModal(false);
                  }}
                />
                <hr />
                <MenuItem
                  icon={<Puzzle />}
                  text="POLA 익스텐션"
                  onClick={() =>
                    window.open(
                      "https://chromewebstore.google.com/detail/pola/fclaojhnblpfnpneiipdkfkhpkghmbcp",
                      "_blank"
                    )
                  }
                />
                <MenuItem
                  icon={<FileText />}
                  text="개인정보 처리방침"
                  onClick={() => router.push("/privacy-policy")}
                />
                <hr />
                <button
                  onClick={async () => {
                    try {
                      await authService.logout();
                    } catch {
                      localStorage.removeItem("accessToken");
                      window.location.href = "/";
                      return;
                    }
                    localStorage.removeItem("accessToken");
                    window.location.href = "/";
                  }}
                  className="w-full flex justify-center gap-2 text-red-500 hover:text-red-600"
                >
                  <LogOut className="w-4 h-4" />
                  로그아웃
                </button>
              </div>
            </div>
          )}
        </div>
      </header>

      {/* 업로드 모달 */}
      {showUploadModal && (
        <div className="fixed inset-0 bg-black/40 z-50 flex justify-center items-center">
          <div className="bg-white w-[90%] max-w-md rounded-2xl p-6 shadow-xl relative">
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

            {/* File Upload */}
            <label
              onDragOver={(e) => e.preventDefault()}
              onDrop={(e) => {
                e.preventDefault();
                const f = e.dataTransfer.files?.[0];
                if (f) handleUploadProcess(f);
              }}
              className="flex flex-col items-center justify-center border-2 border-dashed border-[#D2C9B0] rounded-xl p-8 cursor-pointer"
            >
              <Upload className="w-10 h-10 mb-3" />
              <p className="font-medium mb-1">
                여기로 파일을 드래그하거나 클릭하세요
              </p>
              <p className="text-sm text-gray-500">이미지/텍스트 업로드 가능</p>

              <input
                type="file"
                onChange={(e) => {
                  const f = e.target.files?.[0];
                  if (f) handleUploadProcess(f);
                }}
                className="hidden"
              />
            </label>

            {uploading && (
              <div className="text-center text-sm mt-3">업로드 중입니다...</div>
            )}
            {uploadedFile && (
              <div className="text-center text-green-600 font-semibold mt-3">
                업로드 완료!
              </div>
            )}
          </div>
        </div>
      )}
    </>
  );
}

/* -------------------- 공용 메뉴 아이템 -------------------- */
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
      className="flex justify-between items-center w-full px-3 py-2 rounded-lg hover:bg-[#F7F4EC]"
    >
      <div className="flex items-center gap-2">
        {icon}
        <span>{text}</span>
      </div>
      <span className="text-gray-400">›</span>
    </button>
  );
}
