import { apiClient } from "@/api/apiClient";

export interface PresignedUploadResponse {
  url: string;
  key: string;
}

export const uploadService = {
  /** 1. Presigned URL 요청 */
  getPresignedUploadUrl: async (
    fileName: string
  ): Promise<PresignedUploadResponse> => {
    const res = await apiClient(
      `/s3/presigned/upload?fileName=${encodeURIComponent(fileName)}`,
      {
        method: "GET",
        credentials: "include",
      }
    );

    if (!res.ok) throw new Error("Presigned URL 요청 실패");

    const json = await res.json();
    return json.data;
  },

  /** 2. S3 직접 업로드 */
  uploadToS3: async (url: string, file: File) => {
    console.log("Uploading to S3 URL:", url);

    const res = await fetch(url, {
      method: "PUT",
      body: file,
      headers: {
        "Content-Type": file.type, // 반드시 Presign 생성 시 Content-Type 과 동일해야 함
      },
      mode: "cors", // CORS로 강제
    });

    if (!res.ok) {
      const text = await res.text().catch(() => "");
      console.error("S3 Upload failed:", res.status, text);
      throw new Error("S3 업로드 실패");
    }
  },

  /** 3. DB 등록 */
  completeUpload: async (payload: {
    key: string;
    type: string;
    fileSize: number;
    originUrl: string;
    platform: string;
  }) => {
    const res = await apiClient("/files/complete", {
      method: "POST",
      body: JSON.stringify(payload),
      credentials: "include",
    });

    if (!res.ok) throw new Error("파일 메타데이터 등록 실패");

    const json = await res.json();
    return json.data;
  },

  /** 4. 후처리 */
  postProcess: async (fileId: number) => {
    await apiClient(`/files/${fileId}/post-process`, {
      method: "POST",
      credentials: "include",
    });
  },
};
