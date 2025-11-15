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
      <div
        className="
          flex justify-center
          w-full
          overflow-visible
        "
      >
        <button
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
        favorite: detail.favorite,
        type: detail.type,
        platform: detail.platform ?? file.platform,
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
      {/* 전체 화면 중앙 정렬 + 최대 너비 1300px */}
      <div className="w-full h-full flex justify-center bg-[#FFFEF8] text-[#4C3D25]">
        <div className="w-full max-w-[1200px] h-full flex gap-8 px-6 pb-6">
          {/* ---------------- LEFT CONTENT AREA ---------------- */}
          <div className="flex-1 flex flex-col overflow-hidden">
            {/* 상단 타이틀 */}
            <div className="flex items-center justify-between mb-2 pl-4">
              <div>
                <h1 className="text-4xl font-bold mb-2">{categoryName}</h1>
                <p className="text-xl text-[#7A6A48]">
                  {tags.map((t) => `#${t}`).join(" ")}
                </p>
              </div>

              {/* 모달 버튼 */}
              <button
                className="p-2 rounded-full hover:bg-[#EDE6D8]"
                onClick={() => setIsModalOpen(true)}
              >
                <Pencil className="w-5 h-5" />
              </button>
            </div>

            {/* 리스트 스크롤 영역 */}
            <div
              ref={containerRef}
              className="flex-1 overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-[#CBBF9E]/50"
            >
              {/* 비어있는 경우 */}
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
                    grid gap-6 p-10
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
                    <PolaroidItem
                      key={file.id}
                      file={file}
                      selectedId={selectedFile?.id ?? null}
                      onSelect={handleSelectFile}
                    />
                  ))}
                </div>
              )}

              {/* 로딩 */}
              {isFetching && (
                <div className="text-center text-[#7A6A48] py-4 animate-pulse">
                  불러오는 중...
                </div>
              )}

              {/* 더 이상 없음 */}
              {!isFetching && !hasMore && files.length > 0 && (
                <div className="text-center text-[#7A6A48] py-4">
                  더 이상 데이터가 없습니다.
                </div>
              )}
            </div>
          </div>

          {/* ---------------- RIGHT DETAIL PANEL ---------------- */}
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
          "
          >
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
              onFavoriteChange={handleFavoriteChange}
            />
          </div>
        </div>
      </div>

      {/* EDIT MODAL */}
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
