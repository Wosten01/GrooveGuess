import React, { useState, useEffect, useCallback, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  CircularProgress,
  Grid,
  Alert,
  Fade,
  Grow,
  Slide,
  LinearProgress,
  Snackbar,
  Button,
} from "@mui/material";
import VolumeUpIcon from "@mui/icons-material/VolumeUp";
import VolumeOffIcon from "@mui/icons-material/VolumeOff";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import CancelIcon from "@mui/icons-material/Cancel";
import PlayArrowIcon from "@mui/icons-material/PlayArrow";
import axios from "axios";
import {
  getCurrentRound,
  GameSessionDto,
  submitAnswer,
  getNextRound,
} from "../../api/quiz-game-api";
import { useAuth } from "../../hooks/auth-context";
import {
  AudioControlButton,
  OptionButton,
  ScoreDisplay,
  StyledPaper,
} from "./GameStyledComponents";

interface AnswerResult {
  correct: boolean;
  points: number;
  isLastRound: boolean;
  finalScore?: number;
}

const TIME = 5;

export const Game: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const audioRef = useRef<HTMLAudioElement | null>(null);

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [gameSession, setGameSession] = useState<GameSessionDto | null>(null);
  const [selectedOption, setSelectedOption] = useState<{
    id: number;
    title: string;
  } | null>(null);
  const [audioReady, setAudioReady] = useState<boolean>(false);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [answerResult, setAnswerResult] = useState<AnswerResult | null>(null);
  const [showResult, setShowResult] = useState<boolean>(false);
  const [authChecked, setAuthChecked] = useState<boolean>(false);
  const [currentScore, setCurrentScore] = useState<number>(0);
  const [totalScore, setTotalScore] = useState<number>(0);
  const [muted, setMuted] = useState<boolean>(false);
  const [snackbarOpen, setSnackbarOpen] = useState<boolean>(false);
  const [snackbarMessage, setSnackbarMessage] = useState<string>("");
  const [timeLeft, setTimeLeft] = useState<number>(TIME);
  const [maxTime, setMaxTime] = useState<number>(TIME);
  const [showPlayButton, setShowPlayButton] = useState<boolean>(false);
  const [correctOptionId, setCorrectOptionId] = useState<number | null>(null);

  useEffect(() => {
    if (user !== undefined) {
      setAuthChecked(true);
    }
  }, [user]);

  const playAudio = useCallback(() => {
    if (audioRef.current) {
      audioRef.current
        .play()
        .then(() => {
          setShowPlayButton(false);
        })
        .catch((error) => {
          console.error("Audio playback failed:", error);
          setShowPlayButton(true);
          setSnackbarMessage(
            "Couldn't play audio automatically. Click the play button to start."
          );
          setSnackbarOpen(true);
        });
    }
  }, []);

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
      setShowPlayButton(false);
      setCorrectOptionId(null);

      if (audioRef.current) {
        audioRef.current.pause();
      }

      const response = await getCurrentRound(sessionId, userId);

      setGameSession(response.data);

      setCurrentScore(response.data.score || 0);
      setTotalScore(response.data.score || 0);

      const roundTimeLimit = TIME;
      setTimeLeft(roundTimeLimit);
      setMaxTime(roundTimeLimit);

      if (response.data.currentRound?.url) {
        if (audioRef.current) {
          audioRef.current.src = response.data.currentRound.url;
          audioRef.current.load();
          audioRef.current.addEventListener("canplaythrough", () => {
            setAudioReady(true);
            playAudio();
          });
        } else {
          audioRef.current = new Audio(response.data.currentRound.url);
          audioRef.current.addEventListener("canplaythrough", () => {
            setAudioReady(true);
            playAudio();
          });
        }
      }

      setLoading(false);
    } catch (err) {
      console.error("Error fetching current round:", err);

      if (axios.isAxiosError(err) && err.response?.status === 409) {
        console.log("Game is already completed, redirecting to results page");

        setGameSession((prev) => (prev ? { ...prev, completed: true } : null));

        setError(
          "This game session is already completed. Redirecting to results..."
        );

      } else {
        setError("Failed to load the round. Please try again.");
      }

      setLoading(false);
    }
  }, [authChecked, sessionId, user, playAudio]);

  const fetchNextRound = useCallback(async () => {
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
        setGameSession((prev) => (prev ? { ...prev, completed: true } : null));

      } else {
        setError("Failed to load the next round. Please try again.");
      }

      setLoading(false);
    }
  }, [user, sessionId, fetchCurrentRound]);

  const handleOptionSelect = useCallback(
    async (option: { id: number; title: string }) => {
      if (showResult || !gameSession || !sessionId || !user) return;
  
      setSelectedOption(option);
      setSubmitting(true);
  
      const userId = user.id;
  
      try {
        const result = await submitAnswer(
          sessionId,
          userId!,
          gameSession.currentRoundNumber,
          option.id
        );
  
        setAnswerResult(result);
        setShowResult(true);
  
        if (!result.correct && gameSession.currentRound?.options) {
       
          const correctOption = gameSession.currentRound.options.find(
            opt => opt.id !== option.id
          );
          if (correctOption) {
            setCorrectOptionId(correctOption.id);
          }
        } else {
          setCorrectOptionId(option.id);
        }
  
        if (result.correct) {
          setCurrentScore(result.points);
          setTotalScore((prevScore) => prevScore + result.points);
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
          }, 2000);
        } else {
          setTimeout(() => {
            fetchNextRound();
          }, 2000);
        }
  
        setSubmitting(false);
      } catch (err) {
        console.error("Error submitting answer:", err);
  
        if (axios.isAxiosError(err) && err.response?.status === 409) {
          console.log("Game is already completed, redirecting to results page");
          
          setGameSession((prev) => (prev ? { ...prev, completed: true } : null));
          
          setTimeout(() => {
            navigate(`/game/player/${userId}/session/${sessionId}/results`);
          }, 1500);
        } else {
          setError("Failed to submit your answer. Please try again.");
        }
  
        setSubmitting(false);
      }
    },
    [gameSession, sessionId, showResult, user, fetchNextRound, navigate]
  );

  useEffect(() => {
    if (!gameSession || !sessionId || !user || showResult || timeLeft <= 0) return;

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          if (!showResult && selectedOption) {
            handleOptionSelect(selectedOption);
          } else if (!showResult) {
            const isLastRound = gameSession.currentRoundNumber === gameSession.totalRounds - 1;
            
            if (isLastRound) {
              setSnackbarMessage(
                "Time's up! This was the last round. Redirecting to results..."
              );
              setSnackbarOpen(true);

              setGameSession((prev) =>
                prev ? { ...prev, completed: true } : null
              );
              
              
              (async () => {
                try {
                  await submitAnswer(
                    sessionId,
                    user.id!,
                    gameSession.currentRoundNumber,
                    -1
                  );
                  
                  setTimeout(() => {
                    navigate(`/game/player/${user.id}/session/${sessionId}/results`);
                  }, 2000);
                } catch (error) {
                  console.error("Error submitting timeout answer:", error);
                  setError("Failed to submit your answer. Please try again.");
                }
              })();
            } else {
              setSnackbarMessage("Time's up! Moving to next round...");
              setSnackbarOpen(true);
              
              (async () => {
                try {
                  await submitAnswer(
                    sessionId,
                    user.id!,
                    gameSession.currentRoundNumber,
                    -1
                  );
                  
                  setTimeout(() => {
                    fetchNextRound();
                  }, 1500);
                } catch (error) {
                  console.error("Error submitting timeout answer:", error);
                  setError("Failed to submit your answer. Please try again.");
                }
              })();
            }
          }
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [gameSession, navigate, sessionId, showResult, selectedOption, timeLeft, user, fetchNextRound, handleOptionSelect]);

  useEffect(() => {
    if (sessionId && authChecked && user) {
      fetchCurrentRound();
    }

    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
    };
  }, [sessionId, authChecked, user, fetchCurrentRound]);

  const toggleMute = () => {
    if (audioRef.current) {
      const newMutedState = !muted;
      setMuted(newMutedState);
      audioRef.current.muted = newMutedState;
    }
  };

  const handleSnackbarClose = () => {
    setSnackbarOpen(false);
  };

  const getOptionBackgroundColor = (optionId: number) => {
    if (!showResult) {
      return selectedOption?.id === optionId ? "primary.main" : "transparent";
    }
    
    if (optionId === correctOptionId) {
      return "success.main"; 
    }
    
    if (selectedOption?.id === optionId && optionId !== correctOptionId) {
      return "error.main";
    }
    
    return "transparent";
  };

 
  const getOptionTextColor = (optionId: number) => {
    if (!showResult) {
      return selectedOption?.id === optionId ? "white" : "text.primary";
    }
    
    if (optionId === correctOptionId || (selectedOption?.id === optionId && optionId !== correctOptionId)) {
      return "white"; 
    }
    
    return "text.primary";
  };

  if (!authChecked) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="80vh"
      >
        <CircularProgress />
        <Typography variant="body1" sx={{ ml: 2 }}>
          Checking authentication...
        </Typography>
      </Box>
    );
  }

  if (authChecked && !user) {
    return (
      <Fade in={true} timeout={800}>
        <Box
          display="flex"
          flexDirection="column"
          justifyContent="center"
          alignItems="center"
          minHeight="80vh"
        >
          <Alert severity="warning" sx={{ mb: 2 }}>
            You need to be logged in to play the game.
          </Alert>
          <Box
            component="button"
            onClick={() =>
              navigate("/login", { state: { from: window.location.pathname } })
            }
            sx={{
              mt: 2,
              py: 1,
              px: 3,
              backgroundColor: "primary.main",
              color: "white",
              border: "none",
              borderRadius: 2,
              cursor: "pointer",
              fontSize: "1rem",
              "&:hover": {
                backgroundColor: "primary.dark",
              },
            }}
          >
            Log In
          </Box>
        </Box>
      </Fade>
    );
  }

  if (loading && !gameSession) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        justifyContent="center"
        alignItems="center"
        minHeight="80vh"
      >
        <CircularProgress />
        <Typography variant="body1" sx={{ mt: 2 }}>
          Loading game...
        </Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Fade in={true} timeout={800}>
        <Box
          display="flex"
          justifyContent="center"
          alignItems="center"
          minHeight="80vh"
        >
          <Typography color="error" variant="h6">
            {error}
          </Typography>
          {error.includes("already completed") && (
            <CircularProgress size={24} sx={{ ml: 2 }} />
          )}
        </Box>
      </Fade>
    );
  }

  return (
    <Box
      sx={{
        maxWidth: 800,
        mx: "auto",
        p: 3,
        minHeight: "80vh",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
      }}
    >
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={6000}
        onClose={handleSnackbarClose}
        message={snackbarMessage}
      />

      {gameSession && gameSession.currentRound && (
        <Fade in={true} timeout={500}>
          <StyledPaper elevation={3}>
            <Box position="relative">
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  mb: 2,
                }}
              >
                <AudioControlButton onClick={toggleMute} size="large">
                  {muted ? <VolumeOffIcon /> : <VolumeUpIcon />}
                </AudioControlButton>
              </Box>

              <Slide direction="down" in={true} timeout={700}>
                <Box>
                  <Typography variant="h4" gutterBottom align="center">
                    Round {gameSession.currentRoundNumber + 1} of{" "}
                    {gameSession.totalRounds}
                  </Typography>

                  <Box sx={{ mb: 1 }}>
                    <Typography
                      variant="body2"
                      color="textSecondary"
                      align="center"
                    >
                      Time remaining: {timeLeft} seconds
                    </Typography>
                    <LinearProgress
                      variant="determinate"
                      value={(timeLeft / maxTime) * 100}
                      sx={{
                        mt: 1,
                        mb: 3,
                        height: 8,
                        borderRadius: 4,
                        background: "rgba(0,0,0,0.05)",
                        "& .MuiLinearProgress-bar": {
                          borderRadius: 4,
                        },
                      }}
                    />
                    {showPlayButton && (
                      <Box className="flex justify-center">
                        <Button
                          variant="contained"
                          color="primary"
                          startIcon={<PlayArrowIcon />}
                          onClick={playAudio}
                          sx={{
                            borderRadius: 20,
                            px: 3,
                            py: 1,
                            animation: "pulse 1.5s infinite",
                            "@keyframes pulse": {
                              "0%": {
                                boxShadow: "0 0 0 0 rgba(25, 118, 210, 0.4)",
                              },
                              "70%": {
                                boxShadow: "0 0 0 10px rgba(25, 118, 210, 0)",
                              },
                              "100%": {
                                boxShadow: "0 0 0 0 rgba(25, 118, 210, 0)",
                              },
                            },
                          }}
                        >
                          Play Track
                        </Button>
                      </Box>
                    )}
                  </Box>
                </Box>
              </Slide>

              {showResult && answerResult && (
                <Grow in={true} timeout={500}>
                  <Alert
                    severity={answerResult.correct ? "success" : "error"}
                    sx={{
                      mb: 3,
                      borderRadius: 2,
                      display: "flex",
                      alignItems: "center",
                    }}
                    icon={
                      answerResult.correct ? (
                        <CheckCircleIcon fontSize="large" />
                      ) : (
                        <CancelIcon fontSize="large" />
                      )
                    }
                  >
                    <Box>
                      <Typography variant="h6">
                        {answerResult.correct
                          ? `Correct! +${answerResult.points} points`
                          : "Wrong answer!"}
                      </Typography>
                      <Typography variant="body2">
                        {answerResult.isLastRound
                          ? "This was the last round! Redirecting to results..."
                          : "Next round loading..."}
                      </Typography>
                    </Box>
                  </Alert>
                </Grow>
              )}

              <Box sx={{ mb: 4, textAlign: "center" }}>
                <Typography variant="h6" gutterBottom>
                  Listen to the track and guess the title:
                </Typography>

                {!audioReady && !showPlayButton && (
                  <Box
                    sx={{ display: "flex", justifyContent: "center", my: 2 }}
                  >
                    <CircularProgress size={24} />
                    <Typography variant="body2" sx={{ ml: 1 }}>
                      Loading audio...
                    </Typography>
                  </Box>
                )}
              </Box>

              <Typography variant="h6" gutterBottom align="center">
                Select your answer:
              </Typography>

              <Grid container spacing={2}>
                {gameSession.currentRound.options &&
                  gameSession.currentRound.options.map((option, index) => (
                    <Grid size={{ xs: 12, sm: 6 }} key={option.id}>
                      <OptionButton
                        whileHover={{ scale: 1.02 }}
                        whileTap={{ scale: 0.98 }}
                        animate={{
                          opacity: [0, 1],
                          y: [20, 0],
                        }}
                        transition={{
                          delay: index * 0.1,
                          duration: 0.3,
                        }}
                        onClick={() =>
                          !showResult &&
                          !submitting &&
                          handleOptionSelect(option)
                        }
                      >
                        <Box
                          sx={{
                            py: 2,
                            px: 2,
                            borderRadius: 3,
                            textAlign: "center",
                            backgroundColor: getOptionBackgroundColor(option.id),
                            color: getOptionTextColor(option.id),
                            border: "1px solid",
                            borderColor: showResult
                              ? option.id === correctOptionId
                                ? "success.main"
                                : option.id === selectedOption?.id
                                ? "error.main"
                                : "primary.main"
                              : "primary.main",
                            boxShadow: selectedOption?.id === option.id ? 3 : 0,
                            cursor:
                              showResult || submitting ? "default" : "pointer",
                            opacity: showResult || submitting ? 0.9 : 1,
                            transition: "all 0.2s ease",
                            "&:hover": {
                              backgroundColor:
                                !showResult && !submitting
                                  ? selectedOption?.id === option.id
                                    ? "primary.dark"
                                    : "rgba(0, 0, 0, 0.04)"
                                  : undefined,
                            },
                          }}
                        >
                          <Typography
                            sx={{
                              fontSize: "1rem",
                              fontWeight:
                                selectedOption?.id === option.id || option.id === correctOptionId
                                  ? "medium"
                                  : "normal",
                              color: getOptionTextColor(option.id), 
                            }}
                          >
                            {option.title} - {option.artist}
                          </Typography>
                        </Box>
                      </OptionButton>
                    </Grid>
                  ))}
              </Grid>

              {submitting && (
                <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
                  <CircularProgress size={40} />
                </Box>
              )}

              <ScoreDisplay>
                <Box>
                  <Typography variant="body2" color="textSecondary">
                    Round Score
                  </Typography>
                  <Typography variant="h6">{currentScore}</Typography>
                </Box>
                <Box sx={{ textAlign: "right" }}>
                  <Typography variant="body2" color="textSecondary">
                    Total Score
                  </Typography>
                  <Typography variant="h6">{totalScore}</Typography>
                </Box>
              </ScoreDisplay>
            </Box>
          </StyledPaper>
        </Fade>
      )}
    </Box>
  );
};