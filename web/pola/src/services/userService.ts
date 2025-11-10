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
