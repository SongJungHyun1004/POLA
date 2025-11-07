export async function apiClient(url: string, options: RequestInit = {}) {
  if (typeof window === "undefined") {
    throw new Error("apiClient cannot be used on the server.");
  }

  const baseUrl = process.env.NEXT_PUBLIC_POLA_API_BASE_URL;
  const accessToken = localStorage.getItem("accessToken");
  const refreshToken = localStorage.getItem("refreshToken");

  const originalBody = options.body;

  const request = async (token?: string) => {
    const headers = {
      ...(options.headers || {}),
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };

    return fetch(baseUrl + url, {
      ...options,
      body: originalBody,
      headers,
    });
  };

  let response = await request(accessToken || undefined);

  if (response.status === 401 && refreshToken) {
    const refreshResponse = await fetch(baseUrl + "/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });

    if (!refreshResponse.ok) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      window.location.href = "/";
      throw new Error("Token refresh failed");
    }

    const newTokens = await refreshResponse.json();
    localStorage.setItem("accessToken", newTokens.data.accessToken);

    if (newTokens.data.refreshToken) {
      localStorage.setItem("refreshToken", newTokens.data.refreshToken);
    }

    response = await request(newTokens.data.accessToken);
  }

  if (!response.ok && response.status >= 500) {
    console.error("Server Error:", response.status, url);
  }

  return response;
}
