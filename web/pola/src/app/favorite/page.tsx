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

import PolaroidCard from "@/app/home/components/PolaroidCard";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";
import { getFavoriteFiles } from "@/services/favoriteService";
import { getFileDetail } from "@/services/categoryService";

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

const SortableItem = memo(
  ({ file, selectedId, onSelect }: SortableItemProps) => {
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
        style={style}
        className="flex justify-center w-full overflow-visible"
      >
        <button
          {...attributes}
          {...listeners}
          onClick={() => onSelect(file)}
          className={`
            relative transition-transform
            ${isSelected ? "scale-110 z-20" : "hover:scale-[1.07]"}
          `}
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
              fill={file.favorite ? "#FFD700" : "transparent"}
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
SortableItem.displayName = "SortableItem";

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

  async function loadMore() {
    if (isFetching || !hasMore) return;
    try {
      setIsFetching(true);
      const newFiles = await getFavoriteFiles(page);
      if (newFiles.length === 0) {
        setHasMore(false);
        return;
      }

      const newFilesWithRotation = newFiles.map((f: any) => ({
        ...f,
        rotation: `rotate(${Math.random() * 8 - 4}deg)`,
      }));

      setFiles((prev) => {
        const merged = [...prev, ...newFilesWithRotation];
        return merged.filter(
          (v, i, a) => a.findIndex((t) => t.id === v.id) === i
        );
      });

      setPage((prev) => prev + 1);
    } catch (e) {
      console.error(e);
    } finally {
      setIsFetching(false);
    }
  }

  useEffect(() => {
    loadMore();
  }, []);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const onScroll = () => {
      if (
        container.scrollTop + container.clientHeight >=
        container.scrollHeight - 300
      ) {
        loadMore();
      }
    };
    container.addEventListener("scroll", onScroll);
    return () => container.removeEventListener("scroll", onScroll);
  }, [files, hasMore, isFetching]);

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
        src: detail.src ?? file.src ?? "/images/dummy_image_1.png",
        tags: normalizedTags,
        context: detail.context ?? "",
        created_at: detail.created_at,
        category_id: detail.category_id,
        favorite: detail.favorite,
        type: detail.type,
        platform: detail.platform ?? file.platform,
        ocr_text: detail.ocr_text,
      });
    } catch (e) {
      console.error(e);
    }
  };

  const handleDragEnd = (event: any) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const oldIndex = files.findIndex((f) => f.id === active.id);
    const newIndex = files.findIndex((f) => f.id === over.id);
    setFiles((prev) => arrayMove(prev, oldIndex, newIndex));
  };

  return (
    <div className="w-full h-full flex justify-center bg-[#FFFEF8] text-[#4C3D25]">
      <div className="w-full max-w-[1200px] h-full flex gap-8 px-6 py-6">
        {/* 좌측 리스트 */}
        <div className="flex flex-col flex-1 overflow-hidden">
          <div className="flex items-center justify-between mb-2 pl-4">
            <h1 className="text-5xl font-bold mb-6 mt-8">Favorite</h1>
          </div>
          <div className="flex items-center justify-between mb-2 pl-4">
            즐겨찾기 해둔 파일들을 둘러보세요.
          </div>

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
                    grid-cols-1
                    sm:grid-cols-2
                    md:grid-cols-3
                    lg:grid-cols-4
                    xl:grid-cols-5
                    place-items-center
                    overflow-x-hidden
                  "
                >
                  {files.map((file) => (
                    <div
                      key={file.id}
                      className="w-full flex justify-center"
                      style={{
                        transform: file.rotation,
                        transition: "transform 0.2s ease",
                        transformOrigin: "center bottom",
                      }}
                    >
                      <SortableItem
                        file={file}
                        selectedId={selectedFile?.id ?? null}
                        onSelect={handleSelectFile}
                      />
                    </div>
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

        {/* 우측 상세 */}
        <div className="w-[400px] flex-shrink-0 border-l border-[#E3DCC8] pl-6 flex flex-col items-center justify-center">
          <PolaroidDetail
            id={selectedFile?.id}
            src={selectedFile?.src}
            tags={selectedFile?.tags ?? []}
            contexts={selectedFile?.context ?? ""}
            date={selectedFile?.created_at}
            categoryId={selectedFile?.category_id}
            favorite={selectedFile?.favorite}
            type={selectedFile?.type}
            platform={selectedFile?.platform}
            ocr_text={selectedFile?.ocr_text}
            onCategoryUpdated={async () => {
              const refreshed = await getFavoriteFiles(0);
              setFiles(
                refreshed.map((f: any) => ({
                  ...f,
                  rotation: `rotate(${Math.random() * 8 - 4}deg)`,
                }))
              );
            }}
            onFavoriteChange={(newState) => {
              if (!selectedFile) return;
              setSelectedFile(
                (prev) => prev && { ...prev, favorite: newState }
              );
              setFiles((prev) =>
                prev.map((f) =>
                  f.id === selectedFile.id ? { ...f, favorite: newState } : f
                )
              );
            }}
          />
        </div>
      </div>
    </div>
  );
}
