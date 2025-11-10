export async function serverApiClient(url: string, options: RequestInit = {}) {
  const base = process.env.NEXT_PUBLIC_POLA_API_BASE_URL ?? "";

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };

  const res = await fetch(base + url, {
    ...options,
    headers,
    cache: "no-store",
  });

  if (!res.ok) {
    throw new Error(`API Request Failed: ${res.status}`);
  }

  return res;
}
