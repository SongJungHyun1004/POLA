export async function apiClient(url: string, options: RequestInit = {}) {
  const base = process.env.NEXT_PUBLIC_POLA_API_BASE_URL ?? "";

  const accessToken = localStorage.getItem("accessToken") ?? "";

  const clientType = "WEB";

  const headers: Record<string, string> = {
    ...((options.headers as Record<string, string>) || {}),
    "X-Client-Type": clientType,
    "Content-Type": "application/json",
  };

  if (accessToken) {
    headers.Authorization = `Bearer ${accessToken}`;
  }

  let res: Response = await fetch(base + url, {
    ...options,
    headers,
    credentials: "include",
  });

  if (res.status === 401) {
    console.warn("Access Token 만료됨 → /oauth/reissue 호출");

    const refreshRes: Response = await fetch(base + "/oauth/reissue", {
      method: "POST",
      headers: {
        "X-Client-Type": clientType,
        "Content-Type": "application/json",
      },
      credentials: "include",
    });

    if (!refreshRes.ok) {
      console.error("토큰 재발급 실패:", refreshRes.status);
      localStorage.removeItem("accessToken");
      window.location.href = "/";
      throw new Error("Token refresh failed");
    }

    const tokenJson = await refreshRes.json();
    const newAccess = tokenJson?.data?.accessToken ?? "";

    if (!newAccess) {
      console.error("새로운 Access Token 누락:", tokenJson);
      localStorage.removeItem("accessToken");
      window.location.href = "/";
      throw new Error("New access token missing");
    }

    localStorage.setItem("accessToken", newAccess);

    res = await fetch(base + url, {
      ...options,
      headers: {
        ...headers,
        Authorization: `Bearer ${newAccess}`,
      },
      credentials: "include",
    });
  }

  return res;
}
