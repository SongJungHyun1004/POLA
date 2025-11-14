"use client";

import { useState, useRef, useEffect, KeyboardEvent, MouseEvent } from "react";
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
  ChevronDown,
  FolderHeart,
  Puzzle,
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
  const [aiQuery, setAiQuery] = useState("");
  const [aiMode, setAiMode] = useState(false);

  /** 검색 모드: 통합 / 태그 */
  const [searchMode, setSearchMode] = useState<SearchMode>("INTEGRATED");
  const [modeDropdownOpen, setModeDropdownOpen] = useState(false);

  /** 자동완성 */
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [highlightIndex, setHighlightIndex] = useState<number>(-1);

  /** 프로필 / 업로드 모달 */
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadedFile, setUploadedFile] = useState(false);

  const profileRef = useRef<HTMLDivElement>(null);
  const modeDropdownRef = useRef<HTMLDivElement>(null);
  const searchWrapperRef = useRef<HTMLDivElement>(null);

  /* -------------------- 유틸: 최근 검색어 -------------------- */
  const getSearchHistory = (): string[] => {
    if (typeof window === "undefined") return [];
    try {
      return JSON.parse(localStorage.getItem(SEARCH_HISTORY_KEY) || "[]");
    } catch {
      return [];
    }
  };

  const saveSearchHistory = (term: string) => {
    if (!term.trim()) return;
    const prev = getSearchHistory().filter((t) => t !== term);
    const next = [term, ...prev].slice(0, 20);
    localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(next));
  };

  /* -------------------- 자동완성 로직 -------------------- */
  useEffect(() => {
    // 입력이 비었으면 자동완성 숨김
    if (!query.trim()) {
      setSuggestions([]);
      setShowSuggestions(false);
      setHighlightIndex(-1);
      return;
    }

    async function load() {
      if (searchMode === "TAG") {
        // 태그 검색: API 호출
        try {
          const tags = await fetchTagSuggestions(query);
          setSuggestions(tags);
          setShowSuggestions(tags.length > 0);
          setHighlightIndex(tags.length > 0 ? 0 : -1);
        } catch (e) {
          console.error("태그 자동완성 실패:", e);
          setSuggestions([]);
          setShowSuggestions(false);
          setHighlightIndex(-1);
        }
      } else {
        // 통합 검색: 로컬스토리지 기반
        const history = getSearchHistory();
        const filtered = history.filter((t) => t.includes(query));
        setSuggestions(filtered);
        setShowSuggestions(filtered.length > 0);
        setHighlightIndex(filtered.length > 0 ? 0 : -1);
      }
    }

    load();
  }, [query, searchMode]);

  /* -------------------- 키보드 네비게이션 -------------------- */
  const handleSearchInputKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (!showSuggestions || suggestions.length === 0) {
      if (e.key === "Enter") {
        // 자동완성 없을 때는 바로 검색
        e.preventDefault();
        doSearch();
      }
      return;
    }

    if (e.key === "ArrowDown") {
      e.preventDefault();
      setHighlightIndex((prev) =>
        prev < suggestions.length - 1 ? prev + 1 : prev
      );
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setHighlightIndex((prev) => (prev > 0 ? prev - 1 : prev));
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (highlightIndex >= 0 && highlightIndex < suggestions.length) {
        const value = suggestions[highlightIndex];
        setQuery(value);
        // 자동완성 값만 입력창에 채우고, 검색은 실행하지 않음
        setShowSuggestions(false);
        setHighlightIndex(-1);
      } else {
        doSearch();
      }
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
      const params = new URLSearchParams();
      params.append("search", query);
      router.push(`/files?${params.toString()}`);
    } else {
      const params = new URLSearchParams();
      params.append("tag", query);
      router.push(`/files?${params.toString()}`);
    }

    setShowSuggestions(false);
  };

  /* -------------------- AI 검색 -------------------- */
  const doAISearch = () => {
    if (!aiQuery.trim()) return;

    const q = encodeURIComponent(aiQuery.trim());
    router.push(`/ragsearch?query=${q}`);

    setAiMode(false);
  };

  /* -------------------- 외부 클릭 처리 -------------------- */
  useEffect(() => {
    const handleClick = (e: MouseEvent | globalThis.MouseEvent) => {
      const target = e.target as Node;

      // 프로필 모달
      if (profileRef.current && !profileRef.current.contains(target)) {
        setShowProfileModal(false);
      }

      // 검색 모드 드롭다운
      if (
        modeDropdownRef.current &&
        !modeDropdownRef.current.contains(target)
      ) {
        setModeDropdownOpen(false);
      }

      // 자동완성 (검색 영역 밖 클릭 시 닫기)
      if (
        searchWrapperRef.current &&
        !searchWrapperRef.current.contains(target)
      ) {
        setShowSuggestions(false);
        setHighlightIndex(-1);
      }
    };

    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  /* -------------------- 붙여넣기 업로드 -------------------- */
  useEffect(() => {
    const handlePaste = (e: ClipboardEvent) => {
      const items = e.clipboardData?.items;
      if (!items) return;

      for (const item of items) {
        if (item.kind === "file") {
          const file = item.getAsFile();
          if (file) handleUploadProcess(file);
        }
      }
    };

    window.addEventListener("paste", handlePaste);
    return () => window.removeEventListener("paste", handlePaste);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /* -------------------- 텍스트 파일 UTF-8 변환 -------------------- */
  function convertTextFileToUTF8(file: File): Promise<File> {
    return new Promise((resolve) => {
      const reader = new FileReader();

      reader.readAsText(file, "utf-8");

      reader.onload = () => {
        const utf8Blob = new Blob([reader.result as string], {
          type: "text/plain; charset=utf-8",
        });

        const utf8File = new File([utf8Blob], file.name, {
          type: "text/plain; charset=utf-8",
        });

        resolve(utf8File);
      };
    });
  }

  /* -------------------- 업로드 전체 프로세스 -------------------- */
  async function handleUploadProcess(file: File) {
    try {
      setUploading(true);
      setUploadedFile(false);

      let uploadFile = file;
      if (file.type === "text/plain") {
        console.log("텍스트 파일 감지 → UTF-8 변환 실행");
        uploadFile = await convertTextFileToUTF8(file);
        console.log("UTF-8 변환 완료:", uploadFile);
      }

      const { url, key } = await uploadService.getPresignedUploadUrl(
        uploadFile.name
      );

      await uploadService.uploadToS3(url, uploadFile);

      const originUrl = url.split("?")[0];
      const completeData = await uploadService.completeUpload({
        key,
        type: uploadFile.type,
        fileSize: uploadFile.size,
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

  const modeLabel = searchMode === "INTEGRATED" ? "통합 검색" : "태그 검색";

  /* -------------------- 로그인 후 헤더 -------------------- */
  return (
    <>
      <header className="relative flex justify-between items-center w-full pb-10 px-8 pt-6 bg-[#FFFEF8]">
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

        {/* 검색 섹션 (기존 AI 버튼/애니메이션 포함) */}
        <div
          ref={searchWrapperRef}
          className="relative flex items-center w-1/2 gap-3"
        >
          {/* 기본 검색창 */}
          <div
            className={`transition-all duration-300 flex items-center bg-white border rounded-full ${
              aiMode
                ? "w-10 h-10 justify-center p-0"
                : "flex-grow px-4 py-2 border"
            }`}
          >
            {!aiMode ? (
              <>
                {/* 검색 모드 드롭다운 */}
                <div className="relative" ref={modeDropdownRef}>
                  <button
                    type="button"
                    onClick={() => setModeDropdownOpen((prev) => !prev)}
                    className="flex items-center gap-1 font-semibold text-[#4C3D25]"
                  >
                    {modeLabel}
                    <ChevronDown className="w-4 h-4" />
                  </button>

                  {modeDropdownOpen && (
                    <div className="absolute top-[120%] left-0 bg-white border rounded-xl shadow-lg z-50 py-1 w-32">
                      <button
                        type="button"
                        onClick={() => {
                          setSearchMode("INTEGRATED");
                          setModeDropdownOpen(false);
                          setQuery("");
                          setSuggestions([]);
                          setHighlightIndex(-1);
                        }}
                        className="block w-full text-left px-3 py-2 hover:bg-gray-100"
                      >
                        통합 검색
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setSearchMode("TAG");
                          setModeDropdownOpen(false);
                          setQuery("");
                          setSuggestions([]);
                          setHighlightIndex(-1);
                        }}
                        className="block w-full text-left px-3 py-2 hover:bg-gray-100"
                      >
                        태그 검색
                      </button>
                    </div>
                  )}
                </div>

                {/* 검색어 입력 */}
                <input
                  type="text"
                  placeholder="검색어 입력"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyDown={handleSearchInputKeyDown}
                  onFocus={() => {
                    if (suggestions.length > 0) {
                      setShowSuggestions(true);
                    }
                  }}
                  className="flex-grow outline-none text-tertiary placeholder:text-tertiary/50 ml-3"
                />

                {/* 검색 버튼 */}
                <button
                  type="button"
                  onClick={doSearch}
                  className="text-tertiary hover:text-black transition"
                >
                  <Search className="w-5 h-5" />
                </button>
              </>
            ) : (
              <>
                {/* 🔥 AI 모드일 때 왼쪽 동그란 버튼: AI 모드 종료 */}
                <button
                  type="button"
                  onClick={() => {
                    setAiMode(false);
                    setShowSuggestions(false);
                    setHighlightIndex(-1);
                  }}
                  className="text-tertiary hover:text-black transition"
                >
                  <Search className="w-5 h-5" />
                </button>
              </>
            )}
          </div>

          {/* AI 검색 박스 (기존 애니메이션 유지) */}
          <div
            className={`bg-white border rounded-full flex items-center transition-all duration-300 overflow-hidden ${
              aiMode ? "flex-grow px-4 py-2" : "w-10 h-10 justify-center"
            }`}
          >
            {aiMode ? (
              <>
                {/* AI 입력창 */}
                <input
                  type="text"
                  placeholder="AI를 통한 자연어 검색"
                  value={aiQuery}
                  onChange={(e) => setAiQuery(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && doAISearch()}
                  className="flex-grow outline-none placeholder:text-tertiary/50 animate-fade-slide-in"
                />

                {/* 🔥 AI 검색 실행 버튼 (Send 아이콘 유지) */}
                <button
                  type="button"
                  onClick={doAISearch}
                  className="text-tertiary hover:text-black transition"
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

          {/* 자동완성 박스 (검색창 아래로, body를 밀지 않도록 absolute) */}
          {showSuggestions && suggestions.length > 0 && (
            <div className="absolute left-0 right-0 top-[100%] mt-2 bg-white border rounded-2xl shadow-lg z-40 p-4 max-h-80 overflow-y-auto">
              {suggestions.map((s, idx) => (
                <button
                  key={`${s}-${idx}`}
                  type="button"
                  className={`w-full text-left px-3 py-2 rounded-lg ${
                    highlightIndex === idx ? "bg-gray-200" : "hover:bg-gray-100"
                  }`}
                  // onMouseDown 을 써야 input blur 전에 처리 가능
                  onMouseDown={(e) => {
                    e.preventDefault();
                    setQuery(s);
                    setShowSuggestions(false);
                    setHighlightIndex(-1);
                  }}
                >
                  {searchMode === "TAG" ? `#${s}` : s}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* 프로필 영역 (기존 그대로) */}
        <div ref={profileRef} className="relative flex items-center gap-3">
          <button
            onClick={() => setShowProfileModal((prev) => !prev)}
            className="flex items-center gap-3 cursor-pointer hover:opacity-80 transition"
          >
            <span className="font-medium">{user.display_name}</span>
            <div className="relative w-10 h-10 bg-white rounded-full border overflow-hidden">
              <img
                src={user.profile_image_url || "/images/default_profile.png"}
                alt="profile"
                className="object-cover w-full h-full"
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
                {/* 내 정보 */}
                <div>
                  <p className="text-sm font-semibold mb-2">내 정보</p>

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
                </div>

                <hr />

                {/* 기타 */}
                <div>
                  <p className="text-sm font-semibold mb-2">기타</p>

                  <MenuItem
                    icon={<Puzzle />}
                    text="POLA 익스텐션"
                    onClick={() => {
                      window.open(
                        "https://chrome.google.com/webstore",
                        "_blank"
                      );
                      setShowProfileModal(false);
                    }}
                  />

                  <MenuItem
                    icon={<FileText />}
                    text="개인정보 처리방침"
                    onClick={() => router.push("/privacy-policy")}
                  />
                </div>

                <hr />

                {/* 로그아웃 */}
                <button
                  onClick={async () => {
                    try {
                      await authService.logout();
                    } catch (err) {
                      console.error(err);
                      alert("로그아웃 중 오류가 발생했습니다.");
                      localStorage.removeItem("accessToken");
                      window.location.href = "/";
                      return;
                    }

                    localStorage.removeItem("accessToken");
                    window.location.href = "/";
                  }}
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

      {/* 업로드 모달 */}
      {showUploadModal && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex justify-center items-center">
          <div className="bg-white w-[90%] max-w-md rounded-2xl p-6 shadow-xl animate-fade-slide-in relative">
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

            {/* Drag & Drop 영역 */}
            <label
              onDragOver={(e) => e.preventDefault()}
              onDrop={(e) => {
                e.preventDefault();
                const file = e.dataTransfer.files?.[0];
                if (file) handleUploadProcess(file);
              }}
              className="flex flex-col items-center justify-center border-2 border-dashed border-[#D2C9B0] rounded-xl p-8 text-[#7A6A48] cursor-pointer"
            >
              <Upload className="w-10 h-10 mb-3" />
              <p className="font-medium mb-1">
                여기로 파일을 드래그하거나 클릭하세요
              </p>
              <p className="text-sm text-gray-500">
                이미지(PNG/JPG), 텍스트 파일만 업로드 가능합니다.
              </p>

              <input
                type="file"
                accept="image/png, image/jpg, text/plain"
                className="hidden"
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (file) handleUploadProcess(file);
                }}
              />
            </label>

            {/* 로딩 표시 */}
            {uploading && (
              <div className="mt-4 text-center text-sm text-gray-600">
                업로드 중입니다... 잠시만 기다려주세요.
              </div>
            )}

            {uploadedFile && (
              <div className="mt-4 text-sm text-green-700 font-semibold text-center">
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
