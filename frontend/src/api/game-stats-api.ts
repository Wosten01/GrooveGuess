import { apiGet } from './api';

export interface RecentGameDto {
  sessionId: string;
  userId: number;
  username: string;
  quizId: number;
  score: number;
  totalRounds: number;
  correctAnswers: number;
  completed: boolean;
  timestamp: number;
}

export interface GameStatsDto {
  totalGames: number;
  totalScore: number;
  averageScore: number;
  highestScore: number;
  accuracy: number;
}

export interface PaginatedStatsResponseDto {
  games: RecentGameDto[];
  totalGames: number;
  totalPages: number;
  currentPage: number;
  stats: GameStatsDto | null;
}

const BASE_URL = "/game-stats";

export const getRecentGames = async (
  page: number = 0, 
  size: number = 10, 
  userId?: number
): Promise<PaginatedStatsResponseDto> => {
  const params: Record<string, any> = { page, size };
  if (userId) {
    params.userId = userId;
  }
  
  const response = await apiGet<PaginatedStatsResponseDto>(`${BASE_URL}/recent`, { params });
  return response.data;
};


export const getGlobalStats = async (): Promise<GameStatsDto> => {
  const response = await apiGet<GameStatsDto>(`${BASE_URL}/global`);
  return response.data;
};