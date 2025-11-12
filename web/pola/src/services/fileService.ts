import { apiClient } from "@/api/apiClient";
import { FileListRequest, FileListResponse } from "@/dtos/file";

export async function getFileDownloadUrl(fileId: number): Promise<string> {
  const res = await apiClient(`/files/download/${fileId}`, {
    method: "GET",
  });

  if (!res || !res.ok) {
    throw new Error("파일 다운로드 URL 생성 실패");
  }

  const json = await res.json();
  return json.data;
}

export async function getFileList(req: FileListRequest) {
  const body: any = {
    page: req.page,
    size: req.size,
    sortBy: req.sortBy,
    direction: req.direction,
    filterType: req.filterType,
    filterId: req.filterId,
  };

  const res = await apiClient("/files/list", {
    method: "POST",
    body: JSON.stringify(body),
  });

  if (!res.ok) throw new Error("파일 리스트 조회 실패");
  const json = await res.json();
  return json.data as FileListResponse;
}

export async function getTimelineFiles(page = 0, size = 50) {
  const res = await apiClient("/files/list", {
    method: "POST",
    body: JSON.stringify({
      page,
      size,
      sortBy: "createdAt",
      direction: "DESC",
      filterType: null,
    }),
  });

  if (!res.ok) throw new Error("타임라인 파일 조회 실패");
  const json = await res.json();

  const files = json.data?.content ?? [];

  const grouped: Record<string, any[]> = {};
  for (const file of files) {
    const date = new Date(file.createdAt)
      .toISOString()
      .split("T")[0]
      .replace(/-/g, ".");
    if (!grouped[date]) grouped[date] = [];
    grouped[date].push(file);
  }

  const result = Object.entries(grouped)
    .sort(([a], [b]) => (a < b ? 1 : -1))
    .map(([date, images]) => ({ date, images }));

  return result;
}

export async function getFileDetail(fileId: number | string) {
  const id = typeof fileId === "string" ? Number(fileId) : fileId;

  if (!id || Number.isNaN(id)) {
    throw new Error("잘못된 파일 ID");
  }

  const res = await apiClient(`/files/${id}`, {
    method: "GET",
  });

  if (!res?.ok) {
    throw new Error("파일 단건 조회 실패");
  }

  const json = await res.json();
  return json.data;
}

export async function getRemindFiles() {
  const res = await apiClient("/files/reminders", {
    method: "GET",
  });

  if (!res?.ok) {
    throw new Error("리마인드 파일 조회 실패");
  }

  const json = await res.json();
  return json.data as any[];
}

export async function createFileShareLink(fileId: number) {
  const res = await apiClient(`/files/${fileId}/share`, {
    method: "POST",
  });

  if (!res || !res.ok) {
    throw new Error("공유 링크 생성 실패");
  }

  const json = await res.json();
  return json.data;
}

export async function addFileFavorite(fileId: number) {
  const res = await apiClient(`/files/${fileId}/favorite`, {
    method: "PUT",
  });

  if (!res.ok) {
    throw new Error("즐겨찾기 추가 실패");
  }
  return true;
}

export async function removeFileFavorite(fileId: number) {
  const res = await apiClient(`/files/${fileId}/favorite`, {
    method: "DELETE",
  });

  if (!res.ok) {
    throw new Error("즐겨찾기 해제 실패");
  }
  return true;
}

export const fileService = {
  /** 파일 삭제 */
  deleteFile: async (fileId: number): Promise<void> => {
    const res = await apiClient(`/files/${fileId}`, {
      method: "DELETE",
    });

    if (!res.ok) {
      const errorData = await res.json().catch(() => ({}));
      const message = errorData?.message || "파일 삭제 중 오류가 발생했습니다.";
      throw new Error(message);
    }

    const data = await res.json();
    if (data.status !== "SUCCESS") {
      throw new Error(data.message || "파일 삭제 실패");
    }
  },
};

export const fileEditService = {
  /** (1) 파일 내용(context) 수정 */
  async updateFileContext(fileId: number, context: string) {
    const res = await apiClient(`/files/${fileId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ context }),
    });
    if (!res.ok) throw new Error("내용 수정 실패");
    return await res.json();
  },

  /** (2) 파일의 태그 목록 조회 */
  async getFileTags(fileId: number) {
    const res = await apiClient(`/files/${fileId}/tags`, { method: "GET" });
    if (!res.ok) throw new Error("태그 조회 실패");
    const data = await res.json();
    return data.data || []; // [{ id: number, tagName: string }]
  },

  /** (3) 파일에 새 태그 추가 */
  async addFileTags(fileId: number, tagNames: string[]) {
    const res = await apiClient(`/files/${fileId}/tags`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ tagNames }),
    });
    if (!res.ok) throw new Error("태그 추가 실패");
    return await res.json();
  },

  /** (4) 파일에서 특정 태그 제거 */
  async removeFileTag(fileId: number, tagId: number) {
    const res = await apiClient(`/files/${fileId}/tags/${tagId}`, {
      method: "DELETE",
    });
    if (!res.ok) throw new Error("태그 삭제 실패");
    return await res.json();
  },
};
