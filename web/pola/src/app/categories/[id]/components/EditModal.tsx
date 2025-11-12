"use client";

import { fileEditService } from "@/services/fileService";
import { X } from "lucide-react";
import { useEffect, useRef, useState } from "react";

interface EditModalProps {
  fileId: number;
  defaultTags: string[];
  defaultContext: string;
  defaultCategoryId: number;
  categories: { id: number; categoryName: string }[];
  onClose: () => void;
  onSave: () => void;
}

export default function EditModal({
  fileId,
  defaultTags,
  defaultContext,
  defaultCategoryId,
  categories,
  onClose,
  onSave,
}: EditModalProps) {
  const [context, setContext] = useState(defaultContext);
  const [selectedCategory, setSelectedCategory] =
    useState<number>(defaultCategoryId);

  const [tagInput, setTagInput] = useState("");
  const [tags, setTags] = useState<string[]>(
    defaultTags.map((t) => t.replace(/^#/, "")) // 초기 태그에서 '#' 제거
  );
  const [saving, setSaving] = useState(false);
  const tagInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setContext(defaultContext);
    setTags(defaultTags.map((t) => t.replace(/^#/, ""))); // 다시 '#' 제거
    setSelectedCategory(defaultCategoryId);
  }, [defaultContext, defaultTags, defaultCategoryId]);

  /** ✅ 태그 추가 시 '#' 제거하고 중복 방지 */
  const handleAddTag = (val: string) => {
    const parts = val
      .split(" ")
      .map((t) => t.trim().replace(/^#/, "")) // '#' 제거
      .filter(Boolean);

    const newOnes = parts.filter((t) => !tags.includes(t));

    if (newOnes.length === 0) {
      setTagInput("");
      return;
    }

    setTags([...tags, ...newOnes]);
    setTagInput("");
  };

  /** ✅ 태그 제거 */
  const handleRemoveTag = (tag: string) => {
    setTags(tags.filter((t) => t !== tag));
  };

  /** ✅ 저장 버튼 클릭 */
  const handleSave = async () => {
    if (!fileId || saving) return;
    setSaving(true);

    try {
      // 1️⃣ context 수정
      await fileEditService.updateFileContext(fileId, context);

      // 2️⃣ 기존 태그 재조회
      const existing = await fileEditService.getFileTags(fileId);
      const existingNames = existing.map((t: any) => t.tagName);
      const existingMap: Record<string, number> = {};
      existing.forEach((t: any) => (existingMap[t.tagName] = t.id));

      // 3️⃣ 추가해야 할 태그
      const toAdd = tags.filter((t) => !existingNames.includes(t));
      if (toAdd.length > 0) await fileEditService.addFileTags(fileId, toAdd);

      // 4️⃣ 삭제해야 할 태그
      const toDelete = existing.filter((t: any) => !tags.includes(t.tagName));
      for (const del of toDelete) {
        await fileEditService.removeFileTag(fileId, del.id);
      }

      alert("파일 정보가 수정되었습니다.");
      onSave();
      onClose();
    } catch (err) {
      console.error("수정 중 오류:", err);
      alert("수정 중 오류가 발생했습니다.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center"
      onClick={onClose}
    >
      <div
        className="bg-white w-[420px] max-w-[90%] rounded-xl shadow-lg p-5 relative"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex justify-between items-center mb-3">
          <h2 className="text-lg font-semibold text-[#4C3D25]">정보 수정</h2>
          <button onClick={onClose}>
            <X className="w-5 h-5 text-[#4C3D25]" />
          </button>
        </div>

        {/* 카테고리 */}
        <label className="text-sm text-[#4C3D25] font-medium">카테고리</label>
        <div className="relative mb-4">
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(Number(e.target.value))}
            className="w-full px-3 py-2 text-sm bg-[#FFFEF8] text-[#4C3D25] border rounded-md focus:outline-none"
          >
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.categoryName}
              </option>
            ))}
          </select>
        </div>

        {/* 태그 */}
        <label className="text-sm text-[#4C3D25] font-medium">
          태그 입력 (Space)
        </label>
        <input
          ref={tagInputRef}
          value={tagInput}
          onChange={(e) => {
            if (e.target.value.includes(" ")) handleAddTag(e.target.value);
            else setTagInput(e.target.value);
          }}
          onKeyDown={(e) => e.key === "Enter" && handleAddTag(tagInput)}
          className="w-full border rounded-md px-3 py-2 mb-2 text-sm bg-[#FFFEF8] text-[#4C3D25] focus:outline-none"
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
                onClick={() => handleRemoveTag(tag)}
                className="ml-1 text-xs text-red-500"
              >
                ✕
              </button>
            </span>
          ))}
        </div>

        {/* 내용 */}
        <label className="text-sm text-[#4C3D25] font-medium">내용</label>
        <textarea
          className="w-full border rounded-md px-3 py-2 h-32 text-sm bg-[#FFFEF8] text-[#4C3D25] resize-none focus:outline-none"
          value={context}
          onChange={(e) => setContext(e.target.value)}
          placeholder="내용을 입력하세요..."
        />

        {/* 버튼 */}
        <div className="mt-4 flex justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2 border border-[#C5BEAE] rounded-md text-sm text-[#4C3D25] bg-[#EFE9DA] hover:bg-[#E3DAC5]"
          >
            취소
          </button>
          <button
            onClick={handleSave}
            disabled={saving}
            className="px-4 py-2 rounded-md text-sm bg-[#4C3D25] text-white hover:bg-[#3c321f] disabled:opacity-60"
          >
            {saving ? "저장 중..." : "저장"}
          </button>
        </div>
      </div>
    </div>
  );
}
