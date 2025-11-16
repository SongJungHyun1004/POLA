import { apiClient } from "@/api/apiClient";

export async function ragSearch(query: string) {
  const res = await apiClient("/rag/search", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ query }),
  });

  if (!res.ok) throw new Error("RAG 검색 실패");

  const json = await res.json();
  return json; // { answer, sources }
}
