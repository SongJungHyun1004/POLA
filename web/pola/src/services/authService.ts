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
};
