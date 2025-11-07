export async function apiClient(url: string, options: RequestInit = {}) {
  const base = process.env.NEXT_PUBLIC_POLA_API_BASE_URL ?? "";
  const accessToken = localStorage.getItem("accessToken") ?? "";
  const refreshToken = localStorage.getItem("refreshToken") ?? "";

  const headers: Record<string, string> = {
    ...((options.headers as Record<string, string>) || {}),
    Authorization: accessToken ? `Bearer ${accessToken}` : "",
    "Content-Type": "application/json",
  };

  let res: Response = await fetch(base + url, { ...options, headers });

  // Access Token 만료 → 401
  if (res.status === 401 && refreshToken) {
    const refreshRes: Response = await fetch(base + "/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });

    // Refresh 실패 → 로그아웃 처리
    if (!refreshRes.ok) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      window.location.href = "/";
      throw new Error("Token refresh failed");
    }

    // 새 Access Token 저장
    const newTokens = await refreshRes.json();
    const newAccessToken: string = newTokens?.data?.accessToken ?? "";

    if (!newAccessToken) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      window.location.href = "/";
      throw new Error("New access token missing");
    }

    localStorage.setItem("accessToken", newAccessToken);

    res = await fetch(base + url, {
      ...options,
      headers: {
        ...headers,
        Authorization: `Bearer ${newAccessToken}`,
      },
    });
  }

  return res;
}
