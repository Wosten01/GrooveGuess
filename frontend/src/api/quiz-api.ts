import { Track } from "./tracks-api";
import { ApiResponse, apiDelete, apiGet, apiPost, apiPut } from "./api";

export interface Quiz {
  id: number;
  title: string;
  description: string;
  roundCount: number;
  creatorId?: number;
  createdAt?: string;
  tracks?: Track[];
}

export interface QuizRequest {
  title: string;
  description: string;
  roundCount: number;
  trackIds?: number[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

const BASE_URL = "/quizzes";

export async function getQuizzes(page = 0, size = 10): Promise<Page<Quiz>> {
  const response = await apiGet<Page<Quiz>>(`${BASE_URL}?page=${page}&size=${size}`);
  return response.data;
}

export async function getQuiz(id: number): Promise<ApiResponse<Quiz>> {
  return apiGet<Quiz>(`${BASE_URL}/${id}`);
}

export async function createQuiz(
  quiz: QuizRequest,
  creatorId: number
): Promise<ApiResponse<Quiz>> {
  return apiPost<Quiz, QuizRequest>(`${BASE_URL}`, quiz, {
    params: { creatorId },
  });
}

export async function updateQuiz(
  id: number,
  quiz: QuizRequest,
  userId: number
): Promise<ApiResponse<Quiz>> {
  return apiPut<Quiz, QuizRequest>(`${BASE_URL}/${id}`, quiz, {
    params: { userId },
  });
}

export async function deleteQuiz(id: number, userId: number): Promise<ApiResponse<void>> {
  const response = await apiDelete<void>(`${BASE_URL}/${id}`, {
    params: { userId },
  });
  return response;
}

export async function addTrackToQuiz(
  quizId: number,
  trackId: number,
  userId: number
): Promise<ApiResponse<Quiz>> {
  return apiPost<Quiz, undefined>(`${BASE_URL}/${quizId}/tracks`, undefined, {
    params: { trackId, userId },
  });
}

export async function addTracksToQuiz(
  quizId: number,
  trackIds: number[],
  userId: number
): Promise<ApiResponse<Quiz>> {
  return apiPost<Quiz, number[]>(`${BASE_URL}/${quizId}/tracks/bulk`, trackIds, {
    params: { userId },
  });
}