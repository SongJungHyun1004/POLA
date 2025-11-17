"use client";

import { X } from "lucide-react";

interface OCRModalProps {
  text: string;
  onClose: () => void;
}

export default function OCRModal({ text, onClose }: OCRModalProps) {
  return (
    <div
      className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center"
      onClick={onClose}
    >
      <div
        className="bg-[#FFFEF8] text-[#4C3D25] w-[50%] h-[80%] rounded-xl shadow-lg flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="sticky top-0 z-10 flex justify-between items-center rounded-xl bg-[#FFFEF8] px-6 py-4 border-b border-[#E3DCC8]">
          <h2 className="text-lg font-semibold">텍스트 보기</h2>
          <button onClick={onClose}>
            <X className="w-5 h-5 text-[#4C3D25]" />
          </button>
        </div>

        <div
          className="flex-1 overflow-y-auto px-6 py-4 scrollbar-none"
          style={{
            WebkitOverflowScrolling: "touch",
            overscrollBehavior: "contain",
            touchAction: "auto",
            msOverflowStyle: "none",
            scrollbarWidth: "none",
          }}
        >
          <pre className="whitespace-pre-line text-black text-xl leading-relaxed break-words">
            {text || "(텍스트 없음)"}
          </pre>
        </div>
      </div>
    </div>
  );
}
