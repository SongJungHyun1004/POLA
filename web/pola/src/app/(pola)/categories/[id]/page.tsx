// --- CategoryPage.tsx (수정본 전문) ---
"use client";

import { useParams } from "next/navigation";
import { useState, useEffect, useRef, memo } from "react";
import PolaroidCard from "@/app/(pola)/home/components/PolaroidCard";
import PolaroidDetail from "./components/PolaroidDetail";
import { Pencil, Star } from "lucide-react";

import {
  getCategoryInfo,
  getCategoryFiles,
  getFileDetail,
  updateCategoryName,
  fetchCategoryTags,
  addCategoryTags,
  removeCategoryTag,
} from "@/services/categoryService";

import { removeFileFavorite } from "@/services/fileService";

import CategoryModal from "@/app/onboarding/components/CategoryModal";
import useCategoryStore from "@/store/useCategoryStore";

interface SelectedFile {
  id: number;
  src: string;
  tags: string[];
  context: string;
  created_at: string;
  category_id?: number;
  favorite: boolean;
  type?: string;
  platform?: string;
  ocr_text?: string;
}

/* ===========================================================
    PolaroidItem (리스트 아이템)
    - ⭐(Star) 클릭→ 즐겨찾기 해제 기능 추가
=========================================================== */
interface SortableItemProps {
  file: any;
  selectedId: number | null;
  onSelect: (file: any) => void;
  onUnfavorite: (fileId: number) => void; // ⭐해제
}

const PolaroidItem = memo(
  ({ file, selectedId, onSelect, onUnfavorite }: SortableItemProps) => {
    const isSelected = selectedId === file.id;

    return (
      <div className="flex justify-center w-full overflow-visible relative">
        <button
          onClick={() => onSelect(file)}
          className={`relative transition-transform ${
            isSelected ? "scale-110 z-20" : "hover:scale-[1.07]"
          }`}
          style={{
            transform: `${file.rotation} ${isSelected ? "scale(1.1)" : ""}`,
            transformOrigin: "center bottom",
          }}
        >
          {/* ⭐ 버튼이 아닌 div로 변경 */}
          {file.favorite && (
            <div
              onClick={(e) => {
                e.stopPropagation();
                onUnfavorite(file.id);
              }}
              className="absolute top-2 right-2 z-20 cursor-pointer"
            >
              <Star
                fill="#FFD700"
                stroke="#FFD700"
                className="w-6 h-6 drop-shadow-sm"
              />
            </div>
          )}

          <PolaroidCard
            src={file.src}
            type={file.type}
            ocr_text={file.ocr_text}
          />
        </button>
      </div>
    );
  }
);
PolaroidItem.displayName = "PolaroidItem";

