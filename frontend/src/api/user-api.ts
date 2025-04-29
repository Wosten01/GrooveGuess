import { apiGet } from "./api";

export interface User {
  id: number;
  username: string;
  email: string;
  score: number;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

const BASE = "/users"

export async function getScoreboardUsers(
    page = 0, size = 20, search?: string
): Promise<Page<User>> {
    let url = `${BASE}?page=${page}&size=${size}`;
    if (search && search.length > 0) {
        url += `&search=${encodeURIComponent(search)}`;
    }
    const response = await apiGet<Page<User>>(url);
  return response.data;
}