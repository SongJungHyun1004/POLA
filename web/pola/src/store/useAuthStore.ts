import { create } from "zustand";

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

const useAuthStore = create<AuthStore>((set) => ({
  user: null,
  setUser: (user: User) => set(() => ({ user })),
  clearUser: () => set(() => ({ user: null })),
}));

export default useAuthStore;
