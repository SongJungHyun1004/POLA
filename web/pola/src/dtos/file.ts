export interface FileListRequest {
  page: number;
  size: number;
  sortBy: "createdAt" | "views";
  direction: "ASC" | "DESC";
  filterType: "category" | "favorite" | "";
  filterId?: number;
}

export interface FileListResponse {
  content: any[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface FileResult {
  fileId: number;
  userId: number;
  categoryName: string;
  fileType: string;
  platform?: string;
  tags: string;
  context: string;
  ocrText: string;
  imageUrl: string;
  createdAt: string;
  favorite: boolean;
}
