import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Paper,
  Grid,
  Alert,
} from "@mui/material";
import axios from "axios";
import { getCurrentRound, GameSessionDto, submitAnswer, getNextRound } from "../../api/quiz-game-api";
import { useAuth } from "../../hooks/auth-context";

interface AnswerResult {
  correct: boolean;
  points: number;
  isLastRound: boolean;
  finalScore?: number;
}

export const Game: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [gameSession, setGameSession] = useState<GameSessionDto | null>(null);
  const [selectedOption, setSelectedOption] = useState<{ id: number, title: string } | null>(null);
  const [audioElement, setAudioElement] = useState<HTMLAudioElement | null>(null);
  const [audioReady, setAudioReady] = useState<boolean>(false);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [answerResult, setAnswerResult] = useState<AnswerResult | null>(null);
  const [showResult, setShowResult] = useState<boolean>(false);
  const [authChecked, setAuthChecked] = useState<boolean>(false);
  const [currentScore, setCurrentScore] = useState<number>(0);
  const [totalScore, setTotalScore] = useState<number>(0);

  useEffect(() => {
    if (user !== undefined) {
      setAuthChecked(true);
    }
  }, [user]);

  const fetchCurrentRound = useCallback(async () => {
    if (!sessionId || !authChecked || !user) return;
    
    const userId = user.id;
    
    try {
      setLoading(true);
      setError(null);
      setSelectedOption(null);
      setAnswerResult(null);
      setShowResult(false);
      setAudioReady(false);
      
      if (audioElement) {
        audioElement.pause();
        audioElement.removeEventListener("canplaythrough", () => {});
      }

      const response = await getCurrentRound(sessionId, userId);
      setGameSession(response.data);

      setCurrentScore(response.data.score || 0);
      setTotalScore(response.data.score || 0);
      
      if (response.data.currentRound?.url) {
        const audio = new Audio(response.data.currentRound.url);
        
        audio.addEventListener("canplaythrough", () => {
          setAudioReady(true);
        });
        
        setAudioElement(audio);
      }
      
      setLoading(false);
    } catch (err) {
      console.error("Error fetching current round:", err);
      
      // Check if the error is a 409 Conflict (Game is already completed)
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        console.log("Game is already completed, redirecting to results page");
        
        // Set the game as completed
        setGameSession(prev => prev ? { ...prev, completed: true } : null);
        
        // Optionally show a message before redirecting
        setError("This game session is already completed. Redirecting to results...");
        
        // Redirect to results page after a short delay
        setTimeout(() => {
          navigate(`/game/player/${userId}/session/${sessionId}/results`);
        }, 1500);
      } else {
        setError("Failed to load the round. Please try again.");
      }
      
      setLoading(false);
    }
  },[audioElement, authChecked, sessionId, user, navigate])

  useEffect(() => {
    if (sessionId && authChecked && user) {
      fetchCurrentRound();
    }

    return () => {
      if (audioElement) {
        audioElement.pause();
      }
    };
  }, [sessionId, authChecked, user]);

  const handleSubmitAnswer = async () => {
    if (!gameSession || !selectedOption || !sessionId || !user) return;
    
    const userId = user.id;
    
    try {
      setSubmitting(true);
      
      const result = await submitAnswer(
        sessionId,
        userId!,
        gameSession.currentRoundNumber,
        selectedOption.id
      );
      
      setAnswerResult(result);
      setShowResult(true);

      if (result.correct) {
        setCurrentScore(result.points);
        setTotalScore(prevScore => prevScore + result.points);
      } else {
        setCurrentScore(0);
      }

      if (result.isLastRound) {
        if (result.finalScore !== undefined) {
          setTotalScore(result.finalScore);
          setCurrentScore(0);
        }

        setTimeout(() => {
          navigate(`/game/player/${userId}/session/${sessionId}/results`);
        }, 3000);
      } else {
        setTimeout(() => {
          fetchNextRound();
        }, 2000);
      }
      
      setSubmitting(false);
    } catch (err) {
      console.error("Error submitting answer:", err);
      
      // Check if the error is a 409 Conflict (Game is already completed)
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        console.log("Game is already completed, redirecting to results page");
        
        // Set the game as completed
        setGameSession(prev => prev ? { ...prev, completed: true } : null);
        
        // Redirect to results page after a short delay
        setTimeout(() => {
          navigate(`/game/player/${userId}/session/${sessionId}/results`);
        }, 1500);
      } else {
        setError("Failed to submit your answer. Please try again.");
      }
      
      setSubmitting(false);
    }
  };

  const fetchNextRound = async () => {
    if (!user || !sessionId) return;
    
    const userId = user.id;
    
    try {
      setLoading(true);
      
      await getNextRound(sessionId, userId);
      
      fetchCurrentRound();
    } catch (err) {
      console.error("Error fetching next round:", err);
      
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        console.log("Game is already completed, redirecting to results page");
        
        setGameSession(prev => prev ? { ...prev, completed: true } : null);
        
        setTimeout(() => {
          navigate(`/game/player/${userId}/session/${sessionId}/results`);
        }, 1500);
      } else {
        setError("Failed to load the next round. Please try again.");
      }
      
      setLoading(false);
    }
  };

  const playAudio = () => {
    if (audioElement && audioReady) {
      audioElement.currentTime = 0;
      
      const playPromise = audioElement.play();
      
      if (playPromise !== undefined) {
        playPromise.catch((error) => {
          console.error("Audio playback failed:", error);
        });
      }
    }
  };

  if (!authChecked) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <CircularProgress />
        <Typography variant="body1" sx={{ ml: 2 }}>
          Checking authentication...
        </Typography>
      </Box>
    );
  }

  if (authChecked && !user) {
    return (
      <Box display="flex" flexDirection="column" justifyContent="center" alignItems="center" minHeight="80vh">
        <Alert severity="warning" sx={{ mb: 2 }}>
          You need to be logged in to play the game.
        </Alert>
        <Button 
          variant="contained" 
          color="primary"
          onClick={() => navigate("/login", { state: { from: window.location.pathname } })}
        >
          Log In
        </Button>
      </Box>
    );
  }

  if (loading && !gameSession) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <Typography color="error" variant="h6">{error}</Typography>
        {error.includes("already completed") && <CircularProgress size={24} sx={{ ml: 2 }} />}
      </Box>
    );
  }

  if (gameSession?.completed) {
    return (
      <Box display="flex" flexDirection="column" justifyContent="center" alignItems="center" minHeight="80vh">
        <Typography variant="h5" gutterBottom>This game session is already completed.</Typography>
        <Button 
          variant="contained" 
          color="primary"
          onClick={() => navigate(`/game/player/${user?.id}/session/${sessionId}/results`)}
          sx={{ mt: 2 }}
        >
          View Results
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 800, mx: "auto", p: 3 }}>
      {gameSession && gameSession.currentRound && (
        <Paper elevation={3} sx={{ p: 3 }}>
          <Typography variant="h4" gutterBottom>
            Round {gameSession.currentRoundNumber + 1} of {gameSession.totalRounds}
          </Typography>
          
          {showResult && answerResult && (
            <Alert 
              severity={answerResult.correct ? "success" : "error"}
              sx={{ mb: 3 }}
            >
              {answerResult.correct 
                ? `Correct! You earned ${answerResult.points} points.` 
                : "Wrong answer! Better luck on the next one."}
              {answerResult.isLastRound && " This was the last round!"}
            </Alert>
          )}
          
          <Box sx={{ mb: 4 }}>
            <Typography variant="h6" gutterBottom>
              Listen to the track and guess the title:
            </Typography>
            <Button
              variant="contained"
              onClick={playAudio}
              disabled={!audioReady || showResult}
              sx={{ mt: 1 }}
            >
              {!audioReady ? "Loading Audio..." : "Play Track"}
            </Button>
          </Box>
          
          <Typography variant="h6" gutterBottom>
            Select your answer:
          </Typography>
          
          <Grid container spacing={2}>
            {gameSession.currentRound.options && gameSession.currentRound.options.map((option) => (
              <Grid  size={{xs:12, sm:6}} key={option.id}>
                <Button
                  variant={selectedOption?.id === option.id ? "contained" : "outlined"}
                  color={
                    showResult && answerResult
                      ? option.id === selectedOption?.id
                        ? answerResult.correct ? "success" : "error"
                        : "primary"
                      : "primary"
                  }
                  fullWidth
                  onClick={() => !showResult && setSelectedOption(option)}
                  disabled={showResult || submitting}
                  sx={{ py: 1.5 }}
                >
                  {option.title} - {option.artist}
                </Button>
              </Grid>
            ))}
          </Grid>
          
          {!showResult ? (
            <Box sx={{ mt: 4, textAlign: "center" }}>
              {selectedOption && (
                <Typography variant="body1" gutterBottom>
                  You selected: <strong>{selectedOption.title}</strong>
                </Typography>
              )}
              <Button
                variant="contained"
                color="primary"
                size="large"
                sx={{ mt: 2 }}
                onClick={handleSubmitAnswer}
                disabled={!selectedOption || submitting}
              >
                {submitting ? <CircularProgress size={24} /> : "Submit Answer"}
              </Button>
            </Box>
          ) : (
            <Box sx={{ mt: 4, textAlign: "center" }}>
              {!answerResult?.isLastRound && (
                <Typography variant="body1" gutterBottom>
                  Loading next round...
                </Typography>
              )}
              {answerResult?.isLastRound && (
                <Typography variant="body1" gutterBottom>
                  Redirecting to results page...
                </Typography>
              )}
              <CircularProgress size={24} />
            </Box>
          )}
          
          {/* Display current score */}
          <Box sx={{ mt: 4, display: "flex", justifyContent: "space-between" }}>
            <Typography variant="h6">
              Round Score: {currentScore}
            </Typography>
            <Typography variant="h6">
              Total Score: {totalScore}
            </Typography>
          </Box>
        </Paper>
      )}
    </Box>
  );
};

export default Game;