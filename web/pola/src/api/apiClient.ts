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

  // ✅ Access Token 만료 시(401) → Refresh Token으로 재발급
  if (res.status === 401 && refreshToken) {
    const refreshRes: Response = await fetch(base + "/oauth/reissue", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${refreshToken}`,
        "Content-Type": "application/json",
      },
    });

    // Refresh 실패 → 강제 로그아웃
    if (!refreshRes.ok) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      window.location.href = "/";
      throw new Error("Token refresh failed");
    }

    const tokenJson = await refreshRes.json();

    const newAccess = tokenJson?.data?.accessToken ?? "";
    const newRefresh = tokenJson?.data?.refreshToken ?? "";

    if (!newAccess || !newRefresh) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      window.location.href = "/";
      throw new Error("New tokens missing");
    }

    // ✅ 새 토큰 저장
    localStorage.setItem("accessToken", newAccess);
    localStorage.setItem("refreshToken", newRefresh);

    // ✅ 새 Access Token으로 원래 요청 재시도
    res = await fetch(base + url, {
      ...options,
      headers: {
        ...headers,
        Authorization: `Bearer ${newAccess}`,
      },
    });
  }

  return res;
}
