"use client";

import { create } from "zustand";
import { getMyCategories } from "@/services/categoryService";

interface CategoryStore {
  categories: any[];
  isLoading: boolean;

  refreshCategories: () => Promise<void>;
}

const useCategoryStore = create<CategoryStore>((set) => ({
  categories: [],
  isLoading: false,

  refreshCategories: async () => {
    set({ isLoading: true });
    try {
      const list = await getMyCategories();
      set({ categories: list });
    } catch (e) {
      console.error("카테고리 목록 갱신 실패:", e);
    } finally {
      set({ isLoading: false });
    }
  },
}));

export default useCategoryStore;
