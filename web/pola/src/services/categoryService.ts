import { apiClient } from "@/api/apiClient";
import { getFileList } from "./fileService";

export interface RecommendedCategory {
  categoryName: string;
  tags: string[];
}

export async function getRecommendedCategories(): Promise<
  RecommendedCategory[]
> {
  const res = await apiClient(`/categories/tags/recommendations`, {
    method: "GET",
  });

  if (!res || !res.ok) {
    throw new Error("추천 카테고리 조회 실패");
  }

  const json = await res.json();
  const arr = json?.data?.recommendations ?? [];

  return arr.map((item: any) => ({
    categoryName: item.categoryName,
    tags: item.tags ?? [],
  }));
}

export async function getCategoryInfo(categoryId: string | number) {
  const res = await apiClient(`/users/me/categories/${categoryId}`);

  if (!res || !res.ok) {
    throw new Error("카테고리 정보 조회 실패");
  }

  const json = await res.json();
  return json.data;
}

export async function getCategoryTags(categoryId: string | number) {
  const res = await apiClient(`/categories/${categoryId}/tags`);

  // 태그가 없을 경우 서버가 404를 줄 가능성 → 빈 리스트로 처리
  if (res && res.status === 404) {
    return [];
  }

  if (!res || !res.ok) {
    console.error("Tag API Failed / fallback to [] : ", res);
    return [];
  }

  const json = await res.json();
  return json.data ?? [];
}

export async function getCategoryFiles(
  categoryId: string | number,
  page: number
) {
  const fileListResponse = await getFileList({
    page,
    size: 30,
    sortBy: "createdAt",
    direction: "DESC",
    filterType: "category",
    filterId: Number(categoryId),
  });

  return fileListResponse.content;
}

export async function getFileDetail(fileId: number | string) {
  const res = await apiClient(`/files/${fileId}`);

  if (!res || !res.ok) {
    throw new Error("파일 상세 조회 실패");
  }

  const json = await res.json();
  return json.data;
}

export async function updateFileCategory(fileId: number, categoryId: number) {
  const res = await apiClient(
    `/files/${fileId}/category?categoryId=${categoryId}`,
    {
      method: "PUT",
    }
  );

  if (!res.ok) throw new Error("카테고리 변경 실패");

  const json = await res.json();
  return json.data;
}

export async function getMyCategories() {
  const res = await apiClient(`/users/me/categories`);

  if (!res.ok) {
    throw new Error("카테고리 목록 조회 실패");
  }

  const json = await res.json();
  return json.data;
}

export async function createInitialCategories(categories: any[]) {
  const res = await apiClient("/categories/tags/init", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ categories }),
  });

  if (!res.ok) throw new Error("기본 카테고리 생성 실패");

  const json = await res.json();
  return json.data;
}
