"use client";

import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import PolaroidPreview from "./components/PolaroidPreview";
import ThumbnailStrip from "./components/ThumbnailStrip";
import { ChevronLeft, ChevronRight } from "lucide-react";
import PolaroidDetail from "../categories/[id]/components/PolaroidDetail";
import { getRemindFiles, getFileDetail } from "@/services/fileService";

export default function RemindPage() {
  const [reminds, setReminds] = useState<any[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [direction, setDirection] = useState<1 | -1>(1);
  const [currentDetail, setCurrentDetail] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);

  // 1) 초기 데이터 로딩
  useEffect(() => {
    async function load() {
      try {
        const list = await getRemindFiles(); // [{ id, src, type, ocr_text, favorite, tags, createdAt }]
        setReminds(list);

        if (list.length > 0) {
          const first = list[0];
          const detail = await getFileDetail(first.id);

          const tags = (detail.tags ?? []).map((t: any) => `#${t.tagName}`);

          setCurrentDetail({
            id: detail.id,
            src: detail.src ?? first.src,
            type: detail.type ?? first.type,
            platform: detail.platform ?? first.platform,
            ocr_text: detail.ocr_text ?? first.ocr_text,
            favorite: detail.favorite,
            tags,
            contexts: detail.context ?? "",
            date: detail.createdAt,
          });
        }
      } catch (e) {
        console.error("Remind load error:", e);
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  // 2) 특정 인덱스 상세 정보 업데이트
  const updateDetail = async (index: number) => {
    const target = reminds[index];
    if (!target) return;

    try {
      const detail = await getFileDetail(target.id);
      const tags = (detail.tags ?? []).map((t: any) => `#${t.tagName}`);

      setCurrentDetail({
        id: detail.id,
        src: detail.src ?? target.src,
        type: detail.type ?? target.type,
        platform: detail.platform ?? target.platform,
        ocr_text: detail.ocr_text ?? target.ocr_text,
        favorite: detail.favorite,
        tags,
        contexts: detail.context ?? "",
        date: detail.created_at,
      });
    } catch (e) {
      console.error("Detail update error:", e);
    }
  };

  // 3) 페이지 이동
  const paginate = (dir: 1 | -1) => {
    if (reminds.length === 0) return;
    setDirection(dir);
    const nextIndex = (currentIndex + dir + reminds.length) % reminds.length;
    setCurrentIndex(nextIndex);
    updateDetail(nextIndex);
  };

  // 4) 방향키 처리
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      const a = document.activeElement;
      if (
        a instanceof HTMLInputElement ||
        a instanceof HTMLTextAreaElement ||
        a instanceof HTMLSelectElement
      ) {
        return;
      }

      if (e.key === "ArrowLeft") paginate(-1);
      if (e.key === "ArrowRight") paginate(1);

      // Space → 다음 이미지
      if (e.key === " ") {
        e.preventDefault();
        paginate(1);
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [reminds, currentIndex]);

  // 로딩 중 화면
  if (loading) {
    return (
      <div className="flex items-center justify-center h-full text-2xl text-[#4C3D25]">
        Loading...
      </div>
    );
  }

  if (reminds.length === 0) {
    return (
      <div className="flex items-center justify-center h-full text-xl text-[#4C3D25]">
        리마인드할 이미지가 없습니다.
      </div>
    );
  }

  // Prev / Next
  const leftIndex = (currentIndex - 1 + reminds.length) % reminds.length;
  const rightIndex = (currentIndex + 1) % reminds.length;

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

  return (
    <div className="relative w-full h-full text-[#4C3D25] overflow-hidden flex flex-col">
      {/* Title */}
      <h1 className="text-6xl font-bold ml-8 mt-6 mb-4 flex-none">Remind</h1>

      {/* Slider */}
      <div className="relative flex-none h-[460px] flex items-center justify-center">
        {/* Left Preview */}
        <div
          onClick={() => paginate(-1)}
          className="absolute left-[20%] top-1/2 -translate-y-1/2 scale-[0.88] opacity-60 cursor-pointer z-10 hover:opacity-80"
        >
          <PolaroidPreview data={reminds[leftIndex]} />
        </div>

        {/* Center - Detail */}
        <AnimatePresence custom={direction} mode="popLayout">
          <motion.div
            key={currentDetail?.id}
            custom={direction}
            variants={variants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.35, ease: "easeInOut" }}
            className="relative z-20"
          >
            {currentDetail && <PolaroidDetail {...currentDetail} />}
          </motion.div>
        </AnimatePresence>

        {/* Right Preview */}
        <div
          onClick={() => paginate(1)}
          className="absolute right-[20%] top-1/2 -translate-y-1/2 scale-[0.88] opacity-60 cursor-pointer z-10 hover:opacity-80"
        >
          <PolaroidPreview data={reminds[rightIndex]} />
        </div>

        {/* Arrow Buttons */}
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

      {/* Thumbnails */}
      <div className="absolute bottom-6 left-1/2 -translate-x-1/2 w-[90%] pointer-events-auto">
        <ThumbnailStrip
          images={reminds}
          currentIndex={currentIndex}
          onSelect={(i) => {
            setDirection(i > currentIndex ? 1 : -1);
            setCurrentIndex(i);
            updateDetail(i);
          }}
        />
      </div>
    </div>
  );
}
