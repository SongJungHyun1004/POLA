"use client";

import { useState, useEffect, useRef, memo } from "react";
import { Star } from "lucide-react";
import {
  DndContext,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  useSortable,
  rectSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

import PolaroidCard from "@/app/(pola)/home/components/PolaroidCard";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";

import { getFavoriteFiles } from "@/services/favoriteService";
import { getFileDetail } from "@/services/categoryService";
import { removeFileFavorite, addFileFavorite } from "@/services/fileService";

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
  onUnfavorite: (id: number) => void; // ⭐ 리스트에서 해제 기능
}

/* ============================================================
    SortableItem — CategoryPage 방식 그대로 ⭐ 적용!
============================================================ */
const SortableItem = memo(
  ({ file, selectedId, onSelect, onUnfavorite }: SortableItemProps) => {
    const {
      attributes,
      listeners,
      setNodeRef,
      transform,
      transition,
      isDragging,
    } = useSortable({ id: file.id });

    const isSelected = selectedId === file.id;

    const style = {
      transform: CSS.Transform.toString(transform),
      transition: isDragging ? "none" : transition || "transform 0.2s ease",
      transformOrigin: "center bottom",
      zIndex: isDragging ? 50 : isSelected ? 30 : 1,
    } as const;

    return (
      <div
        ref={setNodeRef}
        className="flex justify-center w-full overflow-visible relative"
        style={style}
      >
        <button
          {...attributes}
          {...listeners}
          onClick={() => onSelect(file)}
          className={`relative transition-transform ${
            isSelected ? "scale-110 z-20" : "hover:scale-[1.07]"
          }`}
          style={{
            transform: `${file.rotation} ${isSelected ? "scale(1.1)" : ""}`,
            transformOrigin: "center bottom",
          }}
        >
          {/* ⭐ 버튼이 아닌 div (CategoryPage와 동일) */}
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
                strokeWidth={2.5}
                className="w-6 h-6 drop-shadow-sm"
              />
            </div>
          )}

          <PolaroidCard
            src={file.src || "/images/dummy_image_1.png"}
            type={file.type}
            ocr_text={file.ocr_text}
          />
        </button>
      </div>
    );
  }
);
SortableItem.displayName = "SortableItem";

