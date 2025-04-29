import React, { useEffect, useState } from "react";
import {
  Box,
  Button,
  Typography,
  Card,
  CardContent,
  Grid,
  Paper,
} from "@mui/material";
import { useTranslation } from "react-i18next";
import { getNextRound, RoundData, submitAnswer } from "../../api/quiz-game-api";
import { useAuth } from "../../hooks/auth-context";
import { useNavigate, useParams } from "react-router-dom";



type QuizGamePlayerProps = {
  quizId: number;
  onFinish: (score: number) => void;
};

export const QuizGameWrapper = () => {
  const { quizId } = useParams<{ quizId: string }>();
  const navigate = useNavigate();
  
  const handleFinish = (score: number) => {
    console.log(`Game finished with score: ${score}`);
    setTimeout(() => {
      navigate('/scoreboard');
    }, 3000);
  };
  
  return (
    <QuizGamePlayer 
      quizId={Number(quizId)} 
      onFinish={handleFinish} 
    />
  );
};

export const QuizGamePlayer: React.FC<QuizGamePlayerProps> = ({ quizId, onFinish }) => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [round, setRound] = useState<RoundData | null>(null);
  const [score, setScore] = useState(0);
  const [selected, setSelected] = useState<number | null>(null);
  const [feedback, setFeedback] = useState<"correct" | "wrong" | null>(null);
  const [gameOver, setGameOver] = useState(false);
  const { user } = useAuth();

  useEffect(() => {
    setLoading(true);
    getNextRound(quizId)
      .then((data) => {
        setRound(data as RoundData);
        setSelected(null);
        setFeedback(null);
      })
      .finally(() => setLoading(false));
  }, [quizId]);
  
  const handleOptionClick = async (optionId: number) => {
    if (selected !== null) return;
    setSelected(optionId);
    setLoading(true);
    const res = await submitAnswer(quizId, round!.roundNumber, optionId, user?.id || -1);
    setFeedback(res.correct ? "correct" : "wrong");
    if (res.correct) setScore((s) => s + res.points);
    setTimeout(() => {
      if (res.isLastRound) {
        setGameOver(true);
        onFinish(score + (res.correct ? res.points : 0));
      } else {
        getNextRound(quizId).then((data) => {
          setRound(data as RoundData);
          setSelected(null);
          setFeedback(null);
        });
      }
      setLoading(false);
    }, 1200);
  };

  if (gameOver) {
    return (
      <Paper sx={{ p: 4, textAlign: "center" }}>
        <Typography variant="h4" color="primary" gutterBottom>
          {t("quizGame.finished", "Игра окончена!")}
        </Typography>
        <Typography variant="h5">
          {t("quizGame.yourScore", "Ваш счёт")}: {score}
        </Typography>
      </Paper>
    );
  }

  return (
    <Box maxWidth={600} mx="auto" mt={4}>
      <Card>
        <CardContent>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            {t("quizGame.round", "Раунд")} {round?.roundNumber} / {round?.totalRounds}
          </Typography>
          <audio controls autoPlay src={round?.audioUrl} style={{ width: "100%" }} />
          <Grid container spacing={2} mt={2}>
            {round?.options.map((option) => (
              <Grid size={{ xs:12, sm:6}} key={option.id}>
                <Button
                  fullWidth
                  variant={
                    selected === null
                      ? "contained"
                      : option.id === selected
                      ? "contained"
                      : "outlined"
                  }
                  color={
                    selected === null
                      ? "primary"
                      : option.id === selected
                      ? feedback === "correct"
                        ? "success"
                        : "error"
                      : "primary"
                  }
                  onClick={() => handleOptionClick(option.id)}
                  disabled={selected !== null}
                  sx={{ minHeight: 56, fontSize: "1.1rem" }}
                >
                  {option.title}
                </Button>
              </Grid>
            ))}
          </Grid>
          <Box mt={3} textAlign="right">
            <Typography variant="subtitle1">
              {t("quizGame.score", "Счёт")}: {score}
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};