"use client";

import { useState, useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { ChevronDown, LayoutGrid } from "lucide-react";
import useCategoryStore from "@/store/useCategoryStore";

export default function CategoryDropdown() {
  const router = useRouter();
  const pathname = usePathname();

  const { categories, refreshCategories, isLoading } = useCategoryStore();

  const currentCategoryId = pathname.startsWith("/categories/")
    ? Number(pathname.split("/")[2])
    : null;

  const [isOpen, setIsOpen] = useState(Boolean(currentCategoryId));

  useEffect(() => {
    refreshCategories();
  }, []);

  const handleCategoryClick = (categoryId: number) => {
    router.push(`/categories/${categoryId}`);
    setIsOpen(true);
  };

  return (
    <div className="relative">
      {/* Top Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-4 px-4 py-3 w-full rounded-xl transition-colors cursor-pointer hover:bg-gray-100"
      >
        <LayoutGrid className="w-5 h-5 text-[#8B7355]" />
        <span className="text-lg font-semibold text-[#4C3D25]">Categories</span>

        <ChevronDown
          className={`w-5 h-5 text-[#8B7355] transition-transform duration-300 ${
            isOpen ? "rotate-180" : ""
          }`}
        />
      </button>

      {/* Dropdown List with Animation */}
      <div
        className={`
          overflow-hidden transition-all duration-300 
          ${isOpen ? "max-h-[600px]" : "max-h-0"}
        `}
      >
        {!isLoading && (
          <div className="pl-12 bg-white">
            {categories.map((category) => {
              const isSelected = category.id === currentCategoryId;
              return (
                <div
                  key={category.id}
                  onClick={() => handleCategoryClick(category.id)}
                  className={`
                    px-4 py-2 text-lg font-semibold text-[#4C3D25] rounded-xl cursor-pointer
                    ${
                      isSelected
                        ? "bg-[#FFF4E0] hover:bg-[#FFE7C2]"
                        : "hover:bg-gray-100"
                    }
                  `}
                >
                  {category.categoryName}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
