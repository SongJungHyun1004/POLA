"use client";

import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import PolaroidPreview from "./components/PolaroidPreview";
import ThumbnailStrip from "./components/ThumbnailStrip";
import { ChevronLeft, ChevronRight } from "lucide-react";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";

export default function RemindPage() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [direction, setDirection] = useState<1 | -1>(1);

  const images = Array.from({ length: 30 }, (_, i) => ({
    id: i + 1,
    src: "/images/dummy_image_1.png",
    tags: ["#태그1", "#태그2", "#태그3", "#태그4", "#태그5", "#태그6"],
    contexts: "내용을 입력하세요...",
    favorite: i % 4 === 0,
    date: "2025.10.30",
  }));

  const paginate = (dir: 1 | -1) => {
    setDirection(dir);
    setCurrentIndex((prev) => (prev + dir + images.length) % images.length);
  };

  const leftIndex = (currentIndex - 1 + images.length) % images.length;
  const rightIndex = (currentIndex + 1) % images.length;
  const current = images[currentIndex];

  const variants = {
    enter: (direction: number) => ({
      x: direction > 0 ? 250 : -250,
      opacity: 0,
      scale: 0.8,
    }),
    center: { x: 0, opacity: 1, scale: 1 },
    exit: (direction: number) => ({
      x: direction > 0 ? -250 : 250,
      opacity: 0,
      scale: 0.8,
    }),
  };

  // ▶️ 방향키 입력 처리
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // 입력창 포커스 시 슬라이딩 방지 (선택)
      const active = document.activeElement;
      if (
        active instanceof HTMLInputElement ||
        active instanceof HTMLTextAreaElement ||
        active instanceof HTMLSelectElement
      ) {
        return;
      }

      if (e.key === "ArrowLeft") paginate(-1);
      if (e.key === "ArrowRight") paginate(1);
      if (e.key === " ") {
        e.preventDefault();
        paginate(1);
      }
      if (document.activeElement instanceof HTMLInputElement) return;
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  return (
    <div className="relative w-full h-full text-[#4C3D25] overflow-hidden flex flex-col">
      {/* Title Row */}
      <h1 className="text-6xl font-bold ml-8 mt-6 mb-4 flex-none">Remind</h1>

      {/* Slider fixed region under title */}
      <div className="relative flex-none h-[460px] flex items-center justify-center">
        {/* Left Preview */}
        <div
          onClick={() => paginate(-1)}
          className="absolute left-[20%] top-1/2 -translate-y-1/2 scale-[0.88] opacity-60 cursor-pointer z-10 hover:opacity-80"
        >
          <PolaroidPreview data={images[leftIndex]} />
        </div>

        {/* Main Card */}
        <AnimatePresence custom={direction} mode="popLayout">
          <motion.div
            key={current.id}
            custom={direction}
            variants={variants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.35, ease: "easeInOut" }}
            className="relative z-20"
          >
            <PolaroidDetail {...current} />
          </motion.div>
        </AnimatePresence>

        {/* Right Preview */}
        <div
          onClick={() => paginate(1)}
          className="absolute right-[20%] top-1/2 -translate-y-1/2 scale-[0.88] opacity-60 cursor-pointer z-10 hover:opacity-80"
        >
          <PolaroidPreview data={images[rightIndex]} />
        </div>

        {/* Arrows */}
        <button
          className="absolute left-[12%] top-1/2 -translate-y-1/2 hover:opacity-80 z-20"
          onClick={() => paginate(-1)}
        >
          <ChevronLeft size={42} />
        </button>
        <button
          className="absolute right-[12%] top-1/2 -translate-y-1/2 hover:opacity-80 z-20"
          onClick={() => paginate(1)}
        >
          <ChevronRight size={42} />
        </button>
      </div>

      {/* Thumbnails pinned to bottom */}
      <div className="absolute bottom-6 left-1/2 -translate-x-1/2 w-[90%] pointer-events-auto">
        <ThumbnailStrip
          images={images}
          currentIndex={currentIndex}
          onSelect={(i) => {
            setDirection(i > currentIndex ? 1 : -1);
            setCurrentIndex(i);
          }}
        />
      </div>
    </div>
  );
}
