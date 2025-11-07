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
