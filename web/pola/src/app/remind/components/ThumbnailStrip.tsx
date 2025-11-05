"use client";
import { useEffect, useRef } from "react";

interface Props {
  images: {
    id: number;
    src: string;
  }[];
  currentIndex: number;
  onSelect: (i: number) => void;
}

export default function ThumbnailStrip({
  images,
  currentIndex,
  onSelect,
}: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const visibleCount = 7;
  const thumbnailSize = 56;

  // Wheel → horizontal scroll
  const handleWheel = (e: React.WheelEvent<HTMLDivElement>) => {
    if (!containerRef.current) return;

    // vertical scroll → horizontal scroll
    containerRef.current.scrollLeft += e.deltaY;
  };

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
      className="flex gap-3 overflow-x-auto no-scrollbar pt-6 px-2 pb-2 justify-start mx-auto max-w-3/5"
    >
      {images.map((item, i) => (
        <div
          key={item.id}
          className={`w-14 h-14 border border-gray-400 rounded-md overflow-hidden cursor-pointer shrink-0 ${
            currentIndex === i ? "ring-2 ring-black" : ""
          }`}
          onClick={() => onSelect(i)}
        >
          <img src={item.src} className="w-full h-full object-cover" />
        </div>
      ))}
    </div>
  );
}
