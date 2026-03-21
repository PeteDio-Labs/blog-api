export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}

export function paginate<T>(
  data: T[],
  page: number,
  size: number,
  totalElements: number,
): PaginatedResponse<T> {
  return {
    data,
    pagination: {
      page,
      size,
      totalElements,
      totalPages: Math.ceil(totalElements / size),
    },
  };
}

export function parsePagination(query: {
  page?: string;
  size?: string;
}): { page: number; size: number; offset: number } {
  // UI uses 1-based indexing, API uses 0-based for offset
  const page = Math.max(1, Number(query.page) || 1);
  const size = Math.min(100, Math.max(1, Number(query.size) || 20));
  const offset = (page - 1) * size;
  return { page, size, offset };
}
