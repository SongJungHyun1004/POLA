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
  fetchCategoryTags,
  updateCategoryName,
  addCategoryTags,
  removeCategoryTag,
} from "@/services/categoryService";

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

interface SortableItemProps {
  file: any;
  selectedId: number | null;
  onSelect: (file: any) => void;
}

const PolaroidItem = memo(
  ({ file, selectedId, onSelect }: SortableItemProps) => {
    const isSelected = selectedId === file.id;

    return (
      <div className="flex justify-center w-full overflow-visible">
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
          <PolaroidCard
            src={file.src || "/images/dummy_image_1.png"}
            type={file.type}
            ocr_text={file.ocr_text}
          />

          {file.favorite && (
            <Star
              fill="#FFD700"
              stroke="#FFD700"
              strokeWidth={2.5}
              className="absolute top-2 right-2 drop-shadow-sm w-6 h-6 z-10"
            />
          )}
        </button>
      </div>
    );
  }
);
PolaroidItem.displayName = "PolaroidItem";

export default function CategoryPage() {
  const params = useParams();
  if (typeof params.id !== "string") {
    return (
      <div className="p-10 text-center text-xl text-red-600">
        잘못된 접근입니다. (id 없음)
      </div>
    );
  }

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

  /* ================================
     카테고리 메타데이터 로드
     ================================ */
  useEffect(() => {
    async function loadMeta() {
      try {
        const info = await getCategoryInfo(id);
        setCategoryName(info.categoryName ?? "");

        const tagList = await fetchCategoryTags(id);
        setTags(tagList.map((t: any) => t.tagName));
      } catch {
        alert("카테고리 정보를 불러오는 중 오류 발생");
      }
    }
    loadMeta();
  }, [id]);

  /* ================================
     파일 무한 스크롤 로딩
     ================================ */
  async function loadMoreFiles(targetPage?: number) {
    if (isFetching || !hasMore) return;

    const currentPage = targetPage ?? page;

    try {
      setIsFetching(true);
      const newFiles = await getCategoryFiles(id, currentPage);

      if (!newFiles || newFiles.length === 0) {
        setHasMore(false);
        return;
      }

      const rotated = newFiles.map((f: any) => ({
        ...f,
        rotation: `rotate(${Math.random() * 8 - 4}deg)`,
      }));

      setFiles((prev) => {
        const merged = [...prev, ...rotated];
        return merged.filter(
          (v, i, arr) => arr.findIndex((t) => t.id === v.id) === i
        );
      });

      setPage(currentPage + 1);
    } finally {
      setIsFetching(false);
    }
  }

  // 카테고리 변경 시 처음부터 다시 로드
  useEffect(() => {
    setFiles([]);
    setPage(0);
    setHasMore(true);
    loadMoreFiles(0);
    setSelectedFile(null);
  }, [id]);

  // 스크롤 리스너
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
  }, [hasMore, isFetching]);

  /* ================================
     파일 선택 및 상세 정보 로딩
     ================================ */
  const handleSelectFile = async (file: any) => {
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

    try {
      const detail = await getFileDetail(file.id);
      setSelectedFile({
        id: detail.id,
        src: detail.src,
        tags: (detail.tags ?? []).map((t: any) => `#${t.tagName}`),
        context: detail.context ?? "",
        created_at: detail.created_at,
        category_id: detail.category_id,
        favorite: detail.favorite ?? file.favorite,
        type: detail.type,
        platform: detail.platform,
        ocr_text: detail.ocr_text,
      });
    } catch {
      // 무시
    }
  };

  useEffect(() => {
    if (files.length > 0 && !selectedFile) {
      handleSelectFile(files[0]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [files]);

  /* ================================
     Detail → CategoryPage 반영
     ================================ */
  const handleFavoriteChange = (state: boolean) => {
    if (!selectedFile) return;

    setSelectedFile((prev) => prev && { ...prev, favorite: state });
    setFiles((prev) =>
      prev.map((f) =>
        f.id === selectedFile.id ? { ...f, favorite: state } : f
      )
    );
  };

  // 삭제 후 리스트에서 제거 + 다음 파일 선택
  const handleFileDeleted = (fileId: number) => {
    setFiles((prev) => {
      const filtered = prev.filter((f) => f.id !== fileId);

      if (selectedFile?.id === fileId) {
        const next = filtered[0];
        if (next) {
          handleSelectFile(next);
        } else {
          setSelectedFile(null);
        }
      }

      return filtered;
    });
  };

  // 카테고리/태그/파일 편집 후 전체 재로드
  const handleCategoryUpdated = async () => {
    try {
      // 카테고리 이름/태그 메타 갱신
      const info = await getCategoryInfo(id);
      setCategoryName(info.categoryName ?? "");

      const tagList = await fetchCategoryTags(id);
      setTags(tagList.map((t: any) => t.tagName));

      // 파일 목록 처음부터 다시 로드
      setFiles([]);
      setPage(0);
      setHasMore(true);
      setSelectedFile(null);
      await loadMoreFiles(0);

      await refreshCategories();
    } catch (e) {
      console.error(e);
    }
  };

  // 카테고리 모달 저장 (이름/태그 수정)
  const handleCategoryModalSave = async (
    newName: string,
    newTags: string[]
  ) => {
    try {
      // 1) 이름 변경
      if (newName !== categoryName) {
        await updateCategoryName(id, newName);
        setCategoryName(newName);
      }

      // 2) 태그 동기화
      const currentTagList = await fetchCategoryTags(id); // [{id, tagName}]
      const currentTagNames = currentTagList.map((t: any) => t.tagName);

      const toAdd = newTags.filter((t) => !currentTagNames.includes(t));
      if (toAdd.length > 0) {
        await addCategoryTags(id, toAdd);
      }

      const toRemove = currentTagList.filter(
        (t: any) => !newTags.includes(t.tagName)
      );
      for (const r of toRemove) {
        await removeCategoryTag(id, r.id);
      }

      setTags(newTags);
      await refreshCategories();
      alert("카테고리가 성공적으로 수정되었습니다!");
    } catch (e) {
      console.error(e);
      alert("카테고리 수정 중 오류가 발생했습니다.");
    }
  };

  /* ================================
     렌더링
     ================================ */
  return (
    <>
      <div className="w-full h-full flex justify-center bg-[#FFFEF8] text-[#4C3D25]">
        <div className="w-full max-w-[1200px] h-full flex gap-8 p-6">
          {/* LEFT LIST */}
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
                  <span key={index} className="whitespace-nowrap">
                    #{t}
                  </span>
                ))}
              </div>
            </div>

            <div
              ref={containerRef}
              className="flex-1 overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-[#CBBF9E]/50"
            >
              {files.length === 0 && !isFetching ? (
                <div className="flex flex-col items-center justify-center py-20 opacity-80">
                  <img
                    src="/images/POLA_file_empty.png"
                    alt="empty"
                    className="w-80 h-80 object-contain"
                  />
                  <p className="text-lg text-[#7A6A48] mt-4">
                    더 이상 표시할 컨텐츠가 없습니다
                  </p>
                </div>
              ) : (
                <div
                  className="
                      grid gap-6 pt-12 px-10 pb-10
                      grid-cols-1 sm:grid-cols-2 md:grid-cols-3
                      lg:grid-cols-4 xl:grid-cols-5
                      place-items-center
                    "
                >
                  {files.map((file) => (
                    <PolaroidItem
                      key={file.id}
                      file={file}
                      selectedId={selectedFile?.id ?? null}
                      onSelect={handleSelectFile}
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

          {/* RIGHT PANEL */}
          {selectedFile && (
            <div
              className="
                w-[400px] 
                flex-shrink-0 
                border-l border-[#E3DCC8] 
                pl-6 
                flex 
                items-start
                justify-center
                pt-4
                overflow-y-auto
                scrollbar-thin scrollbar-thumb-[#CBBF9E]/50
              "
            >
              <PolaroidDetail
                id={selectedFile.id}
                src={selectedFile.src}
                type={selectedFile.type}
                platform={selectedFile.platform}
                ocr_text={selectedFile.ocr_text}
                tags={selectedFile.tags}
                date={selectedFile.created_at}
                contexts={selectedFile.context}
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

      {/* EDIT CATEGORY MODAL */}
      <CategoryModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleCategoryModalSave}
        onDelete={() => {}}
        defaultName={categoryName}
        defaultTags={tags}
        isEditing={true}
        showDeleteButton={false}
      />
    </>
  );
}
