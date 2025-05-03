import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import {
  Box,
  Typography,
  Avatar,
  Paper,
  Button,
  Divider,
  CircularProgress,
  Grid,
  Container,
  Card,
  CardContent,
  CardActions,
  Fade,
  Chip,
  Tooltip,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import HistoryIcon from "@mui/icons-material/History";
import EqualizerIcon from "@mui/icons-material/Equalizer";
import MusicNoteIcon from "@mui/icons-material/MusicNote";
import EmojiEventsIcon from "@mui/icons-material/EmojiEvents";
import { useAuth } from "../../hooks/auth-context";
import axios, { AxiosError } from "axios";
import {
  getRecentGames,
  GameStatsDto,
  RecentGameDto,
} from "../../api/game-stats-api";
import { useTranslation } from 'react-i18next';
import { TranslationNamespace } from '../../i18n';

interface UserProfile {
  id: number;
  username: string;
  email: string;
  avatarUrl: string;
  totalScore: number;
  gamesPlayed: number;
  winRate: number;
  recentGames: RecentGameDto[];
  stats: GameStatsDto | null;
}

interface ErrorResponse {
  message: string;
  status: number;
}

const ProfilePaper = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(4),
  borderRadius: 16,
  boxShadow: "0 4px 20px rgba(0,0,0,0.08)",
  position: "relative",
  overflow: "hidden",
  "&::before": {
    content: '""',
    position: "absolute",
    top: 0,
    left: 0,
    right: 0,
    height: 120,
    background: theme.palette.primary.main,
    zIndex: 0,
  },
}));

const LargeAvatar = styled(Avatar)(({ theme }) => ({
  width: 120,
  height: 120,
  border: `4px solid ${theme.palette.background.paper}`,
  boxShadow: "0 4px 10px rgba(0,0,0,0.1)",
  marginTop: 60,
  zIndex: 1,
}));

const StatCard = styled(Card)(() => ({
  height: "100%",
  borderRadius: 12,
  transition: "transform 0.2s ease-in-out",
  "&:hover": {
    transform: "translateY(-5px)",
    boxShadow: "0 8px 20px rgba(0,0,0,0.12)",
  },
}));

const ActionButton = styled(Button)(() => ({
  borderRadius: 20,
  padding: "8px 24px",
  fontWeight: 600,
  textTransform: "none",
  boxShadow: "0 4px 10px rgba(0,0,0,0.1)",
}));

const AdminBadge = styled(Chip)(({ theme }) => ({
  fontWeight: "bold",
  fontSize: "0.85rem",
  padding: "0.5rem",
  borderRadius: "12px",
  background: `linear-gradient(45deg, ${theme.palette.warning.main} 30%, ${theme.palette.warning.dark} 90%)`,
  boxShadow: `0 3px 5px 2px ${theme.palette.warning.main}33`,
  marginTop: theme.spacing(1),
  animation: "pulse 2s infinite",
  "@keyframes pulse": {
    "0%": {
      boxShadow: `0 0 0 0 ${theme.palette.warning.main}80`,
    },
    "70%": {
      boxShadow: `0 0 0 10px ${theme.palette.warning.main}00`,
    },
    "100%": {
      boxShadow: `0 0 0 0 ${theme.palette.warning.main}00`,
    },
  },
  "& .MuiChip-icon": {
    color: "white",
  },
}));

