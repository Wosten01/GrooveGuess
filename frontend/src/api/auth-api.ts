import { apiGet, apiPost, ApiResponse } from "./api";

export interface RegisterDTO {
    email: string;
    username: string;
    password: string;
  }
  
  export interface LoginDTO {
    email: string;
    password: string;
  }
  
  export interface AuthResponse {
    message: {
      message: string;
    };
  }

  export interface User {
    id?: number;
    username: string;
    email: string;
    role: string;
    score: number; 
  }  

  const authPrefix = "/auth"
  
  export async function registerUser(data: RegisterDTO): Promise<ApiResponse<{ message: string }>> {
    return apiPost<{ message: string }, RegisterDTO>(authPrefix + "/register", data,);
  }
  
  export async function loginUser(data: LoginDTO): Promise<ApiResponse<AuthResponse>> {
    return apiPost<AuthResponse, LoginDTO>(authPrefix + "/login", data);
  }
  
  export async function getCurrentUser(): Promise<ApiResponse<User>> {
    return apiGet<User>(authPrefix + "/me");
  }

  export async function logoutUser(): Promise<ApiResponse<{ message: string }>> {
    return apiPost<{ message: string }, void>(authPrefix + "/logout");
  }
  