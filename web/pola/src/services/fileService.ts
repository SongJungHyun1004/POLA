import { apiClient } from "@/api/apiClient";
import { FileListRequest, FileListResponse } from "@/dtos/file";

export async function getFileList(req: FileListRequest) {
  const body: any = {
    page: req.page,
    size: req.size,
    sortBy: req.sortBy,
    direction: req.direction,
    filterType: req.filterType,
  };

  if (req.filterType === "category" && req.filterId !== undefined) {
    body.filterId = req.filterId;
  }

  const res = await apiClient("/files/list", {
    method: "POST",
    body: JSON.stringify(body),
  });

  if (!res?.ok) {
    throw new Error("파일 리스트 조회 실패");
  }

  const json = await res.json();
  return json.data as FileListResponse;
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