export const Profile: React.FC = () => {
  const { t } = useTranslation(TranslationNamespace.Common, { keyPrefix: 'pages.profile' });
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);

  // Function to determine accuracy color based on percentage
  const getAccuracyColor = (
    accuracy: number
  ): "success" | "info" | "warning" | "error" => {
    if (accuracy >= 80) return "success";
    if (accuracy >= 60) return "info";
    if (accuracy >= 40) return "warning";
    return "error";
  };

  const getAccuracyLabel = (accuracy: number): string => {
    if (accuracy >= 80) return t('accuracyLabels.excellent');
    if (accuracy >= 60) return t('accuracyLabels.good');
    if (accuracy >= 40) return t('accuracyLabels.average');
    return t('accuracyLabels.poor');
  };

  useEffect(() => {
    const fetchUserProfile = async () => {
      if (!user) return;

      try {
        setLoading(true);

        const gamesResponse = await getRecentGames(0, 3, user.id);

        const userProfile: UserProfile = {
          id: user.id!,
          username: user.username || "Music Lover",
          email: user.email || "user@example.com",
          avatarUrl: user.username[0],
          totalScore: user.score,
          gamesPlayed: gamesResponse.stats?.totalGames || 0,
          winRate: gamesResponse.stats?.accuracy || 0,
          recentGames: gamesResponse.games,
          stats: gamesResponse.stats,
        };

        setProfile(userProfile);
        setLoading(false);
      } catch (err) {
        console.error("Error fetching user profile:", err);

        if (axios.isAxiosError(err)) {
          const axiosError = err as AxiosError<ErrorResponse>;
          const errorMessage =
            axiosError.response?.data?.message ||
            axiosError.message ||
            t('errorLoading');
          setError(errorMessage);
        } else {
          setError(t('unexpectedError'));
        }

        setLoading(false);
      }
    };

    fetchUserProfile();
  }, [user, t]);

  const handleViewResults = () => {
    navigate("/results");
  };

  const handleViewStats = () => {
    navigate("/scoreboard");
  };

  const handlePlayGame = () => {
    navigate("/quizzes");
  };

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="80vh"
      >
        <CircularProgress />
        <Typography variant="body1" sx={{ ml: 2 }}>
          {t('loading')}
        </Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="80vh"
      >
        <Typography color="error" variant="h6">
          {error}
        </Typography>
      </Box>
    );
  }

  if (!user) {
    return (
      <Fade in={true} timeout={800}>
        <Box
          display="flex"
          flexDirection="column"
          justifyContent="center"
          alignItems="center"
          minHeight="80vh"
        >
          <Typography variant="h5" gutterBottom>
            {t('loginRequired')}
          </Typography>
          <Button
            variant="contained"
            color="primary"
            onClick={() => navigate("/login", { state: { from: "/profile" } })}
            sx={{ mt: 2 }}
          >
            {t('loginButton')}
          </Button>
        </Box>
      </Fade>
    );
  }

  return (
    <Container maxWidth="lg">
      <Fade in={true} timeout={800}>
        <Box sx={{ py: 4 }}>
          <ProfilePaper>
            <Box display="flex" flexDirection="column" alignItems="center">
              <LargeAvatar src={profile?.avatarUrl} alt={profile?.username}>
                {profile?.username?.charAt(0) ||
                  user.username?.charAt(0) ||
                  "U"}
              </LargeAvatar>

              <Box
                sx={{
                  mt: 2,
                  textAlign: "center",
                  position: "relative",
                  zIndex: 1,
                }}
              >
                <Box
                  sx={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                  }}
                >
                  <Typography variant="h4" fontWeight="bold">
                    {profile?.username || user.username}
                  </Typography>
                </Box>
                <Typography variant="body1" color="textSecondary" gutterBottom>
                  {profile?.email || user.email}
                </Typography>

                <Chip
                  icon={<EmojiEventsIcon />}
                  label={`${t('totalScoreLabel')} ${user?.score || 0}`}
                  color="primary"
                  sx={{ fontWeight: "bold", my: 1 }}
                />
              </Box>
              {user.role === "ADMIN" && (
                <Tooltip title="Administrator privileges" arrow placement="top">
                  <AdminBadge
                    icon={<AdminPanelSettingsIcon />}
                    label="ADMIN"
                    color="warning"
                    variant="filled"
                    size="medium"
                  />
                </Tooltip>
              )}
            </Box>

            <Divider sx={{ my: 4 }} />

            <Grid container spacing={3}>
              <Grid size={{ xs: 12, sm: 8 }}>
                <Typography variant="h5" fontWeight="bold" gutterBottom>
                  {t('statsOverview')}
                </Typography>

                <Grid container spacing={2} sx={{ mb: 4 }}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <StatCard>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          {t('gamesPlayed')}
                        </Typography>
                        <Typography variant="h4" component="div">
                          {profile?.gamesPlayed || 0}
                        </Typography>
                      </CardContent>
                    </StatCard>
                  </Grid>

                  <Grid size={{ xs: 12, sm: 6 }}>
                    <StatCard>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          {t('accuracy')}
                        </Typography>
                        <Box
                          sx={{
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                          }}
                        >
                          <Typography
                            variant="h4"
                            component="div"
                            color={getAccuracyColor(profile?.winRate || 0)}
                          >
                            {profile?.winRate || 0}%
                          </Typography>
                          <Chip
                            label={getAccuracyLabel(profile?.winRate || 0)}
                            color={getAccuracyColor(profile?.winRate || 0)}
                            size="small"
                            sx={{ mt: 1 }}
                          />
                        </Box>
                      </CardContent>
                    </StatCard>
                  </Grid>

                </Grid>

                <Typography variant="h5" fontWeight="bold" gutterBottom>
                  {t('recentGames')}
                </Typography>

                {profile?.recentGames && profile.recentGames.length > 0 ? (
                  <Box>
                    {profile.recentGames.map((game) => {
                      const accuracy =
                        game.totalRounds > 0
                          ? Math.round(
                              (game.correctAnswers / game.totalRounds) * 100
                            )
                          : 0;

                      return (
                        <Card
                          key={game.sessionId}
                          sx={{ mb: 2, borderRadius: 2 }}
                        >
                          <CardContent sx={{ py: 2 }}>
                            <Box
                              display="flex"
                              justifyContent="space-between"
                              alignItems="center"
                            >
                              <Box>
                                <Typography variant="h6">
                                  {t('quiz', { id: game.quizId })}
                                </Typography>
                                <Typography
                                  variant="body2"
                                  color="textSecondary"
                                >
                                  {new Date(
                                    game.timestamp * 1000
                                  ).toLocaleDateString()}
                                </Typography>
                              </Box>
                              <Box>
                                <Typography
                                  variant="h6"
                                  color="primary"
                                  fontWeight="bold"
                                >
                                  {t('points', { score: game.score })}
                                </Typography>
                                <Chip
                                  label={t('accuracyPercentage', { accuracy })}
                                  size="small"
                                  color={getAccuracyColor(accuracy)}
                                  sx={{ mt: 0.5 }}
                                />
                              </Box>
                            </Box>
                          </CardContent>
                          <CardActions
                            sx={{ justifyContent: "flex-end", p: 1 }}
                          >
                            <Button
                              size="small"
                              onClick={() =>
                                navigate(
                                  `/game/player/${user?.id}/session/${game.sessionId}/results`
                                )
                              }
                            >
                              {t('viewDetails')}
                            </Button>
                          </CardActions>
                        </Card>
                      );
                    })}

                    <Box sx={{ textAlign: "center", mt: 2 }}>
                      <Button
                        variant="outlined"
                        onClick={handleViewResults}
                        startIcon={<HistoryIcon />}
                      >
                        {t('viewAllResults')}
                      </Button>
                    </Box>
                  </Box>
                ) : (
                  <Box sx={{ textAlign: "center", py: 4 }}>
                    <Typography
                      variant="body1"
                      color="textSecondary"
                      gutterBottom
                    >
                      {t('noGamesPlayed')}
                    </Typography>
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={handlePlayGame}
                      startIcon={<MusicNoteIcon />}
                      sx={{ mt: 2 }}
                    >
                      {t('playFirstGame')}
                    </Button>
                  </Box>
                )}
              </Grid>

              <Grid size={{ xs: 12, md: 4 }}>
                <Paper sx={{ p: 3, borderRadius: 3, height: "100%" }}>
                  <Typography variant="h5" fontWeight="bold" gutterBottom>
                    {t('actions')}
                  </Typography>

                  <Box
                    sx={{
                      display: "flex",
                      flexDirection: "column",
                      gap: 2,
                      mt: 3,
                    }}
                  >
                    <ActionButton
                      variant="contained"
                      color="primary"
                      startIcon={<MusicNoteIcon />}
                      onClick={handlePlayGame}
                      fullWidth
                    >
                      {t('playNewGame')}
                    </ActionButton>

                    <ActionButton
                      variant="outlined"
                      startIcon={<HistoryIcon />}
                      onClick={handleViewResults}
                      fullWidth
                    >
                      {t('viewGameHistory')}
                    </ActionButton>

                    <ActionButton
                      variant="outlined"
                      startIcon={<EqualizerIcon />}
                      onClick={handleViewStats}
                      fullWidth
                    >
                      {t('worldStatistics')}
                    </ActionButton>
                  </Box>

                  {profile?.stats && (
                    <Box sx={{ mt: 4 }}>
                      <Typography variant="h6" gutterBottom>
                        {t('yourStatistics')}
                      </Typography>
                      <Box
                        sx={{
                          display: "flex",
                          flexDirection: "column",
                          gap: 1,
                        }}
                      >
                        <Typography variant="body2">
                          <strong>{t('highestScore')}</strong>{" "}
                          {profile.stats.highestScore}
                        </Typography>
                        <Typography variant="body2">
                          <strong>{t('averageScore')}</strong>{" "}
                          {profile.stats.averageScore}
                        </Typography>
                        <Typography variant="body2">
                          <strong>{t('totalScore')}</strong>{" "}
                          {profile.stats.totalScore}
                        </Typography>
                      </Box>
                    </Box>
                  )}

                  <Box sx={{ mt: 4 }}>
                    <Typography variant="h6" gutterBottom>
                      {t('achievements')}
                    </Typography>
                    <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1 }}>
                      {profile?.stats?.totalGames &&
                        profile.stats.totalGames > 0 && (
                          <Chip
                            label={t('firstWin')}
                            size="small"
                            color="success"
                          />
                        )}
                      {profile?.stats?.highestScore &&
                        profile.stats.highestScore >= 100 && (
                          <Chip
                            label={t('centuryScore')}
                            size="small"
                            color="primary"
                          />
                        )}
                      {profile?.stats?.accuracy &&
                        profile.stats.accuracy >= 80 && (
                          <Chip
                            label={t('accuracyExpert')}
                            size="small"
                            color="success"
                          />
                        )}
                      {profile?.stats?.totalGames &&
                        profile.stats.totalGames >= 5 && (
                          <Chip label={t('gameStreak')} size="small" />
                        )}
                    </Box>
                  </Box>
                </Paper>
              </Grid>
            </Grid>
          </ProfilePaper>
        </Box>
      </Fade>
    </Container>
  );
};