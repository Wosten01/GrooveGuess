import { apiGet, apiPost, ApiResponse } from "./api";

type TrackDtoOption = {
  id: number;
  title: string;
  artist: string;
};

export type GameSessionDto = {
  sessionId: string;
  totalRounds: number;
  currentRoundNumber: number;
  score: number;
  completed: boolean;
  currentRound?: RoundData;
};

export type RoundData = {
  currentRound: number,
  url: string;
  options: TrackDtoOption[];
};

export type AnswerDto = {
  roundNumber: number;
  optionId: number;
};

export type AnswerResultDto = {
  correct: boolean;
  points: number;
  isLastRound: boolean;
  finalScore?: number;
};

export type TrackOptionDto = {
  id: number;
  title: string;
  artist: string;
};


export type TrackResultDto = {
  roundNumber: number;
  trackId: number;
  title: string;
  artist: string;
  url: string;
  wasGuessed: boolean;
  options: TrackOptionDto[];
};

export type GameResultsDto = {
  quizId: number;
  totalRounds: number;
  score: number;
  tracks: TrackResultDto[];
};


const BASE_URL = "/quiz-game";

export async function startGame(quizId: number, userId: number = 1): Promise<GameSessionDto> {
  const res = await apiPost<GameSessionDto>(`${BASE_URL}/${quizId}/start`, null, {
    params: { userId },
  });
  return res.data;
}

export async function getNextRound(sessionId: string, userId: number = 1): Promise<RoundData> {
  const res = await apiGet<RoundData>(`${BASE_URL}/player/${userId}/session/${sessionId}/next-round`);
  return res.data;
}

export async function getCurrentRound(sessionId: string, userId: number = 1): Promise<ApiResponse<GameSessionDto>> {
  const res = await apiGet<GameSessionDto>(`${BASE_URL}/player/${userId}/session/${sessionId}/current-round`);
  return res;
}

export async function submitAnswer(
  sessionId: string, 
  userId: number, 
  roundNumber: number, 
  optionId: number
): Promise<AnswerResultDto> {
  const answer: AnswerDto = {
    roundNumber,
    optionId,
  };
  
  const res = await apiPost<AnswerResultDto>(
    `${BASE_URL}/player/${userId}/session/${sessionId}/answer`,
    answer
  );
  return res.data;
}

export async function getGameResults(sessionId: string, userId: number = 1): Promise<GameResultsDto> {
  const res = await apiGet<GameResultsDto>(`${BASE_URL}/player/${userId}/session/${sessionId}/results`);
  return res.data;
}