"use client";
import { useEffect, useRef } from "react";

interface Props {
  images: {
    id: number;
    src: string;
    type?: string; // image/png | text/plain
    ocr_text?: string;
  }[];
  currentIndex: number;
  onSelect: (i: number) => void;
}

// 텍스트 문서 아이콘 경로 (없으면 Fallback)
const TEXT_ICON = "/images/text_file_icon.png";

export default function ThumbnailStrip({
  images,
  currentIndex,
  onSelect,
}: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const visibleCount = 7;
  const thumbnailSize = 56;

  const handleWheel = (e: React.WheelEvent<HTMLDivElement>) => {
    if (!containerRef.current) return;
    containerRef.current.scrollLeft += e.deltaY;
  };

  // 현재 선택된 인덱스 중앙으로 스크롤 이동
  useEffect(() => {
    if (!containerRef.current) return;

    const container = containerRef.current;
    const scrollPos =
      Math.max(currentIndex - Math.floor(visibleCount / 2), 0) *
      (thumbnailSize + 12);

    container.scrollTo({ left: scrollPos, behavior: "smooth" });
  }, [currentIndex]);

  return (
    <div
      ref={containerRef}
      onWheel={handleWheel}
      className="flex gap-3 overflow-x-auto no-scrollbar pt-6 px-2 pb-2 justify-start mx-auto max-w-[60%]"
    >
      {images.map((item, i) => {
        const isText = item.type?.startsWith("text");

        return (
          <div
            key={item.id}
            className={`w-14 h-14 border border-gray-400 rounded-md overflow-hidden cursor-pointer shrink-0 transition-transform duration-150
              ${
                currentIndex === i
                  ? "ring-2 ring-black scale-105"
                  : "hover:-translate-y-1"
              }
            `}
            onClick={() => onSelect(i)}
          >
            {/* TEXT FILE → 아이콘 대신 TXT 표시 */}
            {isText ? (
              <div className="w-full h-full bg-[#F4F1E8] flex items-center justify-center text-[#4C3D25] font-semibold text-md">
                TXT
              </div>
            ) : (
              /* IMAGE FILE */
              <img
                src={item.src}
                alt=""
                className="w-full h-full object-cover"
              />
            )}
          </div>
        );
      })}
    </div>
  );
}
