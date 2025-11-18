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
import {
  getMyCategories,
  updateFileCategory,
} from "@/services/categoryService";
import {
  getFileDownloadUrl,
  addFileFavorite,
  removeFileFavorite,
  fileService,
} from "@/services/fileService";
import { useRouter } from "next/navigation";

export interface PolaroidDetailProps {
  id?: number;
  src?: string;
  type?: string;
  platform?: string;
  ocr_text?: string;
  tags: string[];
  date?: string;
  contexts: string;
  categoryId?: number;
  onCategoryUpdated?: () => void;
  sharedView?: boolean;
  downloadUrl?: string;
  favorite?: boolean;
  onFavoriteChange?: (newState: boolean) => void;
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
  onCategoryUpdated,
  sharedView,
  downloadUrl,
  favorite: initialFavorite = false,
  onFavoriteChange,
}: PolaroidDetailProps) {
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

  const router = useRouter();

  const isTextFile =
    type?.includes("text/plain") ||
    (src?.endsWith(".txt") ?? false) ||
    src?.includes("/text/");

  useEffect(() => setTagState(tags), [tags]);
  useEffect(() => setContext(contexts), [contexts]);
  useEffect(() => {
    setFavorite(initialFavorite);
  }, [initialFavorite]);

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

  if (!src && !ocr_text) {
    return (
      <div className="flex flex-col items-center w-full h-full">
        <div className="w-80 h-full flex items-center justify-center text-center text-[#A89B82] bg-[#FFFEF8] p-6">
          이미지를 선택하세요.
        </div>
      </div>
    );
  }

  async function openEdit() {
    try {
      const list = await getMyCategories();
      setCategories(list);
      setEditOpen(true);
    } catch (e) {
      console.error(e);
      alert("카테고리 목록을 불러오지 못했습니다.");
    }
  }

  async function handleSave(
    tags: string[],
    context: string,
    newCategoryId: number
  ) {
    if (!id) return;
    setTagState(tags);
    setContext(context);

    try {
      await updateFileCategory(id, newCategoryId);
      onCategoryUpdated?.();
    } catch {
      alert("카테고리 변경 실패");
    }
  }

  async function handleDownload() {
    if (sharedView && downloadUrl) {
      const a = document.createElement("a");
      a.href = downloadUrl;
      a.download = "";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
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
      document.body.removeChild(a);
    } catch (e) {
      console.error("다운로드 실패:", e);
      alert("다운로드에 실패했습니다.");
    } finally {
      setDownloading(false);
    }
  }

  async function handleToggleFavorite(e: React.MouseEvent) {
    e.stopPropagation();
    if (!id || updatingFavorite) return;

    setUpdatingFavorite(true);
    const prev = favorite;
    const next = !prev;
    setFavorite(next);
    onFavoriteChange?.(next);

    try {
      if (next) await addFileFavorite(id);
      else await removeFileFavorite(id);
    } catch (err) {
      console.error("즐겨찾기 변경 실패:", err);
      alert("즐겨찾기 변경 중 오류가 발생했습니다.");
      setFavorite(prev);
      onFavoriteChange?.(prev);
    } finally {
      setUpdatingFavorite(false);
    }
  }

  async function handleDelete() {
    if (!id || deleting) return;
    if (!confirm("정말 이 파일을 삭제하시겠습니까?")) return;

    try {
      setDeleting(true);
      await fileService.deleteFile(id);
      alert("파일이 성공적으로 삭제되었습니다.");
      onCategoryUpdated?.();
    } catch (err: any) {
      console.error("파일 삭제 실패:", err);
      alert(err.message || "파일 삭제 중 오류가 발생했습니다.");
    } finally {
      setDeleting(false);
    }
  }

  const handleTagClick = (tag: string) => {
    const cleanTag = tag.startsWith("#") ? tag.substring(1) : tag;
    router.push(`/files?tag=${encodeURIComponent(cleanTag)}`);
  };

  return (
    <div className="flex flex-col items-center w-full">
      <p className="text-md mb-2">사진을 눌러서 뒤집어 보세요</p>
      {/* ---------- 카드 ---------- */}
      <div
        className={`relative bg-white rounded-md shadow-custom w-[340px] h-[460px] flex items-center justify-center transition-transform duration-500 [transform-style:preserve-3d] ${
          flipped ? "rotate-y-180" : ""
        }`}
        onClick={() => setFlipped((prev) => !prev)}
      >
        {/* ---------- FRONT ---------- */}
        <div className="absolute w-full h-full backface-hidden flex flex-col items-center justify-center cursor-pointer">
          <div className="relative w-[85%] h-[78%] overflow-hidden rounded-sm bg-[#FFFEF8]">
            {isTextFile ? (
              <div
                className="w-full h-full overflow-y-auto text-base leading-tight text-[#4C3D25] whitespace-pre-line break-words scrollbar-none p-2 shadow-inner-custom"
                onWheel={(e) => e.stopPropagation()}
              >
                {ocr_text || "(텍스트 없음)"}
              </div>
            ) : (
              <>
                <Image
                  src={displaySrc}
                  alt="selected polaroid"
                  fill
                  className="object-cover object-center z-0"
                />
                <div className="absolute inset-0 shadow-inner-custom z-10 pointer-events-none"></div>
              </>
            )}
          </div>

          {(formattedDate || platformInfo || !sharedView) && (
            <div className="w-[85%] h-10 mt-4 flex items-center gap-3 text-[#4C3D25] text-sm">
              <div className="flex items-center gap-2 font-semibold tracking-tight">
                {formattedDate && (
                  <span className="text-sm">{formattedDate}</span>
                )}
                {platformInfo && (
                  <span className="inline-flex items-center gap-1 text-[#4C3D25] text-sm">
                    <platformInfo.Icon className="w-5 h-5 text-[#7A6A48]" />
                  </span>
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
                    className="drop-shadow-sm w-6 h-6"
                  />
                </button>
              )}
            </div>
          )}
        </div>

        {/* ---------- BACK ---------- */}
        <div className="absolute w-full h-full rotate-y-180 backface-hidden p-4 flex flex-col">
          <div className="flex justify-between items-center mb-3">
            <h2 className="text-lg font-semibold text-[#4C3D25]">Context</h2>
            <div className="flex gap-3">
              <button
                onClick={openEdit}
                className={`${sharedView && "hidden"}`}
              >
                <Pencil className="w-5 h-5 text-[#4C3D25] hover:text-black" />
              </button>
              <button
                onClick={handleDownload}
                disabled={downloading}
                title="다운로드"
              >
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
                  <button onClick={() => id && setShareOpen(true)}>
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
            className="flex-1 resize-none p-3 rounded-md text-base text-[#4C3D25] focus:outline-none cursor-default"
            value={context}
            readOnly
          />
        </div>
      </div>

      {/* ---------- TAG + 확대 버튼 ---------- */}
      <div className="mt-4 text-center text-white flex flex-col items-center w-80 max-w-full">
        <div className="flex flex-wrap justify-start gap-2 mb-2 w-full">
          {tagState.map((t, idx) => (
            <button
              key={idx}
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

      {/* 모달 */}
      {open &&
        (isTextFile ? (
          <OCRModal text={ocr_text ?? ""} onClose={() => setOpen(false)} />
        ) : (
          <ImageModal src={displaySrc} onClose={() => setOpen(false)} />
        ))}

      {shareOpen && id && (
        <ShareModal id={id} onClose={() => setShareOpen(false)} />
      )}
      {editOpen && id && (
        <EditModal
          fileId={id}
          defaultTags={tagState}
          defaultContext={context}
          defaultCategoryId={categoryId ?? 0}
          categories={categories}
          onClose={() => setEditOpen(false)}
          onSave={onCategoryUpdated ?? (() => {})}
        />
      )}
    </div>
  );
}