/* ===========================================================
    CategoryPage 본문
=========================================================== */
export default function CategoryPage() {
  const params = useParams();
  const id = Number(params.id);

  const [categoryName, setCategoryName] = useState("");
  const [tags, setTags] = useState<string[]>([]);
  const [files, setFiles] = useState<any[]>([]);
  const [selectedFile, setSelectedFile] = useState<SelectedFile | null>(null);

  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isFetching, setIsFetching] = useState(false);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const refreshCategories = useCategoryStore((s) => s.refreshCategories);

  /* ---------------------------- 카테고리 메타 ---------------------------- */
  useEffect(() => {
    async function loadMeta() {
      const info = await getCategoryInfo(id);
      setCategoryName(info.categoryName);

      const tagList = await fetchCategoryTags(id);
      setTags(tagList.map((t: any) => t.tagName));
    }
    loadMeta();
  }, [id]);

  /* ---------------------------- 파일 로드 ---------------------------- */
  async function loadMoreFiles(forceReset = false) {
    if (isFetching || (!hasMore && !forceReset)) return;

    setIsFetching(true);

    try {
      const newPage = forceReset ? 0 : page;
      const newFiles = await getCategoryFiles(id, newPage);

      if (!newFiles || newFiles.length === 0) {
        setHasMore(false);
        return;
      }

      const rotated = newFiles.map((f: any) => ({
        ...f,
        rotation: `rotate(${Math.random() * 8 - 4}deg)`,
      }));

      setFiles((prev) => {
        const merged = forceReset ? rotated : [...prev, ...rotated];
        return merged.filter(
          (v, i, arr) => arr.findIndex((t) => t.id === v.id) === i
        );
      });

      setPage((prev) => (forceReset ? 1 : prev + 1));
    } finally {
      setIsFetching(false);
    }
  }

  useEffect(() => {
    // 카테고리 변경 시 초기화
    setFiles([]);
    setPage(0);
    setHasMore(true);
    loadMoreFiles(true);
  }, [id]);

  /* ---------------------------- 스크롤 감지 ---------------------------- */
  useEffect(() => {
    const box = containerRef.current;
    if (!box) return;

    const onScroll = () => {
      if (box.scrollTop + box.clientHeight >= box.scrollHeight - 300) {
        loadMoreFiles();
      }
    };

    box.addEventListener("scroll", onScroll);
    return () => box.removeEventListener("scroll", onScroll);
  }, []);

  /* ---------------------------- 파일 선택 ---------------------------- */
  async function handleSelectFile(file: any) {
    setSelectedFile({
      id: file.id,
      src: file.src,
      favorite: file.favorite,
      tags: [],
      context: "",
      created_at: "",
      type: file.type,
      platform: file.platform,
      ocr_text: file.ocr_text,
    });

    const detail = await getFileDetail(file.id);
    setSelectedFile({
      id: detail.id,
      src: detail.src,
      tags: detail.tags.map((t: any) => `#${t.tagName}`),
      context: detail.context,
      created_at: detail.created_at,
      category_id: detail.category_id,
      favorite: detail.favorite,
      type: detail.type,
      platform: detail.platform,
      ocr_text: detail.ocr_text,
    });
  }

  useEffect(() => {
    if (files.length > 0 && !selectedFile) {
      handleSelectFile(files[0]);
    }
  }, [files]);

  /* ---------------------------- FAVORITE (Detail 쪽) ---------------------------- */
  const handleFavoriteChange = (state: boolean) => {
    if (!selectedFile) return;

    setSelectedFile((prev) => prev && { ...prev, favorite: state });

    setFiles((prev) =>
      prev.map((f) =>
        f.id === selectedFile.id ? { ...f, favorite: state } : f
      )
    );
  };

  /* ---------------------------- FAVORITE 해제 (List) ---------------------------- */
  async function handleUnfavorite(fileId: number) {
    try {
      await removeFileFavorite(fileId);

      setFiles((prev) =>
        prev.map((f) => (f.id === fileId ? { ...f, favorite: false } : f))
      );

      if (selectedFile?.id === fileId) {
        setSelectedFile((prev) => prev && { ...prev, favorite: false });
      }
    } catch {
      alert("즐겨찾기 해제 실패");
    }
  }

  /* ---------------------------- 삭제 ---------------------------- */
  const handleFileDeleted = (fileId: number) => {
    setFiles((prev) => prev.filter((f) => f.id !== fileId));

    if (selectedFile?.id === fileId) {
      const next = files.find((f) => f.id !== fileId);
      setSelectedFile(next ?? null);
    }
  };

  /* ---------------------------- 수정 반영 ---------------------------- */
  const handleCategoryUpdated = async () => {
    const info = await getCategoryInfo(id);
    setCategoryName(info.categoryName);

    const tagList = await fetchCategoryTags(id);
    setTags(tagList.map((t: any) => t.tagName));

    await loadMoreFiles(true);
    await refreshCategories();
  };

  const handleCategorySave = async (newName: string, newTags: string[]) => {
    try {
      /* -----------------------------------------
       * 1) 현재 카테고리 데이터 불러오기
       * ----------------------------------------- */
      const current = await getCategoryInfo(id);
      const currentTags = await fetchCategoryTags(id);
      const currentTagNames = currentTags.map((t: any) => t.tagName);

      /* -----------------------------------------
       * 2) 카테고리 이름 변경
       * ----------------------------------------- */
      if (current.categoryName !== newName) {
        await updateCategoryName(id, newName);
        setCategoryName(newName); // UI 즉시 반영
      }

      /* -----------------------------------------
       * 3) 태그 추가 / 삭제 적용
       * ----------------------------------------- */

      // 추가해야 하는 태그
      const toAdd = newTags.filter((t) => !currentTagNames.includes(t));
      if (toAdd.length > 0) {
        await addCategoryTags(id, toAdd);
      }

      // 삭제해야 하는 태그
      const toRemove = currentTags.filter(
        (t: any) => !newTags.includes(t.tagName)
      );
      for (const r of toRemove) {
        await removeCategoryTag(id, r.id);
      }

      /* -----------------------------------------
       * 4) 화면 갱신
       * ----------------------------------------- */
      setTags(newTags);

      await loadMoreFiles(true);
      await refreshCategories();

      alert("카테고리가 수정되었습니다.");
    } catch (error) {
      console.error(error);
      alert("카테고리 수정 중 오류가 발생했습니다.");
    }
  };

  /* ---------------------------- Render ---------------------------- */
  return (
    <>
      <div className="w-full h-full flex justify-center bg-[#FFFEF8] text-[#4C3D25]">
        <div className="w-full max-w-[1200px] h-full flex gap-8 p-6">
          {/* LEFT */}
          <div className="w-full flex-1 flex flex-col overflow-hidden">
            <div className="w-full mb-2 pl-4">
              <div className="flex w-full items-center justify-between mb-6">
                <h2 className="text-5xl font-bold">{categoryName}</h2>
                <button
                  className="p-2 rounded-full hover:bg-[#EDE6D8]"
                  onClick={() => setIsModalOpen(true)}
                >
                  <Pencil className="w-5 h-5" />
                </button>
              </div>

              <div className="text-2xl text-[#7A6A48] flex flex-wrap gap-x-2">
                {tags.map((t, index) => (
                  <span key={index}>#{t}</span>
                ))}
              </div>
            </div>
            <div
              ref={containerRef}
              className="flex-1 overflow-y-auto pr-2 scrollbar-thin"
            >
              {/* 🟫 Empty UI */}
              {!isFetching && files.length === 0 && (
                <div className="flex flex-col items-center justify-center py-20 opacity-80 w-full">
                  <img
                    src="/images/POLA_file_empty.png"
                    alt="empty"
                    className="w-72 h-72 object-contain mb-6"
                  />
                  <p className="text-lg text-[#7A6A48]">
                    아직 이 카테고리에 파일이 없습니다
                  </p>
                </div>
              )}

              {/* 🟦 File Grid */}
              {files.length > 0 && (
                <div
                  className="grid gap-6 pt-12 px-10 pb-10
      grid-cols-1 sm:grid-cols-2 md:grid-cols-3
      lg:grid-cols-4 xl:grid-cols-5 place-items-center"
                >
                  {files.map((file) => (
                    <PolaroidItem
                      key={file.id}
                      file={file}
                      selectedId={selectedFile?.id ?? null}
                      onSelect={handleSelectFile}
                      onUnfavorite={handleUnfavorite}
                    />
                  ))}
                </div>
              )}

              {isFetching && (
                <div className="text-center text-[#7A6A48] py-4 animate-pulse">
                  불러오는 중...
                </div>
              )}

              {!isFetching && !hasMore && files.length > 0 && (
                <div className="text-center text-[#7A6A48] py-4">
                  더 이상 데이터가 없습니다.
                </div>
              )}
            </div>
          </div>

          {/* RIGHT */}
          {selectedFile && (
            <div
              className="w-[400px] flex-shrink-0 border-l pl-6 pt-4
              overflow-y-auto scrollbar-thin"
            >
              <PolaroidDetail
                id={selectedFile.id}
                src={selectedFile.src}
                type={selectedFile.type}
                platform={selectedFile.platform}
                tags={selectedFile.tags}
                ocr_text={selectedFile.ocr_text}
                contexts={selectedFile.context}
                date={selectedFile.created_at}
                categoryId={selectedFile.category_id}
                favorite={selectedFile.favorite}
                onFavoriteChange={handleFavoriteChange}
                onCategoryUpdated={handleCategoryUpdated}
                onFileDeleted={handleFileDeleted}
              />
            </div>
          )}
        </div>
      </div>

      <CategoryModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleCategorySave}
        onDelete={() => {}}
        defaultName={categoryName}
        defaultTags={tags}
        isEditing={true}
        showDeleteButton={false}
      />
    </>
  );
}
