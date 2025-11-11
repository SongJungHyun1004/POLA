import { apiClient } from "@/api/apiClient";

export interface FavoriteFileListRequest {
  page: number;
  size: number;
  sortBy: string;
  direction: "ASC" | "DESC";
  filterType: "favorite";
}

/** 즐겨찾기 파일 리스트 조회 (페이지네이션 지원) */
export async function getFavoriteFiles(page = 0, size = 30) {
  const body: FavoriteFileListRequest = {
    page,
    size,
    sortBy: "createdAt",
    direction: "DESC",
    filterType: "favorite",
  };

  const res = await apiClient("/files/list", {
    method: "POST",
    body: JSON.stringify(body),
  });

  if (!res.ok) throw new Error("즐겨찾기 목록 조회 실패");

  const json = await res.json();
  return json.data?.content ?? [];
}
