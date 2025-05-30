
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
import { useTranslation } from "react-i18next";
import { TranslationNamespace } from "../../i18n";

interface AnswerResult {
  correct: boolean;
  points: number;
  isLastRound: boolean;
  finalScore?: number;
}

const TIME = 15;

export const Game: React.FC = () => {
  const { t } = useTranslation(TranslationNamespace.Common, {
    keyPrefix: "pages.game",
  });
  const { sessionId } = useParams<{ sessionId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const audioRef = useRef<HTMLAudioElement | null>(null);
  // Add a ref to track if a timeout submission is in progress
  const timeoutSubmissionInProgress = useRef<boolean>(false);

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
  const [totalScore, setTotalScore] = useState<number>(0);
  const [muted, setMuted] = useState<boolean>(false);
  const [snackbarOpen, setSnackbarOpen] = useState<boolean>(false);
  const [snackbarMessage, setSnackbarMessage] = useState<string>("");
  const [timeLeft, setTimeLeft] = useState<number>(TIME);
  const [maxTime, setMaxTime] = useState<number>(TIME);
  const [showPlayButton, setShowPlayButton] = useState<boolean>(false);
  const [correctOptionId, setCorrectOptionId] = useState<number | null>(null);
  const [timerActive, setTimerActive] = useState<boolean>(false);

  useEffect(() => {
    if (user !== undefined) {
      setAuthChecked(true);
    }
  }, [user]);

  // Start timer only when audio is ready
  useEffect(() => {
    if (audioReady && !timerActive) {
      setTimerActive(true);
    }
  }, [audioReady, timerActive]);

  const playAudio = useCallback(() => {
    if (audioRef.current) {
      // For Safari compatibility, ensure audio is properly loaded
      if (audioRef.current.readyState < 2) {  // HAVE_CURRENT_DATA or higher
        audioRef.current.load();
      }
      
      audioRef.current
        .play()
        .then(() => {
          setShowPlayButton(false);
        })
        .catch((error) => {
          console.error("Audio playback failed:", error);
          setShowPlayButton(true);
          setSnackbarMessage(t("autoplayError"));
          setSnackbarOpen(true);
        });
    }
  }, [t]);

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
      setTimerActive(false);
      // Reset the timeout submission flag
      timeoutSubmissionInProgress.current = false;

      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current.removeAttribute('src');
        audioRef.current.load();
      }

      const response = await getCurrentRound(sessionId, userId);

      setGameSession(response.data);

      setTotalScore(response.data.score || 0);

      const roundTimeLimit = TIME;
      setTimeLeft(roundTimeLimit);
      setMaxTime(roundTimeLimit);

      if (response.data.currentRound?.url) {
        // Process Dropbox URL if needed
        let audioUrl = response.data.currentRound.url;
        if (audioUrl.includes('dropbox.com')) {
          if (audioUrl.includes('dl=0')) {
            audioUrl = audioUrl.replace('dl=0', 'dl=1');
          } else if (!audioUrl.includes('dl=')) {
            audioUrl = audioUrl.includes('?') ? `${audioUrl}&dl=1` : `${audioUrl}?dl=1`;
          }
        }

        if (audioRef.current) {
          // Remove any existing event listeners to prevent duplicates
          const oldAudio = audioRef.current;
          oldAudio.oncanplaythrough = null;
          oldAudio.onerror = null;
          
          audioRef.current.src = audioUrl;
          audioRef.current.preload = "auto"; // Ensure preloading for Safari
          audioRef.current.load();
          
          audioRef.current.oncanplaythrough = () => {
            setAudioReady(true);
            playAudio();
          };
          
          audioRef.current.onerror = (e) => {
            console.error("Audio loading error:", e);
            setShowPlayButton(true);
            setSnackbarMessage(t("audioLoadError"));
            setSnackbarOpen(true);
          };
        } else {
          audioRef.current = new Audio(audioUrl);
          audioRef.current.preload = "auto";
          
          audioRef.current.oncanplaythrough = () => {
            setAudioReady(true);
            playAudio();
          };
          
          audioRef.current.onerror = (e) => {
            console.error("Audio loading error:", e);
            setShowPlayButton(true);
            setSnackbarMessage(t("audioLoadError"));
            setSnackbarOpen(true);
          };
        }
      }

      setLoading(false);
    } catch (err) {
      console.error("Error fetching current round:", err);

      if (axios.isAxiosError(err) && err.response?.status === 409) {
        console.log("Game is already completed, redirecting to results page");

        setGameSession((prev) => (prev ? { ...prev, completed: true } : null));

        setError(t("gameCompleted"));
      } else {
        setError(t("loadRoundError"));
      }

      setLoading(false);
    }
  }, [authChecked, sessionId, user, playAudio, t]);

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
        setError(t("loadRoundError"));
      }

      setLoading(false);
    }
  }, [user, sessionId, fetchCurrentRound, t]);

  const handleOptionSelect = useCallback(
    async (option: { id: number; title: string }) => {
      if (showResult || !gameSession || !sessionId || !user || timeLeft <= 0)
        return;

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
            (opt) => opt.id !== option.id
          );
          if (correctOption) {
            setCorrectOptionId(correctOption.id);
          }
        } else {
          setCorrectOptionId(option.id);
        }

        if (result.correct) {
          setTotalScore((prevScore) => prevScore + result.points);
        }

        if (result.isLastRound) {
          if (result.finalScore !== undefined) {
            setTotalScore(result.finalScore);
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

          setGameSession((prev) =>
            prev ? { ...prev, completed: true } : null
          );

          setTimeout(() => {
            navigate(`/game/player/${userId}/session/${sessionId}/results`);
          }, 1500);
        } else {
          setError(t("submitAnswerError"));
        }

        setSubmitting(false);
      }
    },
    [
      gameSession,
      sessionId,
      showResult,
      user,
      fetchNextRound,
      navigate,
      t,
      timeLeft,
    ]
  );

  // Handle timeout submission
  const handleTimeoutSubmission = useCallback(async () => {
    if (!gameSession || !sessionId || !user || timeoutSubmissionInProgress.current) return;
    
    // Set flag to prevent duplicate submissions
    timeoutSubmissionInProgress.current = true;
    
    const isLastRound = gameSession.currentRoundNumber === gameSession.totalRounds - 1;

    if (isLastRound) {
      setSnackbarMessage(t("timeUpLastRound"));
      setSnackbarOpen(true);
      setGameSession((prev) => prev ? { ...prev, completed: true } : null);

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
        setError(t("submitAnswerError"));
        timeoutSubmissionInProgress.current = false;
      }
    } else {
      setSnackbarMessage(t("timeUp"));
      setSnackbarOpen(true);

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
        setError(t("submitAnswerError"));
        timeoutSubmissionInProgress.current = false;
      }
    }
  }, [gameSession, sessionId, user, navigate, fetchNextRound, t]);

  useEffect(() => {
    // Only start the timer if audio is ready (or play button is shown) and timer should be active
    if (!gameSession || !sessionId || !user || showResult || timeLeft <= 0 || !timerActive)
      return;

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          if (!showResult && selectedOption) {
            handleOptionSelect(selectedOption);
          } else if (!showResult) {
            handleTimeoutSubmission();
          }
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [
    gameSession,
    sessionId,
    showResult,
    selectedOption,
    timeLeft,
    user,
    handleOptionSelect,
    handleTimeoutSubmission,
    timerActive,
  ]);

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

    if (
      optionId === correctOptionId ||
      (selectedOption?.id === optionId && optionId !== correctOptionId)
    ) {
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
          {t("checkingAuth")}
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
            {t("loginRequired")}
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
            {t("loginButton")}
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
          {t("loadingGame")}
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
        width: "100%",
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
                    {t("roundTitle", {
                      currentRound: gameSession.currentRoundNumber + 1,
                      totalRounds: gameSession.totalRounds,
                    })}
                  </Typography>

                  <Box sx={{ mb: 1 }}>
                    <Typography
                      variant="body2"
                      color="textSecondary"
                      align="center"
                    >
                      {!timerActive ? t("waitingForAudio") : t("timeRemaining", { seconds: timeLeft })}
                    </Typography>
                    <LinearProgress
                      variant={timerActive ? "determinate" : "indeterminate"}
                      value={timerActive ? (timeLeft / maxTime) * 100 : undefined}
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
                          onClick={() => {
                            playAudio();
                            // Start timer when play button is clicked
                            if (!timerActive) {
                              setTimerActive(true);
                            }
                          }}
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
                          {t("playTrack")}
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
                          ? t("correctAnswer", { points: answerResult.points })
                          : t("wrongAnswer")}
                      </Typography>
                      <Typography variant="body2">
                        {answerResult.isLastRound
                          ? t("lastRound")
                          : t("nextRound")}
                      </Typography>
                    </Box>
                  </Alert>
                </Grow>
              )}

              <Box sx={{ mb: 4, textAlign: "center" }}>
                <Typography variant="h6" gutterBottom>
                  {t("listenAndGuess")}
                </Typography>

                {!audioReady && !showPlayButton && (
                  <Box
                    sx={{ display: "flex", justifyContent: "center", my: 2 }}
                  >
                    <CircularProgress size={24} />
                    <Typography variant="body2" sx={{ ml: 1 }}>
                      {t("loadingAudio")}
                    </Typography>
                  </Box>
                )}
              </Box>

              <Typography variant="h6" gutterBottom align="center">
                {t("selectAnswer")}
              </Typography>

              <Grid container spacing={2}>
                {gameSession.currentRound.options &&
                  gameSession.currentRound.options.map((option, index) => (
                    <Grid size={{ xs: 12, sm: 6 }} key={option.id}>
                      <OptionButton
                        whileHover={{
                          scale:
                            timeLeft > 0 && !showResult && !submitting && timerActive
                              ? 1.02
                              : 1,
                        }}
                        whileTap={{
                          scale:
                            timeLeft > 0 && !showResult && !submitting && timerActive
                              ? 0.98
                              : 1,
                        }}
                        animate={{
                          opacity: [0, 1],
                          y: [20, 0],
                        }}
                        transition={{
                          delay: index * 0.1,
                          duration: 0.3,
                        }}
                        onClick={() =>
                          timeLeft > 0 &&
                          !showResult &&
                          !submitting &&
                          timerActive &&
                          handleOptionSelect(option)
                        }
                        style={{
                          width: "100%",
                          height: "100%",
                          cursor:
                            timeLeft > 0 && !showResult && !submitting && timerActive
                              ? "pointer"
                              : "default",
                        }}
                      >
                        <Box
                          sx={{
                            py: 2,
                            px: 2,
                            borderRadius: 3,
                            textAlign: "center",
                            backgroundColor: getOptionBackgroundColor(
                              option.id
                            ),
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
                              timeLeft > 0 && !showResult && !submitting && timerActive
                                ? "pointer"
                                : "default",
                            opacity:
                              timeLeft <= 0 || showResult || submitting || !timerActive
                                ? 0.7
                                : 1,
                            transition: "all 0.2s ease",
                            height: "100%",
                            display: "flex",
                            flexDirection: "column",
                            justifyContent: "center",
                            "&:hover": {
                              backgroundColor:
                                timeLeft > 0 && !showResult && !submitting && timerActive
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
                                selectedOption?.id === option.id ||
                                option.id === correctOptionId
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
                <Box
                  sx={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    width: "100%",
                    background:
                      "linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(33, 150, 243, 0.1) 100%)",
                    borderRadius: 3,
                    padding: 2,
                    boxShadow: "0 4px 12px rgba(0, 0, 0, 0.05)",
                    border: "1px solid rgba(76, 175, 80, 0.3)",
                  }}
                >
                  <Typography
                    variant="body1"
                    color="textSecondary"
                    sx={{
                      fontWeight: 500,
                      fontSize: "1rem",
                      mb: 0.5,
                    }}
                  >
                    {t("totalScore")}
                  </Typography>
                  <Typography
                    variant="h4"
                    sx={{
                      fontWeight: "bold",
                      color: "primary.main",
                      textShadow: "0 2px 4px rgba(0, 0, 0, 0.1)",
                      fontSize: { xs: "2rem", sm: "2.5rem" },
                    }}
                  >
                    {totalScore}
                  </Typography>
                </Box>
              </ScoreDisplay>
            </Box>
          </StyledPaper>
        </Fade>
      )}
    </Box>
  );
};
