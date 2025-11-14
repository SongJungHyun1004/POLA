"use client";

import { useParams } from "next/navigation";
import { useState, useEffect, useRef, memo } from "react";
import PolaroidCard from "@/app/home/components/PolaroidCard";
import PolaroidDetail from "./components/PolaroidDetail";
import { Pencil } from "lucide-react";

import {
  getCategoryInfo,
  getCategoryFiles,
  getFileDetail,
} from "@/services/categoryService";

// 새 API 서비스
import {
  updateCategoryName,
  fetchCategoryTags,
  addCategoryTags,
  removeCategoryTag,
} from "@/services/categoryService";

import CategoryModal from "@/app/onboarding/components/CategoryModal";

interface SelectedFile {
  id: number;
  src: string;
  tags: string[];
  context: string;
  created_at: string;
  category_id?: number;
  favorite: boolean;
  type?: string;
  ocr_text?: string;
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
          <PolaroidCard
            src={file.src || "/images/dummy_image_1.png"}
            type={file.type}
            ocr_text={file.ocr_text}
          />

          {file.favorite && (
            <span className="absolute top-2 right-2 text-yellow-500 text-lg z-10">
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

  /* Metadata 로딩 */
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

  /* Infinite Files Loading */
  async function loadMoreFiles() {
    if (isFetching || !hasMore) return;

    try {
      setIsFetching(true);
      const newFiles = await getCategoryFiles(id, page);

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
        return merged.filter(
          (v, i, a) => a.findIndex((t) => t.id === v.id) === i
        );
      });

      setPage((prev) => prev + 1);
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
    const box = containerRef.current;
    if (!box) return;

    const onScroll = () => {
      if (box.scrollTop + box.clientHeight >= box.scrollHeight - 300) {
        loadMoreFiles();
      }
    };

    box.addEventListener("scroll", onScroll);
    return () => box.removeEventListener("scroll", onScroll);
  }, [files, hasMore]);

  /* File Detail */
  const handleSelectFile = async (file: any) => {
    setSelectedFile({
      id: file.id,
      src: file.src,
      favorite: file.favorite,
      tags: [],
      context: "",
      created_at: "",
      type: file.type,
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
        favorite: detail.favorite,
        type: detail.type,
        ocr_text: detail.ocr_text,
      });
    } catch {}
  };

  const handleFavoriteChange = (state: boolean) => {
    if (!selectedFile) return;

    setSelectedFile((prev) => prev && { ...prev, favorite: state });
    setFiles((prev) =>
      prev.map((f) =>
        f.id === selectedFile.id ? { ...f, favorite: state } : f
      )
    );
  };

  /* --- ⭐ 모달에서 저장 눌렀을 때 API 로직 ⭐ --- */
  const handleSaveModal = async (newName: string, newTags: string[]) => {
    try {
      // 1) 이름 변경
      if (newName !== categoryName) {
        await updateCategoryName(id, newName);
        setCategoryName(newName);
      }

      // 2) 최신 태그 목록 조회 (id + tagId 필요)
      const currentTagList = await fetchCategoryTags(id); // [{id, tagName}]
      const currentTagNames = currentTagList.map((t: any) => t.tagName);

      // 3) 추가된 태그 찾기
      const tagsToAdd = newTags.filter((t) => !currentTagNames.includes(t));
      if (tagsToAdd.length > 0) {
        await addCategoryTags(id, tagsToAdd);
      }

      // 4) 삭제된 태그 찾기
      const tagsToRemove = currentTagList.filter(
        (t: any) => !newTags.includes(t.tagName)
      );

      for (const removed of tagsToRemove) {
        await removeCategoryTag(id, removed.id);
      }

      // 화면 반영
      setTags(newTags);

      alert("카테고리가 성공적으로 수정되었습니다!");
    } catch (e) {
      console.error(e);
      alert("카테고리 수정 중 오류가 발생했습니다.");
    }
  };

  return (
    <>
      <div className="flex h-full bg-[#FFFEF8] text-[#4C3D25] px-8 py-6 gap-8">
        <div className="flex flex-col flex-1 overflow-hidden">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-6xl font-bold mb-2">{categoryName}</h1>
              <p className="text-2xl text-[#7A6A48]">
                {tags.map((t) => `#${t}`).join(" ")}
              </p>
            </div>

            {/* 모달 오픈 */}
            <button
              className="p-2 rounded-full hover:bg-[#EDE6D8]"
              onClick={() => setIsModalOpen(true)}
            >
              <Pencil className="w-5 h-5" />
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
            type={selectedFile?.type}
            ocr_text={selectedFile?.ocr_text}
            onFavoriteChange={handleFavoriteChange}
          />
        </div>
      </div>

      <CategoryModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveModal}
        onDelete={() => {}}
        defaultName={categoryName}
        defaultTags={tags}
        isEditing={true}
        showDeleteButton={false}
      />
    </>
  );
}
