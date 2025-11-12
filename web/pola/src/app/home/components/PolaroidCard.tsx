"use client";

import Image from "next/image";
import { useEffect, useState } from "react";

interface PolaroidProps {
  src: string;
  type?: string; // "image/png" | "image/jpeg" | "text/plain"
  ocr_text?: string;
  small?: boolean;
  medium?: boolean;
}

export default function PolaroidCard({
  src,
  type,
  ocr_text,
  small,
  medium,
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

  const sizeClass = small ? "w-16 h-20" : medium ? "w-24 h-32" : "w-36 h-48";

  const displayText = ocr_text || textPreview;

  return (
    <div
      className={`relative flex items-center justify-center bg-white border border-[#8B857C] rounded-md shadow-sm ${sizeClass}`}
    >
      <div
        className="relative w-[85%] h-[70%] rounded-sm border border-[#8B857C] bg-[#FFFEF8] overflow-hidden p-2 flex items-center justify-center"
        style={{ marginBottom: "14%" }}
      >
        {isTextFile ? (
          <div
            className="w-full h-full text-[9px] leading-tight text-[#4C3D25] whitespace-pre-line break-words text-left overflow-hidden"
            style={{
              display: "-webkit-box",
              WebkitLineClamp: 20,
              WebkitBoxOrient: "vertical",
            }}
          >
            {displayText || "텍스트 로딩 중..."}
          </div>
        ) : (
          <Image
            src={src}
            alt="polaroid photo"
            fill
            style={{ objectFit: "cover", objectPosition: "center" }}
          />
        )}
      </div>
    </div>
  );
}
