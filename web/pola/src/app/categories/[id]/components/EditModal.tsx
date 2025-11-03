"use client";

import { X } from "lucide-react";
import { useState } from "react";

interface EditModalProps {
  defaultTags: string[];
  defaultContext: string;
  onClose: () => void;
  onSave: (tags: string[], context: string) => void;
}

export default function EditModal({
  defaultTags,
  defaultContext,
  onClose,
  onSave,
}: EditModalProps) {
  const [tags, setTags] = useState(defaultTags.join(" "));
  const [context, setContext] = useState(defaultContext);

  const handleSave = () => {
    const newTags = tags.split(" ").filter((t) => t.trim() !== "");
    onSave(newTags, context);
    onClose();
  };

  return (
    <div
      className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50"
      onClick={onClose}
    >
      <div
        className="bg-white w-[420px] max-w-[90%] rounded-xl shadow-lg p-5"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex justify-between items-center mb-3">
          <h2 className="text-lg font-semibold text-[#4C3D25]">정보 수정</h2>
          <button onClick={onClose}>
            <X className="w-5 h-5 text-[#4C3D25]" />
          </button>
        </div>

        {/* Tag Input */}
        <label className="text-sm text-[#4C3D25] font-medium">태그</label>
        <input
          className="w-full border rounded-md px-3 py-2 mb-4 text-sm bg-[#FFFEF8] text-[#4C3D25] focus:outline-none"
          value={tags}
          onChange={(e) => setTags(e.target.value)}
          placeholder="#여행 #카페 #일상"
        />

        {/* Context Input */}
        <label className="text-sm text-[#4C3D25] font-medium">내용</label>
        <textarea
          className="w-full border rounded-md px-3 py-2 h-32 text-sm bg-[#FFFEF8] text-[#4C3D25] resize-none focus:outline-none"
          value={context}
          onChange={(e) => setContext(e.target.value)}
          placeholder="내용을 입력하세요..."
        />

        {/* Buttons */}
        <div className="mt-4 flex justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 border border-[#C5BEAE] rounded-md text-sm text-[#4C3D25] bg-[#EFE9DA] hover:bg-[#E3DAC5]"
          >
            취소
          </button>
          <button
            onClick={handleSave}
            className="px-4 py-2 rounded-md text-sm bg-[#4C3D25] text-white hover:bg-[#3c321f]"
          >
            저장
          </button>
        </div>
      </div>
    </div>
  );
}
