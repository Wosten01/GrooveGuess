import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError, InternalAxiosRequestConfig } from "axios";
import Cookies from 'js-cookie'; 

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const JWT_COOKIE_NAME = "jwt";

const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

function getJwtToken(): string | null {
  try {
    return Cookies.get(JWT_COOKIE_NAME) || null;
  } catch (error) {
    console.error("Error getting JWT token from cookie:", error);
    return null;
  }
}

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    try {
      const token = getJwtToken();

      if (token) {
        config.headers.set('Authorization', `Bearer ${token}`);
      } else {
        console.log("No JWT token available for request:", config.url);
      }
    } catch (error) {
      console.error("Error in request interceptor:", error);
    }
    
    return config;
  },
  (error) => {
    console.error("Request interceptor error:", error);
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      console.warn("Authentication error (401), removing token");
      removeAuthToken();
      
    }
    
    return Promise.reject(error);
  }
);

export interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

interface ApiErrorResponse {
  status: number;
  message: string;
  data: null;
}

const handleApiError = (error: unknown): never => {
  console.error("API Error:", error);
  
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{message?: string}>;
    const errorResponse: ApiErrorResponse = {
      status: axiosError.response?.status || 500,
      message: axiosError.response?.data?.message || axiosError.message || "Unknown error occurred",
      data: null
    };
    throw errorResponse;
  }
  
  const genericError: ApiErrorResponse = {
    status: 500,
    message: error instanceof Error ? error.message : "Unknown error occurred",
    data: null
  };
  throw genericError;
};

function createConfigWithAuth(config?: AxiosRequestConfig): AxiosRequestConfig {
  const token = getJwtToken();
  const newConfig: AxiosRequestConfig = { ...config };
  
  if (!newConfig.headers) {
    newConfig.headers = {};
  }
  
  if (token) {
    newConfig.headers = {
      ...newConfig.headers,
      Authorization: `Bearer ${token}`
    };
  }
  
  return newConfig;
}

export async function apiGet<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  try {
    const authConfig = createConfigWithAuth(config);
    const response: AxiosResponse<T> = await apiClient.get(url, authConfig);
    return { data: response.data, status: response.status };
  } catch (error) {
    return handleApiError(error);
  }
}

export async function apiPost<T, D = unknown>(
  url: string,
  data?: D,
  config?: AxiosRequestConfig
): Promise<ApiResponse<T>> {
  try {
    const authConfig = createConfigWithAuth(config);
    const response: AxiosResponse<T> = await apiClient.post(url, data, authConfig);
    return { data: response.data, status: response.status };
  } catch (error) {
    return handleApiError(error);
  }
}

export async function apiPut<T, D = unknown>(
  url: string,
  data?: D,
  config?: AxiosRequestConfig
): Promise<ApiResponse<T>> {
  try {
    const authConfig = createConfigWithAuth(config);
    const response: AxiosResponse<T> = await apiClient.put(url, data, authConfig);
    return { data: response.data, status: response.status };
  } catch (error) {
    return handleApiError(error);
  }
}

export async function apiDelete<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  try {
    const authConfig = createConfigWithAuth(config);
    const response: AxiosResponse<T> = await apiClient.delete(url, authConfig);
    return { data: response.data, status: response.status };
  } catch (error) {
    return handleApiError(error);
  }
}

export function setAuthToken(token: string, expiresInDays: number = 7): void {
  try {
    Cookies.set(JWT_COOKIE_NAME, token, { 
      expires: expiresInDays, 
      path: '/', 
      secure: window.location.protocol === 'https:',
      sameSite: 'strict'
    });
    console.log("Auth token set successfully");
  } catch (error) {
    console.error("Error setting auth token:", error);
  }
}

export function removeAuthToken(): void {
  try {
    Cookies.remove(JWT_COOKIE_NAME, { path: '/' });
    console.log("Auth token removed successfully");
  } catch (error) {
    console.error("Error removing auth token:", error);
  }
}

export function hasAuthToken(): boolean {
  try {
    return !!getJwtToken();
  } catch (error) {
    console.error("Error checking auth token:", error);
    return false;
  }
}

export function refreshAuthToken(newToken: string, expiresInDays: number = 7): void {
  removeAuthToken();
  setAuthToken(newToken, expiresInDays);
}