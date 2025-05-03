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
  Chip
} from '@mui/material';
import { AxiosError } from 'axios';
import { getGameResults, GameResultsDto, TrackOptionDto } from '../../api/quiz-game-api';
import { useAuth } from '../../hooks/auth-context';
import { useTranslation } from 'react-i18next';
import { TranslationNamespace } from '../../i18n';

interface ErrorResponse {
  message: string;
}

export const GameResults: React.FC = () => {
  const { t } = useTranslation(TranslationNamespace.Common, { keyPrefix: 'pages.gameResults' });
  const { sessionId } = useParams<{ sessionId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  
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
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <CircularProgress />
        <Typography variant="body1" sx={{ ml: 2 }}>
          {t('checkingAuth')}
        </Typography>
      </Box>
    );
  }

  if (authChecked && !user) {
    return (
      <Box display="flex" flexDirection="column" justifyContent="center" alignItems="center" minHeight="80vh">
        <Alert severity="warning" sx={{ mb: 2 }}>
          {t('loginRequired')}
        </Alert>
        <Button 
          variant="contained" 
          color="primary"
          onClick={() => navigate("/login", { state: { from: window.location.pathname } })}
        >
          {t('loginButton')}
        </Button>
      </Box>
    );
  }

  if (loading) {
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
      </Box>
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
      <Typography variant="body2">{title}</Typography>
      <Typography variant="caption" color="textSecondary">
        {t('by')} {artist}
      </Typography>
    </>
  );

  return (
    <Box sx={{ maxWidth: 900, mx: 'auto', p: 3 }}>
      {results && (
        <>
          <Paper elevation={3} sx={{ p: 3, mb: 3, textAlign: 'center' }}>
            <Typography variant="h4" gutterBottom>
              {t('title')}
            </Typography>
            <Typography variant="h5" gutterBottom>
              {t('quizNumber', { id: results.quizId })}
            </Typography>
            
            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 4, my: 3 }}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h3" color="primary">
                  {results.score}
                </Typography>
                <Typography variant="body1">
                  {t('finalScore')}
                </Typography>
              </Box>
              
              <Divider orientation="vertical" flexItem />
              
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h3" color="primary">
                  {stats.accuracy}%
                </Typography>
                <Typography variant="body1">
                  {t('accuracy')}
                </Typography>
              </Box>
              
              <Divider orientation="vertical" flexItem />
              
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h3" color="primary">
                  {stats.correctAnswers}/{results.totalRounds}
                </Typography>
                <Typography variant="body1">
                  {t('correctAnswers')}
                </Typography>
              </Box>
            </Box>
          </Paper>
          
          <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
            <Typography variant="h5" gutterBottom>
              {t('roundDetails')}
            </Typography>
            
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>{t('round')}</TableCell>
                    <TableCell>{t('correctTrack')}</TableCell>
                    <TableCell>{t('yourAnswer')}</TableCell>
                    <TableCell>{t('result')}</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {results.tracks.map((track) => {
                    const userAnswerOption = getUserAnswerOption(track);
                    
                    return (
                      <TableRow 
                        key={track.roundNumber}
                        sx={{ 
                          backgroundColor: track.wasGuessed ? 'rgba(76, 175, 80, 0.1)' : 'rgba(244, 67, 54, 0.1)'
                        }}
                      >
                        <TableCell>{track.roundNumber + 1}</TableCell>
                        <TableCell>
                          <TrackDisplay title={track.title} artist={track.artist} />
                        </TableCell>
                        <TableCell>
                          {userAnswerOption ? (
                            <TrackDisplay title={userAnswerOption.title} artist={userAnswerOption.artist} />
                          ) : (
                            <Typography variant="body2" color="text.secondary">{t('noAnswer')}</Typography>
                          )}
                        </TableCell>
                        <TableCell>
                          {track.wasGuessed ? (
                            <Chip label={t('correct')} color="success" size="small" />
                          ) : (
                            <Chip label={t('incorrect')} color="error" size="small" />
                          )}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
          
          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2 }}>
            <Button 
              variant="contained" 
              color="primary" 
              size="large"
              onClick={handlePlayAgain}
            >
              {t('playAgain')}
            </Button>
            <Button 
              variant="outlined" 
              color="primary" 
              size="large"
              onClick={handleViewScoreboard}
            >
              {t('viewScoreboard')}
            </Button>
          </Box>
        </>
      )}
    </Box>
  );
};