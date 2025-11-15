"use client";

import { useState, useEffect } from "react";

import {
  updateCategoryName,
  fetchCategoryTags,
  addCategoryTags,
  removeCategoryTag,
  deleteMyCategory,
  fetchMyCategories,
} from "@/services/categoryService";
import { apiClient } from "@/api/apiClient";

export default function MyCategoryPage() {
  const [categories, setCategories] = useState<any[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const [name, setName] = useState("");
  const [tags, setTags] = useState<string[]>([]);
  const [tagInput, setTagInput] = useState("");

  const [isAddMode, setIsAddMode] = useState(false); // + 버튼 클릭 시 활성화

  /* ---------------------- 초기 로딩 ---------------------- */
  useEffect(() => {
    async function load() {
      const list = await fetchMyCategories();
      setCategories(list);

      if (list.length > 0) {
        // 첫 번째 카테고리 자동 선택
        const first = list[0];
        loadCategoryDetail(first.id, first.categoryName);
      }
    }
    load();
  }, []);

  /* ---------------------- 한 카테고리 불러오기 ---------------------- */
  async function loadCategoryDetail(id: number, categoryName: string) {
    setSelectedId(id);
    setIsAddMode(false);
    setName(categoryName);

    try {
      const rawTags = await fetchCategoryTags(id); // [{id, tagName}]
      setTags(rawTags.map((t: any) => t.tagName));
    } catch {
      setTags([]);
    }
  }

  /* ---------------------- 태그 추가 ---------------------- */
  const handleTagInput = (val: string) => {
    if (val.includes(" ")) {
      const parts = val.split(" ").filter(Boolean);

      const newTags = parts
        .map((t) => t.trim())
        .filter((t) => !tags.includes(t));

      if (newTags.length === 0) {
        setTagInput("");
        return;
      }
      setTags([...tags, ...newTags]);
      setTagInput("");
    } else {
      setTagInput(val);
    }
  };

  /* ---------------------- 태그 제거 ---------------------- */
  const removeTagLocal = (tag: string) => {
    setTags((prev) => prev.filter((t) => t !== tag));
  };

  /* ---------------------- 카테고리 저장 ---------------------- */
  async function handleSave() {
    if (!name.trim()) {
      alert("카테고리명을 입력해주세요.");
      return;
    }
    if (!selectedId) return;

    try {
      // 1) 이름 변경
      const selectedCategory = categories.find((c) => c.id === selectedId);
      if (selectedCategory.categoryName !== name) {
        await updateCategoryName(selectedId, name);
      }

      // 2) 기존 태그 로딩
      const current = await fetchCategoryTags(selectedId);
      const currentNames = current.map((t: any) => t.tagName);

      // 3) 추가해야 하는 태그
      const toAdd = tags.filter((t) => !currentNames.includes(t));
      if (toAdd.length > 0) {
        await addCategoryTags(selectedId, toAdd);
      }

      // 4) 삭제해야 하는 태그
      const toRemove = current.filter((t: any) => !tags.includes(t.tagName));
      for (const r of toRemove) {
        await removeCategoryTag(selectedId, r.id);
      }

      alert("카테고리가 수정되었습니다!");
    } catch (err) {
      console.error(err);
      alert("저장 중 오류가 발생했습니다.");
    }
  }

  /* ---------------------- 카테고리 삭제 ---------------------- */
  async function handleDelete() {
    if (!selectedId) return;
    if (!confirm("정말 삭제하시겠습니까?")) return;

    try {
      await deleteMyCategory(selectedId);

      // 새 목록 로딩
      const list = await fetchMyCategories();
      setCategories(list);

      if (list.length > 0) {
        loadCategoryDetail(list[0].id, list[0].categoryName);
      } else {
        // 아무것도 없을 때
        setSelectedId(null);
        setName("");
        setTags([]);
      }
    } catch {
      alert("삭제 실패");
    }
  }

  /* ---------------------- 새 카테고리 추가 모드 ---------------------- */
  function startAddMode() {
    setIsAddMode(true);
    setSelectedId(null);
    setName("");
    setTags([]);
    setTagInput("");
  }

  async function handleAddNew() {
    if (!name.trim()) {
      alert("카테고리명을 입력해주세요.");
      return;
    }

    try {
      // 1) 이름을 먼저 생성
      const res = await apiClient(`/users/me/categories?name=${name}`, {
        method: "POST",
      });
      if (!res.ok) throw new Error("카테고리 생성 실패");

      const json = await res.json();
      const newId = json.data.id;

      // 2) 태그 추가
      if (tags.length > 0) {
        await addCategoryTags(newId, tags);
      }

      alert("새 카테고리가 생성되었습니다.");

      // 3) 목록 다시 불러오기
      const list = await fetchMyCategories();
      setCategories(list);
      loadCategoryDetail(newId, name);

      setIsAddMode(false);
    } catch (err) {
      alert("카테고리 생성 중 오류 발생");
    }
  }

  /* ---------------------- 화면 렌더 ---------------------- */
  return (
    <div className="w-full h-full flex justify-center pb-6">
      <div className="w-full h-full max-w-[900px] flex gap-6">
        {/* ---------------- LEFT: 카테고리 목록 ---------------- */}
        <div className="w-1/3 bg-[#F4EFE2] rounded-2xl p-6 shadow-sm h-full flex flex-col">
          {/* 스크롤 가능한 카테고리 리스트 */}
          <div className="flex-1 overflow-y-auto scrollbar-thin scrollbar-thumb-[#CBBF9E]/50 pr-2">
            {categories.map((c) => (
              <button
                key={c.id}
                onClick={() => loadCategoryDetail(c.id, c.categoryName)}
                className={`
          w-full flex items-center justify-between
          px-5 py-5 rounded-xl mb-3 transition-all
          ${
            selectedId === c.id && !isAddMode
              ? "bg-white shadow-md"
              : "bg-[#F4EFE2]"
          }
        `}
              >
                <span className="font-semibold text-[#4C3D25] text-xl">
                  {c.categoryName}
                </span>

                <span className="text-[#7A6A48] text-lg">
                  {c.fileCount} files
                </span>
              </button>
            ))}
          </div>

          {/* 고정된 하단 버튼 */}
          <button
            onClick={startAddMode}
            disabled={categories.length > 10}
            className={`
      w-full mt-4 rounded-xl py-4 font-semibold text-xl
      transition-all
      ${
        categories.length > 10
          ? "bg-[#E0DBD2] text-[#A9A29A] cursor-not-allowed"
          : "bg-[#D8D1C4] hover:bg-[#C9C2B5] text-[#4C3D25]"
      }
    `}
          >
            + 새 카테고리 추가
          </button>

          {/* 제한 안내문 — 최대 10개 */}
          {categories.length > 10 && (
            <p className="text-center text-sm text-red-500 mt-2">
              카테고리는 최대 10개까지 생성할 수 있습니다.
            </p>
          )}
        </div>

        {/* ----------------------- RIGHT: 카테고리 편집 패널 ----------------------- */}
        <div className="flex-1 bg-[#FFFFFF] border border-[#E3DCC8] rounded-2xl shadow-sm p-8 flex flex-col">
          {/* ---- 스크롤 가능한 영역 ---- */}
          <div className="flex-1 overflow-y-auto pr-2 scrollbar-thin scrollbar-thumb-[#CBBF9E]/50">
            {/* 카테고리 이름 */}
            <label className="text-xl font-semibold text-[#4C3D25] mb-3 block">
              카테고리 이름
            </label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full border border-[#CFC8B8] rounded-lg 
        px-4 py-4 mb-8 bg-[#FFFEF8] text-lg 
        focus:outline-none focus:ring-2 focus:ring-[#C5BBA3]"
              placeholder="예: 취미"
            />

            {/* 태그 입력 */}
            <label className="text-xl font-semibold text-[#4C3D25] mb-3 block">
              태그 입력 (Space)
            </label>

            <input
              value={tagInput}
              onChange={(e) => handleTagInput(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleTagInput(tagInput)}
              className="w-full border border-[#CFC8B8] rounded-lg 
        px-4 py-4 mb-6 bg-[#FFFEF8] text-lg 
        focus:outline-none focus:ring-2 focus:ring-[#C5BBA3]"
              placeholder="예: 여행 카페 글쓰기 영화감상"
            />

            {/* 태그 리스트 */}
            <div className="flex flex-wrap gap-3 mb-10">
              {tags.map((tag) => (
                <span
                  key={tag}
                  className="flex items-center bg-[#EFE9DA] px-4 py-2 rounded-full 
            text-lg text-[#4C3D25] font-medium"
                >
                  #{tag}
                  <button
                    onClick={() => removeTagLocal(tag)}
                    className="ml-2 text-[15px] text-red-500 hover:text-red-700"
                  >
                    ✕
                  </button>
                </span>
              ))}
            </div>
          </div>

          {/* Divider */}
          <div className="border-t border-[#E3DCC8] my-6"></div>

          {/* ---------------- Bottom Fixed Buttons ---------------- */}
          <div className="flex justify-between items-center">
            {isAddMode ? (
              <button
                onClick={handleAddNew}
                className="px-6 py-4 rounded-xl bg-[#4C3D25] text-white 
          text-xl font-semibold hover:bg-[#3A311F]"
              >
                카테고리 추가
              </button>
            ) : (
              <>
                {/* 삭제 */}
                <button
                  onClick={handleDelete}
                  className="px-4 py-3 text-[#C0392B] font-semibold text-lg 
            hover:text-red-700"
                >
                  삭제
                </button>

                <div className="flex gap-4">
                  {/* 취소 */}
                  <button
                    onClick={() => {
                      if (selectedId) {
                        const c = categories.find((c) => c.id === selectedId);
                        loadCategoryDetail(c.id, c.categoryName);
                      }
                    }}
                    className="px-6 py-3 text-lg font-medium bg-[#EFE9DA] 
              border border-[#C5BEAE] rounded-xl 
              hover:bg-[#E0D8C9]"
                  >
                    취소
                  </button>

                  {/* 저장 */}
                  <button
                    onClick={handleSave}
                    className="px-6 py-3 rounded-xl bg-[#4C3D25] text-white 
              text-lg font-semibold hover:bg-[#3A311F]"
                  >
                    저장
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
