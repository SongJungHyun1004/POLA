import { apiClient } from "@/api/apiClient";

export const authService = {
  googleLogin: async (idToken: string) => {
    const res = await apiClient("/oauth/token", {
      method: "POST",
      body: JSON.stringify({ idToken }),
    });
    return res;
  },
};
