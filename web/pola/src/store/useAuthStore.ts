import { create } from "zustand";
import { persist } from "zustand/middleware";

interface User {
  id: number | null;
  email: string | null;
  display_name: string | null;
  profile_image_url: string | null;
}

interface AuthStore {
  user: User | null;
  setUser: (user: User) => void;
  clearUser: () => void;
}

const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      user: null,
      setUser: (user: User) => set({ user }),
      clearUser: () => set({ user: null }),
    }),
    {
      name: "pola-auth",
      storage:
        typeof window !== "undefined" ? localStorageStorage() : undefined,
    }
  )
);

function localStorageStorage() {
  return {
    getItem: (name: string) => {
      const value = localStorage.getItem(name);
      return value ? JSON.parse(value) : null;
    },
    setItem: (name: string, value: unknown) => {
      localStorage.setItem(name, JSON.stringify(value));
    },
    removeItem: (name: string) => {
      localStorage.removeItem(name);
    },
  };
}

export default useAuthStore;
