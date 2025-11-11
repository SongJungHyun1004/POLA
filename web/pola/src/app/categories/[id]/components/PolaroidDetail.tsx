"use client";

import Image from "next/image";
import { useState, useMemo, useEffect } from "react";
import ImageModal from "./ImageModal";
import EditModal from "./EditModal";
import ShareModal from "./ShareModal";
import { RotateCcw, Download, Share2, Pencil, Star } from "lucide-react";
import {
  getMyCategories,
  updateFileCategory,
} from "@/services/categoryService";
import {
  getFileDownloadUrl,
  addFileFavorite,
  removeFileFavorite,
} from "@/services/fileService";

interface PolaroidDetailProps {
  id?: number;
  src?: string;
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

  if (!src) {
    return (
      <div className="flex flex-1 items-center justify-center text-[#A89B82]">
        이미지를 선택하세요.
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

  return (
    <div className="flex flex-col items-center w-full">
      {/* 카드 */}
      <div
        className={`relative bg-white border border-[#8B857C] rounded-md shadow-sm w-80 h-[420px] flex items-center justify-center transition-transform duration-500 [transform-style:preserve-3d] ${
          flipped ? "rotate-y-180" : ""
        }`}
      >
        {/* Front */}
        <div
          className="absolute w-full h-full backface-hidden flex flex-col items-center justify-center cursor-pointer"
          onClick={() => setOpen(true)}
        >
          <div
            className="relative w-[85%] h-[70%] overflow-hidden rounded-sm border border-[#8B857C] bg-[#FFFEF8]"
            style={{ marginBottom: "14%" }}
          >
            {!sharedView && (
              <button
                onClick={handleToggleFavorite}
                disabled={updatingFavorite}
                className="absolute top-2 right-2 z-10 hover:scale-110 transition-transform"
              >
                <Star
                  size={22}
                  fill={favorite ? "#FFD700" : "transparent"}
                  stroke="#FFD700"
                  strokeWidth={1.8}
                  className={`drop-shadow-sm transition-colors ${
                    updatingFavorite ? "opacity-60" : "opacity-100"
                  }`}
                />
              </button>
            )}

            <Image
              src={displaySrc}
              alt="selected polaroid"
              fill
              className="object-cover object-center"
            />
          </div>
        </div>

        {/* Back */}
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
                <button onClick={() => id && setShareOpen(true)}>
                  <Share2 className="w-5 h-5 text-[#4C3D25] hover:text-black" />
                </button>
              )}
            </div>
          </div>

          <textarea
            className="flex-1 resize-none p-3 rounded-md text-sm text-[#4C3D25] focus:outline-none cursor-default"
            value={context}
            readOnly
          />
        </div>
      </div>

      {/* Tag + Date */}
      <div className="mt-4 text-center text-[#4C3D25] flex flex-col items-center">
        <p className="text-lg">{tagState.join(" ")}</p>
        <p className="text-2xl font-semibold mt-1">{formattedDate}</p>

        <button
          className="mt-3 bg-white border border-[#8B857C] rounded-full p-2 shadow hover:bg-[#F6F1E7] transition-transform hover:rotate-180"
          onClick={() => setFlipped((prev) => !prev)}
        >
          <RotateCcw className="w-5 h-5 text-[#4C3D25]" />
        </button>
        <p className="text-md mt-2">버튼을 눌러서 사진을 뒤집어 보세요</p>
      </div>

      {open && <ImageModal src={displaySrc} onClose={() => setOpen(false)} />}

      {shareOpen && id && (
        <ShareModal id={id} onClose={() => setShareOpen(false)} />
      )}

      {editOpen && (
        <EditModal
          defaultTags={tagState}
          defaultContext={context}
          defaultCategoryId={categoryId ?? 0}
          categories={categories}
          onClose={() => setEditOpen(false)}
          onSave={handleSave}
        />
      )}
    </div>
  );
}
