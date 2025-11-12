"use client";

import { useParams } from "next/navigation";
import { useState, useEffect, useRef, memo } from "react";
import PolaroidCard from "@/app/home/components/PolaroidCard";
import PolaroidDetail from "./components/PolaroidDetail";
import { Plus } from "lucide-react";

import {
  getCategoryInfo,
  getCategoryTags,
  getCategoryFiles,
  getFileDetail,
} from "@/services/categoryService";

interface SelectedFile {
  id: number;
  src: string;
  tags: string[];
  context: string;
  created_at: string;
  category_id?: number;
  favorite: boolean;
}

interface SortableItemProps {
  file: any;
  selectedId: number | null;
  onSelect: (file: any) => void;
}

const PolaroidItem = memo(
  ({ file, selectedId, onSelect }: SortableItemProps) => {
    return (
      <div
        style={{
          transform: file.rotation,
          transition: "transform 0.2s ease",
          transformOrigin: "center bottom",
          willChange: "transform",
        }}
        className="w-fit overflow-visible"
      >
        <button
          onClick={() => onSelect(file)}
          className={`relative hover:scale-[1.08] transition-transform ${
            selectedId === file.id ? "opacity-90" : "opacity-100"
          }`}
        >
          <PolaroidCard src={file.src || "/images/dummy_image_1.png"} />
          {file.favorite && (
            <span className="absolute top-2 right-2 text-yellow-500 text-lg">
              ★
            </span>
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

  const id = params.id;

  const [categoryName, setCategoryName] = useState("");
  const [tags, setTags] = useState<string[]>([]);
  const [files, setFiles] = useState<any[]>([]);
  const [selectedFile, setSelectedFile] = useState<SelectedFile | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isFetching, setIsFetching] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    async function loadMeta() {
      try {
        const [info, tagList] = await Promise.all([
          getCategoryInfo(id),
          getCategoryTags(id),
        ]);

        setCategoryName(info.categoryName ?? "");
        setTags(tagList.map((t: any) => t.tagName));
      } catch (e) {
        console.error(e);
        alert("카테고리 정보를 불러오는 중 오류가 발생했습니다.");
      }
    }
    loadMeta();
  }, [id]);

  async function loadMoreFiles() {
    if (isFetching || !hasMore) return;
    try {
      setIsFetching(true);
      const newFiles = await getCategoryFiles(id, page);
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
      alert("파일 데이터를 불러오는 중 오류가 발생했습니다.");
    } finally {
      setIsFetching(false);
    }
  }

  useEffect(() => {
    setFiles([]);
    setPage(0);
    setHasMore(true);
    loadMoreFiles();
  }, [id]);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const onScroll = () => {
      if (
        container.scrollTop + container.clientHeight >=
        container.scrollHeight - 300
      ) {
        loadMoreFiles();
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
      });
    } catch (e) {
      console.error(e);
    }
  };

  const handleFavoriteChange = (newState: boolean) => {
    if (!selectedFile) return;
    setSelectedFile((prev) => prev && { ...prev, favorite: newState });
    setFiles((prev) =>
      prev.map((f) =>
        f.id === selectedFile.id ? { ...f, favorite: newState } : f
      )
    );
  };

  return (
    <div className="flex h-full bg-[#FFFEF8] text-[#4C3D25] px-8 py-6 gap-8">
      {/* 좌측 리스트 */}
      <div className="flex flex-col flex-1 overflow-hidden">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-6xl font-bold mb-2">{categoryName}</h1>
            <p className="text-2xl text-[#7A6A48]">
              {tags.length > 0 ? tags.map((t) => `#${t}`).join(" ") : ""}
            </p>
          </div>
          <button className="p-2 rounded-full hover:bg-[#EDE6D8]">
            <Plus className="w-5 h-5" />
          </button>
        </div>

        <div
          ref={containerRef}
          className="flex-1 overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-[#CBBF9E]/50"
        >
          <div className="grid grid-cols-6 gap-6 overflow-visible p-6">
            {files.map((file) => (
              <PolaroidItem
                key={file.id}
                file={file}
                selectedId={selectedFile?.id ?? null}
                onSelect={handleSelectFile}
              />
            ))}
          </div>

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
      <div className="w-2/7 border-l border-[#E3DCC8] pl-6 flex flex-col items-center justify-center">
        <PolaroidDetail
          id={selectedFile?.id}
          src={selectedFile?.src}
          tags={selectedFile?.tags ?? []}
          contexts={selectedFile?.context ?? ""}
          date={selectedFile?.created_at}
          categoryId={selectedFile?.category_id}
          favorite={selectedFile?.favorite}
          onCategoryUpdated={async () => {
            const updated = await getCategoryFiles(id, 0);
            setFiles(
              updated.map((f: any) => ({
                ...f,
                rotation: `rotate(${Math.random() * 8 - 4}deg)`,
              }))
            );
          }}
          onFavoriteChange={handleFavoriteChange}
        />
      </div>
    </div>
  );
}