/* ============================================================
                FavoritePage 본문
============================================================ */
export default function FavoritePage() {
  const [files, setFiles] = useState<any[]>([]);
  const [selectedFile, setSelectedFile] = useState<SelectedFile | null>(null);

  const [page, setPage] = useState(0);
  const [isFetching, setIsFetching] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  const containerRef = useRef<HTMLDivElement>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { delay: 150, tolerance: 5 },
    })
  );

  /* -----------------------------------------------------------
      ⭐ 즐겨찾기 해제
      FavoritePage에서는 해제 = 리스트에서 제거
  ------------------------------------------------------------ */
  const handleUnfavorite = async (fileId: number) => {
    try {
      await removeFileFavorite(fileId);

      // 리스트에서 제거
      setFiles((prev) => prev.filter((f) => f.id !== fileId));

      // Detail에서도 반영
      if (selectedFile?.id === fileId) {
        const remaining = files.filter((f) => f.id !== fileId);
        setSelectedFile(remaining[0] ?? null);
      }
    } catch (err) {
      console.error(err);
      alert("즐겨찾기 해제 실패");
    }
  };

  /* -----------------------------------------------------------
      ⭐ Detail에서 즐겨찾기 등록/해제
      등록 → FavoritePage에서는 리스트에 추가해야 하지만,
      FavoritePage는 '즐겨찾기 목록 페이지'라 등록 로직은 없음
      → 따라서 state만 반영
  ------------------------------------------------------------ */
  const handleFavoriteChange = async (state: boolean) => {
    if (!selectedFile) return;

    const id = selectedFile.id;

    if (!state) {
      // 해제
      await handleUnfavorite(id);
    } else {
      // 이 페이지에서는 등록해도 리스트에 추가 X
      await addFileFavorite(id);

      setSelectedFile((prev) => prev && { ...prev, favorite: true });
      setFiles((prev) =>
        prev.map((f) => (f.id === id ? { ...f, favorite: true } : f))
      );
    }
  };

  /* -----------------------------------------------------------
      삭제 처리
  ------------------------------------------------------------ */
  const handleFileDeleted = (fileId: number) => {
    setFiles((prev) => prev.filter((f) => f.id !== fileId));

    if (selectedFile?.id === fileId) {
      const remain = files.filter((f) => f.id !== fileId);
      setSelectedFile(remain[0] ?? null);
    }
  };

  /* -----------------------------------------------------------
      파일 상세 조회
  ------------------------------------------------------------ */
  const handleSelectFile = async (file: any) => {
    setSelectedFile({
      id: file.id,
      src: file.src ?? "/images/dummy_image_1.png",
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
      const normalizedTags = (detail.tags ?? []).map(
        (t: any) => `#${t.tagName}`
      );

      setSelectedFile({
        id: detail.id,
        src: detail.src ?? file.src,
        tags: normalizedTags,
        context: detail.context ?? "",
        created_at: detail.created_at,
        category_id: detail.category_id,
        favorite: detail.favorite,
        type: detail.type,
        platform: detail.platform,
        ocr_text: detail.ocr_text,
      });
    } catch (e) {
      console.error(e);
    }
  };

  /* -----------------------------------------------------------
      무한 스크롤 로드
  ------------------------------------------------------------ */
  async function loadMore() {
    if (isFetching || !hasMore) return;

    setIsFetching(true);

    try {
      const newFiles = await getFavoriteFiles(page);

      if (newFiles.length === 0) {
        setHasMore(false);
        return;
      }

      const rotated = newFiles.map((f: any) => ({
        ...f,
        rotation: `rotate(${Math.random() * 8 - 4}deg)`,
      }));

      setFiles((prev) => {
        const merged = [...prev, ...rotated];

        // ⭐ 중복 ID 제거 (중요!)
        return merged.filter(
          (v, i, arr) => arr.findIndex((t) => t.id === v.id) === i
        );
      });

      if (page === 0 && rotated.length > 0) {
        handleSelectFile(rotated[0]);
      }

      setPage((p) => p + 1);
    } finally {
      setIsFetching(false);
    }
  }

  useEffect(() => {
    loadMore();
  }, []);

  /* -----------------------------------------------------------
      드래그 앤 드롭
  ------------------------------------------------------------ */
  const handleDragEnd = (event: any) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const oldIndex = files.findIndex((f) => f.id === active.id);
    const newIndex = files.findIndex((f) => f.id === over.id);

    setFiles((prev) => arrayMove(prev, oldIndex, newIndex));
  };

  /* -----------------------------------------------------------
      렌더링
  ------------------------------------------------------------ */
  return (
    <div className="w-full h-full flex justify-center bg-[#FFFEF8] text-[#4C3D25]">
      <div className="w-full max-w-[1200px] h-full flex gap-8 p-6">
        {/* LEFT LIST */}
        <div className="flex flex-col flex-1 overflow-hidden">
          <h1 className="text-5xl font-bold mb-6 pl-4">Favorite</h1>
          <p className="text-xl text-[#7A6A48] mb-4 pl-4">
            즐겨찾기한 파일들을 정리하고 확인해보세요.
          </p>

          <div
            ref={containerRef}
            className="flex-1 overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-[#CBBF9E]/50"
          >
            <DndContext
              sensors={sensors}
              collisionDetection={closestCenter}
              onDragEnd={handleDragEnd}
            >
              <SortableContext items={files} strategy={rectSortingStrategy}>
                <div
                  className="
                    grid gap-6 pt-12 px-10 pb-10
                    grid-cols-1 sm:grid-cols-2 md:grid-cols-3
                    lg:grid-cols-4 xl:grid-cols-5
                    place-items-center
                  "
                >
                  {files.map((file) => (
                    <SortableItem
                      key={file.id}
                      file={file}
                      selectedId={selectedFile?.id ?? null}
                      onSelect={handleSelectFile}
                      onUnfavorite={handleUnfavorite} // ⭐ 여기서 해제
                    />
                  ))}
                </div>
              </SortableContext>
            </DndContext>

            {isFetching && (
              <div className="text-center text-[#7A6A48] py-4 animate-pulse">
                불러오는 중...
              </div>
            )}
            {!hasMore && (
              <div className="text-center text-[#7A6A48] py-4">
                더 이상 데이터가 없습니다.
              </div>
            )}
          </div>
        </div>

        {/* RIGHT DETAIL */}
        {selectedFile && (
          <div className="w-[400px] flex-shrink-0 border-l pl-6 flex flex-col items-center justify-center">
            <PolaroidDetail
              id={selectedFile.id}
              src={selectedFile.src}
              tags={selectedFile.tags}
              contexts={selectedFile.context}
              date={selectedFile.created_at}
              categoryId={selectedFile.category_id}
              favorite={selectedFile.favorite}
              type={selectedFile.type}
              platform={selectedFile.platform}
              ocr_text={selectedFile.ocr_text}
              onFavoriteChange={handleFavoriteChange}
              onFileDeleted={handleFileDeleted}
            />
          </div>
        )}
      </div>
    </div>
  );
}
