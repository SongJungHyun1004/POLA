"use client";

import { useState } from "react";
import CategoryCard from "./components/CategoryCard";
import CategoryModal from "./components/CategoryModal";
import Link from "next/link";

interface Category {
  name: string;
  tags: string[];
}

const initialCategories: Category[] = [
  { name: "Travel", tags: ["여행"] },
  { name: "Food", tags: ["맛집"] },
  { name: "Daily", tags: ["일상"] },
  { name: "Friends", tags: ["친구"] },
  { name: "Memories", tags: ["추억"] },
];

export default function OnboardingPage() {
  const [categories, setCategories] = useState<Category[]>(initialCategories);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editIndex, setEditIndex] = useState<number | null>(null);

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

      <Link href={"/home"}>
        <button className="px-6 py-3 bg-[#4C3D25] text-white rounded-md shadow-md hover:bg-[#3a2b1d] transition">
          POLA 시작하기
        </button>
      </Link>

      <img
        src="/images/POLA_landing_1.png"
        alt="Character"
        className="absolute bottom-0 right-0 mr-6 w-1/5"
      />
    </div>
  );
}
