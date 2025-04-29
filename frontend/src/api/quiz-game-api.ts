import axios from "axios";
import { apiGet, apiPost } from "./api";

type Option = {
  id: number;
  title: string;
};


export type RoundData = {
  roundNumber: number;
  totalRounds: number;
  audioUrl: string;
  options: Option[];
  correctOptionId?: number; 
};

export type AnswerResultDto = {
  correct: boolean,
  points: number,
  isLastRound: boolean,
}

export async function getNextRound(quizId: number) {
  const res = await apiGet<RoundData>(`/api/quiz-game/${quizId}/next-round`, {
    params: { userId: 1 }, 
  });
  return res.data;
}

export async function submitAnswer(quizId: number, roundNumber: number, optionId: number, userId: number) {
  const res = await apiPost<AnswerResultDto>(`/api/quiz-game/${quizId}/answer?userId=${userId}`, {
    roundNumber,
    optionId,
  });
  return res.data;
}

export async function startGame(quizId: number) {
  const res = await axios.post(`/api/quiz-game/${quizId}/start`, null, {
    params: { userId: 1 },
  });
  return res.data;
}