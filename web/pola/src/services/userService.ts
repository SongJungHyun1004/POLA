import { apiClient } from "@/api/apiClient";

export async function getUserMe() {
  const res = await apiClient("/users/me", { method: "GET" });
  if (!res || !res.ok) return null;

  return res.json();
}

export async function getUserHome() {
  const res = await apiClient("/users/me/home", { method: "GET" });
  if (!res || !res.ok) return null;

  return res.json();
}

export const userService = {
  getMyCategories: async () => {
    const res = await apiClient("/users/me/categories", {
      method: "GET",
      credentials: "include",
    });

    if (res.status === 404) return null;
    if (!res.ok) throw new Error(`Category fetch failed: ${res.status}`);

    const json = await res.json();
    return json.data;
  },
};
