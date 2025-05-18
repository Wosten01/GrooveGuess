import { apiGet } from './api';

export interface RecentGameDto {
  sessionId: string;
  userId: number;
  username: string;
  quizId: number;
  quizTitle: string;
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

export const exportPlayerStats = async (
  userId: number,
  format: 'json' | 'csv' = 'json',
  gamesLimit?: number
): Promise<void> => {
  try {
    const { apiClient, getJwtToken } = await import('./api');
    
    const params: Record<string, any> = { 
      userId, 
      format 
    };
    
    if (gamesLimit) {
      params.gamesLimit = gamesLimit;
    }
    
    const token = getJwtToken();
    
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    const response = await apiClient.get(`${BASE_URL}/export`, {
      params,
      headers,
      responseType: 'blob'
    });
    
    const downloadUrl = window.URL.createObjectURL(new Blob([response.data]));

    const link = document.createElement('a');
    link.href = downloadUrl;
    
    const contentDisposition = response.headers['content-disposition'];
    let filename = `player_stats.${format}`;
    
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="(.+)"/);
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1];
      }
    }
    
    link.setAttribute('download', filename);
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    window.URL.revokeObjectURL(downloadUrl);
  } catch (error) {
    console.error('Error downloading stats:', error);
    throw error;
  }
};