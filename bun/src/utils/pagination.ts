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
  const page = Math.max(0, Number(query.page) || 0);
  const size = Math.min(100, Math.max(1, Number(query.size) || 20));
  return { page, size, offset: page * size };
}
