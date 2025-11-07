import { apiClient } from "@/api/apiClient";
import { getFileList } from "./fileService"; // ✅ FileList 함수는 별도 서비스로 분리 권장

// 1) 카테고리 기본 정보 조회
export async function getCategoryInfo(categoryId: string | number) {
  const res = await apiClient(`/users/me/categories/${categoryId}`);

  if (!res || !res.ok) {
    throw new Error("카테고리 정보 조회 실패");
  }

  const json = await res.json();
  return json.data;
}

// ✅ 2) 카테고리 태그 조회
export async function getCategoryTags(categoryId: string | number) {
  const res = await apiClient(`/categories/${categoryId}/tags`);

  // ✅ 태그가 없을 경우 서버가 404를 반환한다면 → 빈 리스트 처리
  if (res && res.status === 404) {
    return [];
  }

  // ✅ 기타 실패는 오류 처리
  if (!res || !res.ok) {
    console.error("Tag API Response:", res);
    return []; // ← 태그 없음을 오류로 처리하지 않음
  }

  const json = await res.json();
  return json.data ?? [];
}

// 3) 카테고리 파일 리스트 조회 → ✅ 파일 리스트 API(/files/list) 기반으로 맞춤
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

  // fileListResponse.content 가 실제 파일 배열
  return fileListResponse.content;
}

// 4) 파일 상세 조회
export async function getFileDetail(fileId: number) {
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
  if (!res.ok) throw new Error("카테고리 목록 조회 실패");

  const json = await res.json();
  return json.data;
}
