"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { getMyCategories } from "@/services/categoryService";
import { ChevronDown, LayoutGrid } from "lucide-react";

export default function CategoryDropdown() {
  const [categories, setCategories] = useState<any[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const categoryList = await getMyCategories();
        setCategories(categoryList);
      } catch (error) {
        console.error("Failed to fetch categories:", error);
      }
    };

    fetchCategories();
  }, []);

  const handleCategoryClick = (categoryId: number) => {
    router.push(`/categories/${categoryId}`);
    setIsOpen(false);
  };

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-4 px-4 py-3 rounded-xl transition-colors cursor-pointer hover:bg-gray-50"
      >
        <LayoutGrid className="w-5 h-5 text-[#8B7355]" />
        <span className="text-lg font-semibold text-[#4C3D25]">Categories</span>
        <ChevronDown className={`w-5 h-5 text-[#8B7355] transition-transform ${isOpen ? 'rotate-180' : ''}`} />
      </button>
      {isOpen && (
        <div className="pl-12 bg-white">
          {categories.map((category) => (
            <div
              key={category.id}
              onClick={() => handleCategoryClick(category.id)}
              className="px-4 py-2 text-lg font-semibold text-[#4C3D25] hover:bg-gray-50 cursor-pointer"            >
              {category.categoryName}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
