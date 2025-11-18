"use client";

import Image from "next/image";
import { useState, useMemo, useEffect } from "react";
import ImageModal from "./ImageModal";
import OCRModal from "./OCRModal";
import EditModal from "./EditModal";
import ShareModal from "./ShareModal";
import {
  Download,
  Share2,
  Pencil,
  Star,
  Trash2,
  Globe,
  Smartphone,
  Search,
} from "lucide-react";
import { getMyCategories } from "@/services/categoryService";
import {
  getFileDownloadUrl,
  addFileFavorite,
  removeFileFavorite,
  fileService,
} from "@/services/fileService";
import { useRouter } from "next/navigation";

export interface PolaroidDetailProps {
  id: number;
  src: string;
  type?: string;
  platform?: string;
  ocr_text?: string;
  tags: string[];
  date?: string;
  contexts: string;
  categoryId?: number;
  favorite?: boolean;

  onFavoriteChange?: (state: boolean) => void;
  onCategoryUpdated?: () => Promise<void> | void;
  onFileDeleted?: (id: number) => void;

  sharedView?: boolean;
  downloadUrl?: string;
}

export default function PolaroidDetail({
  id,
  src,
  type,
  platform,
  ocr_text,
  tags,
  date,
  contexts,
  categoryId,
  favorite: initialFavorite = false,
  onFavoriteChange,
  onCategoryUpdated,
  onFileDeleted,
  sharedView,
  downloadUrl,
}: PolaroidDetailProps) {
  const router = useRouter();

  const [open, setOpen] = useState(false);
  const [flipped, setFlipped] = useState(false);

  const [editOpen, setEditOpen] = useState(false);
  const [shareOpen, setShareOpen] = useState(false);

  const [context, setContext] = useState(contexts);
  const [tagState, setTagState] = useState(tags);

  const [categories, setCategories] = useState<any[]>([]);
  const [downloading, setDownloading] = useState(false);

  const [favorite, setFavorite] = useState(initialFavorite);
  const [updatingFavorite, setUpdatingFavorite] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => setTagState(tags), [tags]);
  useEffect(() => setContext(contexts), [contexts]);
  useEffect(() => setFavorite(initialFavorite), [initialFavorite]);

  const displaySrc =
    src && (src.startsWith("/") || src.startsWith("http"))
      ? src
      : "/images/dummy_image_1.png";

  const formattedDate = useMemo(() => {
    if (!date) return "";
    try {
      return new Date(date).toISOString().split("T")[0].replace(/-/g, ".");
    } catch {
      return date;
    }
  }, [date]);

  const platformInfo = useMemo(() => {
    if (!platform) return null;

    const normalized = platform.toUpperCase();

    const map: Record<string, { label: string; Icon: typeof Globe }> = {
      WEB: { label: "WEB", Icon: Globe },
      APP: { label: "APP", Icon: Smartphone },
    };

    return map[normalized] ?? { label: normalized, Icon: Globe };
  }, [platform]);

  const isTextFile =
    type?.includes("text/plain") ||
    src?.endsWith(".txt") ||
    src?.includes("/text/");

  /* ---------------- 편집 모달 ---------------- */
  async function openEdit() {
    try {
      const list = await getMyCategories();
      setCategories(list);
      setEditOpen(true);
    } catch (err) {
      console.error(err);
      alert("카테고리 목록을 불러올 수 없습니다.");
    }
  }

  /* ---------------- 다운로드 ---------------- */
  async function handleDownload() {
    if (sharedView && downloadUrl) {
      const a = document.createElement("a");
      a.href = downloadUrl;
      a.download = "";
      document.body.appendChild(a);
      a.click();
      a.remove();
      return;
    }

    if (!id || downloading) return;

    try {
      setDownloading(true);
      const url = await getFileDownloadUrl(id);
      const a = document.createElement("a");
      a.href = url;
      a.download = "";
      document.body.appendChild(a);
      a.click();
      a.remove();
    } catch (e) {
      console.error(e);
      alert("다운로드 실패");
    } finally {
      setDownloading(false);
    }
  }

  /* ---------------- 즐겨찾기 ---------------- */
  async function handleToggleFavorite(e: any) {
    e.stopPropagation();
    if (updatingFavorite) return;

    const prev = favorite;
    const next = !prev;

    setFavorite(next);
    onFavoriteChange?.(next);

    try {
      setUpdatingFavorite(true);
      if (next) await addFileFavorite(id);
      else await removeFileFavorite(id);
    } catch (e) {
      console.error(e);
      alert("즐겨찾기 변경 실패");
      setFavorite(prev);
      onFavoriteChange?.(prev);
    } finally {
      setUpdatingFavorite(false);
    }
  }

  /* ---------------- 삭제 ---------------- */
  async function handleDelete() {
    if (deleting) return;
    if (!confirm("정말 삭제하시겠습니까?")) return;

    try {
      setDeleting(true);
      await fileService.deleteFile(id);
      alert("파일 삭제 완료");

      // 상위 상태 갱신
      await onCategoryUpdated?.();
      onFileDeleted?.(id);
    } catch (e) {
      console.error(e);
      alert("삭제 실패");
    } finally {
      setDeleting(false);
    }
  }

  /* ---------------- 태그 클릭 검색 ---------------- */
  function handleTagClick(tag: string) {
    const clean = tag.replace(/^#/, "");
    router.push(`/files?tag=${encodeURIComponent(clean)}`);
  }

  /* ---------------- 렌더링 ---------------- */
  if (!src && !ocr_text) {
    return (
      <div className="flex flex-col items-center w-full h-full">
        <div className="w-80 h-full flex items-center justify-center text-center text-[#A89B82] bg-[#FFFEF8] p-6">
          이미지를 선택하세요.
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center w-full">
      <p className="text-md mb-2">사진을 눌러서 뒤집어 보세요</p>

      {/* 카드 */}
      <div
        onClick={() => setFlipped((v) => !v)}
        className={`relative bg-white rounded-md shadow-custom w-[340px] h-[460px] flex items-center justify-center transition-transform duration-500 [transform-style:preserve-3d] ${
          flipped ? "rotate-y-180" : ""
        }`}
      >
        {/* FRONT */}
        <div className="absolute w-full h-full backface-hidden flex flex-col items-center justify-center cursor-pointer">
          <div className="relative w-[85%] h-[78%] overflow-hidden rounded-sm bg-[#FFFEF8]">
            {isTextFile ? (
              <div className="w-full h-full text-base leading-tight text-[#4C3D25] whitespace-pre-line break-words p-2 shadow-inner-custom">
                {ocr_text || "(텍스트 없음)"}
              </div>
            ) : (
              <>
                <Image
                  src={displaySrc}
                  alt="selected"
                  fill
                  className="object-cover object-center"
                />
                <div className="absolute inset-0 shadow-inner-custom z-10 pointer-events-none" />
              </>
            )}
          </div>

          {/* 날짜, 플랫폼, 즐겨찾기 */}
          <div className="w-[85%] h-10 mt-4 flex items-center gap-3 text-[#4C3D25] text-sm">
            <div className="flex items-center gap-2 font-semibold">
              {formattedDate && <span>{formattedDate}</span>}
              {platformInfo && (
                <platformInfo.Icon className="w-5 h-5 text-[#7A6A48]" />
              )}
            </div>

            {!sharedView && (
              <button
                onClick={handleToggleFavorite}
                disabled={updatingFavorite}
                className={`ml-auto flex items-center justify-center bg-white p-2 transition-all hover:scale-105 ${
                  updatingFavorite ? "opacity-60" : ""
                }`}
              >
                <Star
                  fill={favorite ? "#FFD700" : "transparent"}
                  stroke="#FFD700"
                  strokeWidth={2.5}
                  className="w-6 h-6 drop-shadow-sm"
                />
              </button>
            )}
          </div>
        </div>

        {/* BACK */}
        <div className="absolute w-full h-full rotate-y-180 backface-hidden p-4 flex flex-col">
          <div className="flex justify-between items-center mb-3">
            <h2 className="text-lg font-semibold text-[#4C3D25]">Context</h2>

            <div className="flex gap-3">
              {!sharedView && (
                <button onClick={openEdit}>
                  <Pencil className="w-5 h-5 text-[#4C3D25] hover:text-black" />
                </button>
              )}

              <button onClick={handleDownload} disabled={downloading}>
                <Download
                  className={`w-5 h-5 ${
                    downloading
                      ? "text-gray-400 animate-pulse"
                      : "text-[#4C3D25] hover:text-black"
                  }`}
                />
              </button>

              {!sharedView && (
                <>
                  <button onClick={() => setShareOpen(true)}>
                    <Share2 className="w-5 h-5 text-[#4C3D25] hover:text-black" />
                  </button>
                  <button onClick={handleDelete} disabled={deleting}>
                    <Trash2
                      className={`w-5 h-5 ${
                        deleting
                          ? "text-gray-400 animate-pulse"
                          : "text-red-500 hover:text-red-600"
                      }`}
                    />
                  </button>
                </>
              )}
            </div>
          </div>

          <textarea
            className="flex-1 resize-none p-3 rounded-md text-base text-[#4C3D25] focus:outline-none cursor-default bg-transparent"
            value={context}
            readOnly
          />
        </div>
      </div>

      {/* TAG + 확대 버튼 */}
      <div className="mt-4 flex flex-col items-center w-80 max-w-full">
        <div className="flex flex-wrap justify-start gap-2 mb-2 w-full">
          {tagState.map((t, i) => (
            <button
              key={i}
              onClick={() => handleTagClick(t)}
              className="
                bg-[#B0804C]/95
                px-3 py-1
                rounded-full
                font-bold
                text-sm
                whitespace-nowrap
                inline-block
                text-white
                hover:bg-[#99693E]
                transition-colors
              "
            >
              {t.startsWith("#") ? t : `#${t}`}
            </button>
          ))}
        </div>

        <button
          className="mt-3 bg-white border border-[#8B857C] rounded-full p-2 shadow hover:bg-[#F6F1E7] transition-transform hover:scale-110"
          onClick={() => setOpen(true)}
        >
          <Search className="w-5 h-5 text-[#4C3D25]" />
        </button>
      </div>

      {/* MODALS */}
      {open &&
        (isTextFile ? (
          <OCRModal text={ocr_text ?? ""} onClose={() => setOpen(false)} />
        ) : (
          <ImageModal src={displaySrc} onClose={() => setOpen(false)} />
        ))}

      {shareOpen && <ShareModal id={id} onClose={() => setShareOpen(false)} />}

      {editOpen && (
        <EditModal
          fileId={id}
          defaultTags={tagState}
          defaultContext={context}
          defaultCategoryId={categoryId ?? 0}
          categories={categories}
          onClose={() => setEditOpen(false)}
          onSave={async (newTags, newContext, newCategoryId) => {
            // Detail 내부 상태 업데이트
            setTagState(newTags.map((t) => (t.startsWith("#") ? t : `#${t}`)));
            setContext(newContext);

            // 카테고리 이동/태그 변경 등을 상위에 반영
            await onCategoryUpdated?.();

            // 만약 카테고리가 바뀌었으면 현재 리스트에서 제거
            if (categoryId && newCategoryId !== categoryId) {
              onFileDeleted?.(id);
            }

            setEditOpen(false);
          }}
        />
      )}
    </div>
  );
}
