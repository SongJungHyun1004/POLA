"use client";

import Image from "next/image";

export interface SelectedPreviewProps {
  src?: string;
  type?: string;
  ocr_text?: string;
  date?: string;
  favorite?: boolean;
  onClick?: () => void;
}

export default function SelectedPreview({
  src,
  type,
  ocr_text,
  date,
  onClick,
}: SelectedPreviewProps) {
  const isTextFile =
    type?.includes("text/plain") ||
    (src?.endsWith(".txt") ?? false) ||
    src?.includes("/text/");

  const displaySrc =
    src && (src.startsWith("/") || src.startsWith("http"))
      ? src
      : "/images/dummy_image_1.png";

  return (
    <div
      className="
        bg-white rounded-md shadow-custom
        w-[300px] h-[400px]
        flex flex-col items-center
        pt-4          /* 상단 여백 (좌우와 유사하게 유지) */
        pb-6          /* 하단 여백 크게 */
        cursor-pointer
        transition-transform duration-300 hover:scale-105
      "
      onClick={onClick}
    >
      {/* 이미지 영역 */}
      <div className="relative w-[90%] h-[85%] overflow-hidden rounded-sm bg-[#FFFEF8]">
        {isTextFile ? (
          <div
            className="
      w-full h-full 
      p-2 
      text-[#4C3D25] 
      text-base 
      whitespace-pre-line 
      overflow-hidden      /* 스크롤 제거 */
      shadow-inner-custom
    "
          >
            {ocr_text || "(텍스트 없음)"}
          </div>
        ) : (
          <>
            <Image
              src={displaySrc}
              alt="preview"
              fill
              className="object-cover z-0"
            />
            <div className="absolute inset-0 shadow-inner-custom z-10 pointer-events-none" />
          </>
        )}
      </div>

      {/* 날짜 및 아래 여백 */}
      {date && (
        <span className="mt-3 text-sm font-semibold text-[#4C3D25]">
          {new Date(date).toISOString().split("T")[0].replace(/-/g, ".")}
        </span>
      )}
    </div>
  );
}
