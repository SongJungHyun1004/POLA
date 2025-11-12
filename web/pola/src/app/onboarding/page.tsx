"use client";

import { useState, useEffect } from "react";
import CategoryCard from "./components/CategoryCard";
import CategoryModal from "./components/CategoryModal";
import Link from "next/link";
import {
  getRecommendedCategories,
  createInitialCategories,
} from "@/services/categoryService";

interface Category {
  name: string;
  tags: string[];
}

export default function OnboardingPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editIndex, setEditIndex] = useState<number | null>(null);

  // ✅ 추천 카테고리 불러오기
  useEffect(() => {
    async function loadRecommendations() {
      try {
        const data = await getRecommendedCategories(); // 서버 호출
        const mapped = data.map((item) => ({
          name: item.categoryName,
          tags: item.tags,
        }));
        setCategories(mapped);
      } catch (err) {
        console.error("추천 카테고리를 불러오지 못했습니다.", err);
        // 서버가 실패하면 기본 2개 넣어서라도 페이지 작동 보장
        setCategories([
          { name: "Daily", tags: ["일상"] },
          { name: "Memory", tags: ["추억"] },
        ]);
      }
    }

    loadRecommendations();
  }, []);

  const openAdd = () => {
    if (categories.length >= 10) return;
    setEditIndex(null);
    setIsModalOpen(true);
  };

  const openEdit = (index: number) => {
    setEditIndex(index);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setEditIndex(null);
    setIsModalOpen(false);
  };

  const handleSaveCategory = (name: string, tags: string[]) => {
    if (editIndex === null) {
      setCategories([...categories, { name, tags }]);
    } else {
      const updated = [...categories];
      updated[editIndex] = { name, tags };
      setCategories(updated);
    }
  };

  const deleteCategory = (index: number) => {
    if (categories.length <= 2) {
      alert("2개 이하로 줄일 수 없습니다!");
      return;
    }
    setCategories(categories.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    try {
      await createInitialCategories(
        categories.map((c) => ({
          categoryName: c.name,
          tags: c.tags,
        }))
      );
      window.location.href = "/home";
    } catch (err) {
      alert("카테고리 저장 중 오류가 발생했습니다.");
      console.error(err);
    }
  };

  return (
    <div className="min-h-screen bg-[#FFFEF8] flex flex-col items-center justify-center relative">
      <h1 className="text-4xl font-semibold text-[#4C3D25] mb-8">
        시작하기 전, 관심사를 설정해볼까요?
      </h1>
      <p className="text-lg text-gray-600 mb-6">
        2~10개의 카테고리를 설정해주세요.
      </p>

      <div className="grid grid-cols-4 gap-4 mb-6 w-full max-w-screen-lg">
        {categories.map((cat, index) => (
          <CategoryCard
            key={index}
            category={cat.name}
            onClick={() => openEdit(index)}
            className="w-full"
          />
        ))}

        {categories.length < 10 && (
          <CategoryCard category="+" onClick={openAdd} className="w-full" />
        )}
      </div>

      <CategoryModal
        isOpen={isModalOpen}
        onClose={closeModal}
        onSave={handleSaveCategory}
        onDelete={() => {
          deleteCategory(editIndex!);
          closeModal();
        }}
        defaultName={editIndex !== null ? categories[editIndex].name : ""}
        defaultTags={editIndex !== null ? categories[editIndex].tags : []}
        isEditing={editIndex !== null}
      />

      <button
        onClick={handleSubmit}
        className="px-6 py-3 bg-[#4C3D25] text-white rounded-md shadow-md hover:bg-[#3a2b1d] transition"
      >
        POLA 시작하기
      </button>

      <img
        src="/images/POLA_landing_1.png"
        alt="Character"
        className="absolute bottom-0 right-0 mr-6 w-1/5"
      />
    </div>
  );
}
