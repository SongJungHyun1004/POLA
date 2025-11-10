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
