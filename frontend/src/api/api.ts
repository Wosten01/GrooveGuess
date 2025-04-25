
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";

// You can set your backend base URL here or use an environment variable
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true
});

export interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

// Generic GET
export async function apiGet<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  const response: AxiosResponse<T> = await apiClient.get(url, config);
  return { data: response.data, status: response.status };
}

// Generic POST
export async function apiPost<T, D = unknown>(
  url: string,
  data?: D,
  config?: AxiosRequestConfig
): Promise<ApiResponse<T>> {
  const response: AxiosResponse<T> = await apiClient.post(url, data, config);
  return { data: response.data, status: response.status };
}

// Generic PUT
export async function apiPut<T, D = unknown>(
  url: string,
  data?: D,
  config?: AxiosRequestConfig
): Promise<ApiResponse<T>> {
  const response: AxiosResponse<T> = await apiClient.put(url, data, config);
  return { data: response.data, status: response.status };
}

// Generic DELETE
export async function apiDelete<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  const response: AxiosResponse<T> = await apiClient.delete(url, config);
  return { data: response.data, status: response.status };
}
