"use client";

import Image from "next/image";
import { useEffect, useState } from "react";

interface PolaroidProps {
  src: string;
  type?: string; // "image/png" | "image/jpeg" | "text/plain"
  ocr_text?: string;
  small?: boolean;
  medium?: boolean;
  large?: boolean;
  categoryName?: string; // Added categoryName prop
}

export default function PolaroidCard({
  src,
  type,
  ocr_text,
  small,
  medium,
  large,
  categoryName, // Destructure categoryName
}: PolaroidProps) {
  const [textPreview, setTextPreview] = useState<string>("");

  const isTextFile =
    type?.startsWith("text/") || src.endsWith(".txt") || src.includes("/text/");

  useEffect(() => {
    if (!isTextFile || ocr_text) return; // ✅ OCR이 있을 경우 fetch 불필요

    async function loadText() {
      try {
        const res = await fetch(src, { mode: "cors" });
        const blob = await res.blob();
        const text = await blob.text();

        setTextPreview(text);
      } catch (err) {
        console.error("텍스트 미리보기 로딩 실패:", err);
        setTextPreview("(텍스트 로딩 실패)");
      }
    }

    loadText();
  }, [src, isTextFile, ocr_text]);

  const sizeClass = small
    ? "w-16 h-20"
    : medium
    ? "w-24 h-32"
    : large
    ? "w-72 h-96"
    : "w-36 h-48";

  const displayText = ocr_text || textPreview;

  return (
    <div
      className={`relative flex flex-col items-center justify-center bg-white rounded-md shadow-custom ${sizeClass}`}
    >
      <div
        className="relative w-[85%] h-[70%] rounded-sm bg-[#FFFEF8] overflow-hidden"
        style={{ marginBottom: "14%" }}
      >
        {isTextFile ? (
          <div
            className="w-full h-full p-2 text-[13px] leading-tight text-[#4C3D25] whitespace-pre-line break-words text-left overflow-hidden shadow-inner-custom"
            style={{
              display: "-webkit-box",
              WebkitLineClamp: 20,
              WebkitBoxOrient: "vertical",
            }}
          >
            {displayText || "텍스트 로딩 중..."}
          </div>
        ) : (
          <>
            <Image
              src={src}
              alt="polaroid photo"
              fill
              style={{ objectFit: "cover", objectPosition: "center" }}
            />
            <div className="absolute inset-0 shadow-inner-custom z-10 pointer-events-none"></div>
          </>
        )}
      </div>
      
      {categoryName && (
        <p className="absolute bottom-6 text-2xl font-bold text-[#4C3D25] text-center line-clamp-1">
          {categoryName}
        </p>
      )}
    </div>
  );
}