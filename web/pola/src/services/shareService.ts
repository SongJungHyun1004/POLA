import { serverApiClient } from "@/api/serverApiClient";

export interface SharedFileResponse {
  fileId: number;
  presignedUrl: string;
  downloadUrl: string;
  type: string;
  context: string;
  ocrText: string;
  fileSize: number;
  platform: string;
  originUrl: string;
  createdAt: string;
  ownerName: string;
  tags: string[];
}

export async function getSharedFileByToken(
  token: string
): Promise<SharedFileResponse> {
  const res = await serverApiClient(`/share/${token}`, { method: "GET" });

  if (res.status === 410) {
    throw new Error("EXPIRED_SHARE");
  }

  if (!res.ok) {
    throw new Error(`공유 파일 조회 실패: ${res.status}`);
  }

  const json = await res.json();
  return json.data as SharedFileResponse;
}
