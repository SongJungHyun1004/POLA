"use client";

import React from "react";
import { X } from "lucide-react";

import PolaroidDetail, {
  PolaroidDetailProps,
} from "@/app/(pola)/categories/[id]/components/PolaroidDetail";

// 2. 모달 Props 정의
interface PolaroidDetailModalProps extends PolaroidDetailProps {
  onClose: () => void;
}

export default function PolaroidDetailModal({
  onClose,
  ...itemProps 
}: PolaroidDetailModalProps) {
  return (
    <div
      className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center"
      onClick={onClose}
    >
      <div
        className="bg-[#FFFEF8] w-auto max-w-[90%] rounded-xl shadow-lg px-13 py-8 relative"
        onClick={(e) => e.stopPropagation()}
      >
        {/*  EditModal의 내부 헤더 (제목 + 닫기 버튼) */}
        <div className="flex justify-between items-center mb-7">
          <h2 className="text-xl font-semibold text-[#4C3D25] ">상세 정보</h2>
          <button onClick={onClose}>
            <X className="w-8 h-8  text-[#4C3D25]" />
          </button>
        </div>

        <PolaroidDetail {...itemProps} />
      </div>
    </div>
  );
}