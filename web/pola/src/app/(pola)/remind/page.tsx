"use client";

import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";

import PolaroidPreview from "./components/PolaroidPreview";
import ThumbnailStrip from "./components/ThumbnailStrip";
import SelectedPreview from "./components/SelectedPreview";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { getRemindFiles, getFileDetail } from "@/services/fileService";
import PolaroidDetailModal from "../timeline/components/modals/PolaroidDetailModal";

import { KeyboardEvent as ReactKeyboardEvent } from "react";

export default function RemindPage() {
  const [reminds, setReminds] = useState<any[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [direction, setDirection] = useState<1 | -1>(1);

  // 모달용
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<any | null>(null);

  const [loading, setLoading] = useState(true);

  // 초기 로딩
  useEffect(() => {
    async function load() {
      try {
        const list = await getRemindFiles();
        setReminds(list);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  // 키보드 액션 처리
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // 입력창에 포커스 있을 경우 동작 금지
      const active = document.activeElement;
      if (
        active instanceof HTMLInputElement ||
        active instanceof HTMLTextAreaElement ||
        active instanceof HTMLSelectElement ||
        active?.getAttribute("contenteditable") === "true"
      ) {
        return;
      }

      // 왼쪽
      if (e.key === "ArrowLeft") {
        paginate(-1);
      }

      // 오른쪽
      if (e.key === "ArrowRight") {
        paginate(1);
      }

      // Space → 다음 이미지
      if (e.key === " ") {
        e.preventDefault(); // 페이지 스크롤 방지
        paginate(1);
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [reminds, currentIndex]);

  const paginate = (dir: 1 | -1) => {
    if (reminds.length === 0) return;
    setDirection(dir);
    const nextIndex = (currentIndex + dir + reminds.length) % reminds.length;
    setCurrentIndex(nextIndex);
  };

  const openModalWithDetail = (item: any) => {
    // 1) Remind 리스트의 썸네일 데이터를 먼저 전달 → 빠르게 렌더링됨
    setSelectedItem({
      id: item.id,
      src: item.src,
      type: item.type,
      platform: item.platform,
      ocr_text: item.ocr_text,
      tags: [], // 상세조회 후 갱신됨
      contexts: "",
      date: item.createdAt || item.date, // 즉시 날짜 표시
      favorite: item.favorite,
      categoryId: item.categoryId,
    });

    setIsModalOpen(true);

    // 2) 백그라운드에서 상세 데이터 가져와 모달 갱신
    getFileDetail(item.id)
      .then((detail) => {
        const tags = (detail.tags ?? []).map((t: any) => `#${t.tagName}`);
        setSelectedItem({
          id: detail.id,
          src: detail.src,
          type: detail.type,
          platform: detail.platform,
          ocr_text: detail.ocr_text,
          favorite: detail.favorite,
          tags,
          contexts: detail.context ?? "",
          date: detail.createdAt ?? detail.created_at,
          categoryId: detail.categoryId,
        });
      })
      .catch((err) => {
        console.error("❌ Failed to fetch detail:", err);
      });
  };

  // 로딩
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

  const leftIndex = (currentIndex - 1 + reminds.length) % reminds.length;
  const rightIndex = (currentIndex + 1) % reminds.length;

  const variants = {
    enter: (direction: number) => ({
      x: direction > 0 ? 250 : -250,
      opacity: 0,
      scale: 0.85,
    }),
    center: { x: 0, opacity: 1, scale: 1 },
    exit: (direction: number) => ({
      x: direction > 0 ? -250 : 250,
      opacity: 0,
      scale: 0.85,
    }),
  };

  return (
    <div className="w-full h-full flex justify-center">
      <div className="relative w-full max-w-[1200px] h-full text-[#4C3D25] overflow-hidden flex flex-col px-6 pt-6">
        {/* Title */}
        <h1 className="text-5xl font-bold mb-6 flex-none pl-4">Remind</h1>

        {/* Slider */}
        <div className="relative flex-none h-[460px] flex items-center justify-center -mt-8">
          {/* Left Preview */}
          <div
            onClick={() => paginate(-1)}
            className="absolute left-[20%] top-1/2 -translate-y-1/2 scale-[0.88] opacity-60 cursor-pointer z-10 hover:opacity-80"
          >
            <PolaroidPreview data={reminds[leftIndex]} />
          </div>

          {/* Center Selected Preview */}
          <AnimatePresence custom={direction} mode="popLayout">
            <motion.div
              key={reminds[currentIndex]?.id}
              custom={direction}
              variants={variants}
              initial="enter"
              animate="center"
              exit="exit"
              transition={{ duration: 0.35, ease: "easeInOut" }}
              className="relative z-20"
            >
              <SelectedPreview
                {...reminds[currentIndex]}
                onClick={() => openModalWithDetail(reminds[currentIndex])}
              />
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
        <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-[90%] pointer-events-auto">
          <ThumbnailStrip
            images={reminds}
            currentIndex={currentIndex}
            onSelect={(i) => {
              setDirection(i > currentIndex ? 1 : -1);
              setCurrentIndex(i);
            }}
          />
        </div>
      </div>

      {/* Detail Modal */}
      {isModalOpen && selectedItem && (
        <PolaroidDetailModal
          {...selectedItem}
          onClose={() => {
            setIsModalOpen(false);
            setSelectedItem(null);
          }}
        />
      )}
    </div>
  );
}
