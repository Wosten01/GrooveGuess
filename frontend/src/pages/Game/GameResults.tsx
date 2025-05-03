import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Box, 
  Typography, 
  Button, 
  CircularProgress, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow,
  Divider,
  Alert,
  Chip,
  Container,
  useTheme,
  useMediaQuery
} from '@mui/material';
import { AxiosError } from 'axios';
import { getGameResults, GameResultsDto, TrackOptionDto } from '../../api/quiz-game-api';
import { useAuth } from '../../hooks/auth-context';
import { useTranslation } from 'react-i18next';
import { TranslationNamespace } from '../../i18n';
import { motion } from 'framer-motion';

interface ErrorResponse {
  message: string;
}

export const GameResults: React.FC = () => {
  const { t } = useTranslation(TranslationNamespace.Common, { keyPrefix: 'pages.gameResults' });
  const { sessionId } = useParams<{ sessionId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [results, setResults] = useState<GameResultsDto | null>(null);
  const [authChecked, setAuthChecked] = useState<boolean>(false);

  useEffect(() => {
    if (user !== undefined) {
      setAuthChecked(true);
    }
  }, [user]);

  useEffect(() => {
    const fetchResults = async () => {
      if (!sessionId || !user) return;
      
      try {
        setLoading(true);
        const response = await getGameResults(sessionId, user.id);
        setResults(response);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching game results:', err);
        
        const axiosError = err as AxiosError<ErrorResponse>;
        const errorMessage = axiosError.response?.data?.message || 
                            axiosError.message || 
                            t('fetchError');
        
        setError(errorMessage);
        setLoading(false);
      }
    };

    if (authChecked && user) {
      fetchResults();
    }
  }, [authChecked, user, sessionId, t]);

  const handlePlayAgain = () => {
    navigate('/quizzes');
  };

  const handleViewScoreboard = () => {
    navigate('/scoreboard');
  };

  const calculateStats = () => {
    if (!results) return { correctAnswers: 0, accuracy: 0 };
    
    const correctAnswers = results.tracks.filter(track => track.wasGuessed).length;
    const accuracy = results.totalRounds > 0 
      ? Math.round((correctAnswers / results.totalRounds) * 100) 
      : 0;
    
    return { correctAnswers, accuracy };
  };

  if (!authChecked) {
    return (
      <Container maxWidth="lg" sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "80vh" }}>
        <CircularProgress />
        <Typography variant="body1" sx={{ ml: 2 }}>
          {t('checkingAuth')}
        </Typography>
      </Container>
    );
  }

  if (authChecked && !user) {
    return (
      <Container maxWidth="lg" sx={{ display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center", minHeight: "80vh" }}>
        <Alert severity="warning" sx={{ mb: 2, width: { xs: '100%', sm: '80%', md: '60%' } }}>
          {t('loginRequired')}
        </Alert>
        <Button 
          variant="contained" 
          color="primary"
          onClick={() => navigate("/login", { state: { from: window.location.pathname } })}
        >
          {t('loginButton')}
        </Button>
      </Container>
    );
  }

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "80vh" }}>
        <CircularProgress />
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "80vh" }}>
        <Typography color="error" variant="h6">{error}</Typography>
      </Container>
    );
  }

  const stats = calculateStats();

  const getUserAnswerOption = (track: any): TrackOptionDto | null => {
    if (!track.userAnswer) return null;
    
    return track.options.find(
      (option: TrackOptionDto) => option.id === track.userAnswer.selectedOptionId
    ) || null;
  };

  const TrackDisplay = ({ title, artist }: { title: string, artist: string }) => (
    <>
      <Typography variant="body2" sx={{ fontWeight: 'medium' }}>{title}</Typography>
      <Typography variant="caption" color="textSecondary">
        {t('by')} {artist}
      </Typography>
    </>
  );

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {results && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <Paper 
            elevation={3} 
            sx={{ 
              p: { xs: 2, sm: 3, md: 4 }, 
              mb: 4, 
              textAlign: 'center',
              borderRadius: 2,
              background: `linear-gradient(to bottom, ${theme.palette.background.paper}, ${theme.palette.background.default})`,
              boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)'
            }}
          >
            <Typography 
              variant="h4" 
              gutterBottom
              sx={{ 
                fontWeight: 'bold',
                color: theme.palette.primary.main,
                fontSize: { xs: '1.75rem', sm: '2.25rem', md: '2.5rem' }
              }}
            >
              {t('title')}
            </Typography>
            <Typography 
              variant="h5" 
              gutterBottom
              sx={{ 
                color: theme.palette.text.primary,
                mb: 3,
                fontSize: { xs: '1.25rem', sm: '1.5rem', md: '1.75rem' }
              }}
            >
              {results.quizTitle}
            </Typography>
            
            <Box 
              sx={{ 
                display: 'flex', 
                flexDirection: isMobile ? 'column' : 'row',
                justifyContent: 'center', 
                alignItems: 'center',
                gap: isMobile ? 3 : 4, 
                my: 3 
              }}
            >
              <Box 
                sx={{ 
                  textAlign: 'center',
                  background: 'rgba(76, 175, 80, 0.05)',
                  borderRadius: 2,
                  p: 2,
                  minWidth: { sm: '150px', md: '180px' },
                  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.05)',
                  border: '1px solid rgba(76, 175, 80, 0.2)'
                }}
              >
                <Typography 
                  variant="h3" 
                  color="primary"
                  sx={{ 
                    fontWeight: 'bold',
                    fontSize: { xs: '2.5rem', sm: '3rem', md: '3.5rem' }
                  }}
                >
                  {results.score}
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                  {t('finalScore')}
                </Typography>
              </Box>
              
              {!isMobile && <Divider orientation="vertical" flexItem />}
              
              <Box 
                sx={{ 
                  textAlign: 'center',
                  background: 'rgba(33, 150, 243, 0.05)',
                  borderRadius: 2,
                  p: 2,
                  minWidth: { sm: '150px', md: '180px' },
                  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.05)',
                  border: '1px solid rgba(33, 150, 243, 0.2)'
                }}
              >
                <Typography 
                  variant="h3" 
                  color="primary"
                  sx={{ 
                    fontWeight: 'bold',
                    fontSize: { xs: '2.5rem', sm: '3rem', md: '3.5rem' }
                  }}
                >
                  {stats.accuracy}%
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                  {t('accuracy')}
                </Typography>
              </Box>
              
              {!isMobile && <Divider orientation="vertical" flexItem />}
              
              <Box 
                sx={{ 
                  textAlign: 'center',
                  background: 'rgba(156, 39, 176, 0.05)',
                  borderRadius: 2,
                  p: 2,
                  minWidth: { sm: '150px', md: '180px' },
                  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.05)',
                  border: '1px solid rgba(156, 39, 176, 0.2)'
                }}
              >
                <Typography 
                  variant="h3" 
                  color="primary"
                  sx={{ 
                    fontWeight: 'bold',
                    fontSize: { xs: '2.5rem', sm: '3rem', md: '3.5rem' }
                  }}
                >
                  {stats.correctAnswers}/{results.totalRounds}
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                  {t('correctAnswers')}
                </Typography>
              </Box>
            </Box>
          </Paper>
          
          <Paper 
            elevation={3} 
            sx={{ 
              p: { xs: 2, sm: 3, md: 4 }, 
              mb: 4,
              borderRadius: 2,
              boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)'
            }}
          >
            <Typography 
              variant="h5" 
              gutterBottom
              sx={{ 
                fontWeight: 'bold',
                color: theme.palette.primary.main,
                mb: 3
              }}
            >
              {t('roundDetails')}
            </Typography>
            
            <TableContainer sx={{ borderRadius: 1, overflow: 'hidden' }}>
              <Table>
                <TableHead>
                  <TableRow sx={{ backgroundColor: 'rgba(0, 0, 0, 0.03)' }}>
                    <TableCell sx={{ fontWeight: 'bold', width: '10%' }}>{t('round')}</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', width: '35%' }}>{t('correctTrack')}</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', width: '35%' }}>{t('yourAnswer')}</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', width: '20%' }}>{t('result')}</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {results.tracks.map((track) => {
                    const userAnswerOption = getUserAnswerOption(track);
                    
                    return (
                      <TableRow 
                        key={track.roundNumber}
                        sx={{ 
                          backgroundColor: track.wasGuessed 
                            ? 'rgba(76, 175, 80, 0.1)' 
                            : 'rgba(244, 67, 54, 0.1)',
                          '&:hover': {
                            backgroundColor: track.wasGuessed 
                              ? 'rgba(76, 175, 80, 0.15)' 
                              : 'rgba(244, 67, 54, 0.15)',
                          }
                        }}
                      >
                        <TableCell sx={{ fontWeight: 'medium', fontSize: '1rem' }}>{track.roundNumber + 1}</TableCell>
                        <TableCell>
                          <TrackDisplay title={track.title} artist={track.artist} />
                        </TableCell>
                        <TableCell>
                          {userAnswerOption ? (
                            <TrackDisplay title={userAnswerOption.title} artist={userAnswerOption.artist} />
                          ) : (
                            <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
                              {t('noAnswer')}
                            </Typography>
                          )}
                        </TableCell>
                        <TableCell>
                          {track.wasGuessed ? (
                            <Chip 
                              label={t('correct')} 
                              color="success" 
                              size="small" 
                              sx={{ fontWeight: 'bold', px: 1 }}
                            />
                          ) : (
                            <Chip 
                              label={t('incorrect')} 
                              color="error" 
                              size="small" 
                              sx={{ fontWeight: 'bold', px: 1 }}
                            />
                          )}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
          
          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, flexWrap: 'wrap' }}>
            <Button 
              variant="contained" 
              color="primary" 
              size="large"
              onClick={handlePlayAgain}
              sx={{ 
                px: 4, 
                py: 1.5, 
                borderRadius: 2,
                fontWeight: 'bold',
                fontSize: '1rem',
                boxShadow: '0 4px 12px rgba(76, 175, 80, 0.2)',
                '&:hover': {
                  boxShadow: '0 6px 16px rgba(76, 175, 80, 0.3)',
                }
              }}
            >
              {t('playAgain')}
            </Button>
            <Button 
              variant="outlined" 
              color="primary" 
              size="large"
              onClick={handleViewScoreboard}
              sx={{ 
                px: 4, 
                py: 1.5, 
                borderRadius: 2,
                fontWeight: 'bold',
                fontSize: '1rem'
              }}
            >
              {t('viewScoreboard')}
            </Button>
          </Box>
        </motion.div>
      )}
    </Container>
  );
};