import { apiClient } from "@/api/apiClient";

export const authService = {
  googleLogin: async (idToken: string) => {
    const res = await apiClient("/oauth/token", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Client-Type": "WEB",
      },
      body: JSON.stringify({ idToken }),
      credentials: "include",
    });
    return res;
  },

  verifyAccessToken: async () => {
    const accessToken = localStorage.getItem("accessToken");
    if (!accessToken) throw new Error("No Access Token found");

    const res = await fetch(
      `${process.env.NEXT_PUBLIC_POLA_API_BASE_URL}/oauth/verify`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accessToken}`,
          "Content-Type": "application/json",
        },
        credentials: "include",
      }
    );

    if (!res.ok) throw new Error(`Verify failed: ${res.status}`);
    const json = await res.json();
    return json.data;
  },

  logout: async () => {
    const base = process.env.NEXT_PUBLIC_POLA_API_BASE_URL ?? "";
    const res = await fetch(`${base}/oauth/logout`, {
      method: "POST",
      headers: {
        "X-Client-Type": "WEB",
        "Content-Type": "application/json",
      },
      credentials: "include",
    });

    if (!res.ok) {
      console.error("로그아웃 실패:", res.status);
      throw new Error("Logout failed");
    }

    // ✅ 로컬 토큰 제거
    localStorage.removeItem("accessToken");

    // ✅ 홈(로그인)으로 이동
    window.location.href = "/";
  },
};
