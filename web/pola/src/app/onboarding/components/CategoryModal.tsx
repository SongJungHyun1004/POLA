"use client";

import { useState, useEffect, useRef } from "react";
import { X } from "lucide-react";

interface CategoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (name: string, tags: string[]) => void;
  onDelete: () => void;
  defaultName: string;
  defaultTags: string[];
  isEditing: boolean;
  showDeleteButton?: boolean;
}

export default function CategoryModal({
  isOpen,
  onClose,
  onSave,
  onDelete,
  defaultName,
  defaultTags,
  isEditing,
  showDeleteButton = true,
}: CategoryModalProps) {
  const [name, setName] = useState(defaultName);
  const [tagInput, setTagInput] = useState("");
  const [tags, setTags] = useState<string[]>(defaultTags);
  const nameInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setName(defaultName);
    setTags(defaultTags);
    if (isOpen && nameInputRef.current) {
      nameInputRef.current.focus();
    }
  }, [defaultName, defaultTags, isOpen]);

  const handleAddTag = (val: string) => {
    const parts = val.split(" ").filter(Boolean);

    const newOnes = parts.map((t) => t.trim()).filter((t) => !tags.includes(t));

    if (newOnes.length === 0) {
      alert("이미 등록된 태그입니다.");
      setTagInput("");
      return;
    }

    setTags([...tags, ...newOnes]);
    setTagInput("");
  };

  const removeTag = (tag: string) => {
    setTags(tags.filter((t) => t !== tag));
  };

  const handleSave = () => {
    if (!name.trim()) return;
    onSave(name.trim(), tags);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black/50 backdrop-blur-sm flex justify-center items-center z-50"
      onClick={onClose}
    >
      <div
        className="bg-white w-[420px] max-w-[90%] rounded-xl shadow-lg p-5"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex justify-between items-center mb-3">
          <h2 className="text-lg font-semibold text-[#4C3D25]">
            {isEditing ? "카테고리 수정" : "카테고리 추가"}
          </h2>
          <button onClick={onClose}>
            <X className="w-5 h-5 text-[#4C3D25]" />
          </button>
        </div>

        {/* Name */}
        <label className="text-sm font-medium text-[#4C3D25]">
          카테고리 이름
        </label>
        <input
          ref={nameInputRef}
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="w-full border rounded-md px-3 py-2 mb-4 bg-[#FFFEF8]"
          placeholder="예: Travel"
        />

        {/* Tag input */}
        <label className="text-sm font-medium text-[#4C3D25]">
          태그 입력 (Space)
        </label>
        <input
          value={tagInput}
          onChange={(e) => {
            if (e.target.value.includes(" ")) handleAddTag(e.target.value);
            else setTagInput(e.target.value);
          }}
          onKeyDown={(e) => e.key === "Enter" && handleAddTag(tagInput)}
          className="w-full border rounded-md px-3 py-2 mb-2 bg-[#FFFEF8]"
          placeholder="예: 여행 카페"
        />

        <div className="flex flex-wrap gap-2 mb-4">
          {tags.map((tag) => (
            <span
              key={tag}
              className="flex items-center bg-[#EFE9DA] px-2 py-1 rounded-full text-sm text-[#4C3D25]"
            >
              #{tag}
              <button
                onClick={() => removeTag(tag)}
                className="ml-1 text-xs text-red-500"
              >
                ✕
              </button>
            </span>
          ))}
        </div>

        {/* Buttons */}
        <div className="flex justify-between items-center mt-4">
          {/* 삭제 버튼 — 편집시에만 */}
          {isEditing && showDeleteButton && (
            <button
              onClick={onDelete}
              className="px-4 py-2 text-sm text-red-500 hover:text-red-700"
            >
              삭제
            </button>
          )}

          <div className="flex gap-2 ml-auto">
            <button
              onClick={onClose}
              className="px-4 py-2 border border-[#C5BEAE] rounded-md text-sm bg-[#EFE9DA]"
            >
              취소
            </button>
            <button
              onClick={handleSave}
              className="px-4 py-2 rounded-md text-sm bg-[#4C3D25] text-white"
            >
              저장
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
