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
  Alert
} from '@mui/material';
import { AxiosError } from 'axios';
import { getGameResults, GameResultsDto } from '../../api/quiz-game-api';
import { useAuth } from '../../hooks/auth-context';

interface ErrorResponse {
  message: string;
}

export const GameResults: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [results, setResults] = useState<GameResultsDto | null>(null);
  const [authChecked, setAuthChecked] = useState<boolean>(false);

  // Check if user is authenticated
  useEffect(() => {
    // If user data is available (either logged in or definitely not logged in)
    if (user !== undefined) {
      setAuthChecked(true);
    }
  }, [user]);

  // Fetch results once authentication is checked
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
        
        // Type-safe error handling
        const axiosError = err as AxiosError<ErrorResponse>;
        const errorMessage = axiosError.response?.data?.message || 
                            axiosError.message || 
                            'Failed to load game results';
        
        setError(errorMessage);
        setLoading(false);
      }
    };

    if (authChecked && user) {
      fetchResults();
    }
  }, [authChecked, user, sessionId]);

  const handlePlayAgain = () => {
    navigate('/quizzes');
  };

  const handleViewScoreboard = () => {
    navigate('/scoreboard');
  };

  // Calculate statistics
  const calculateStats = () => {
    if (!results) return { correctAnswers: 0, accuracy: 0 };
    
    const correctAnswers = results.tracks.filter(track => track.wasGuessed).length;
    const accuracy = results.totalRounds > 0 
      ? Math.round((correctAnswers / results.totalRounds) * 100) 
      : 0;
    
    return { correctAnswers, accuracy };
  };

  // Show loading while checking authentication
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

  // Show login prompt if not authenticated
  if (authChecked && !user) {
    return (
      <Box display="flex" flexDirection="column" justifyContent="center" alignItems="center" minHeight="80vh">
        <Alert severity="warning" sx={{ mb: 2 }}>
          You need to be logged in to view game results.
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

  return (
    <Box sx={{ maxWidth: 900, mx: 'auto', p: 3 }}>
      {results && (
        <>
          <Paper elevation={3} sx={{ p: 3, mb: 3, textAlign: 'center' }}>
            <Typography variant="h4" gutterBottom>
              Game Results
            </Typography>
            <Typography variant="h5" gutterBottom>
              Quiz #{results.quizId}
            </Typography>
            
            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 4, my: 3 }}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h3" color="primary">
                  {results.score}
                </Typography>
                <Typography variant="body1">
                  Final Score
                </Typography>
              </Box>
              
              <Divider orientation="vertical" flexItem />
              
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h3" color="primary">
                  {stats.accuracy}%
                </Typography>
                <Typography variant="body1">
                  Accuracy
                </Typography>
              </Box>
              
              <Divider orientation="vertical" flexItem />
              
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h3" color="primary">
                  {stats.correctAnswers}/{results.totalRounds}
                </Typography>
                <Typography variant="body1">
                  Correct Answers
                </Typography>
              </Box>
            </Box>
          </Paper>
          
          <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
            <Typography variant="h5" gutterBottom>
              Round Details
            </Typography>
            
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Round</TableCell>
                    <TableCell>Track</TableCell>
                    <TableCell>Artist</TableCell>
                    <TableCell>Your Answer</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {results.tracks.map((track) => {
                    const userAnswer = "Unknown"; 
                    
                    return (
                      <TableRow 
                        key={track.roundNumber}
                        sx={{ 
                          backgroundColor: track.wasGuessed ? 'rgba(76, 175, 80, 0.1)' : 'rgba(244, 67, 54, 0.1)'
                        }}
                      >
                        <TableCell>{track.roundNumber + 1}</TableCell>
                        <TableCell>
                          <Typography variant="body2">{track.title}</Typography>
                          <Typography variant="caption" color="textSecondary">{track.artist}</Typography>
                        </TableCell>
                        <TableCell>{track.artist}</TableCell>
                        {/* TODO: Add Answer Stats */}
                        <TableCell>{userAnswer}</TableCell>
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
              Play Another Quiz
            </Button>
            <Button 
              variant="outlined" 
              color="primary" 
              size="large"
              onClick={handleViewScoreboard}
            >
              View Scoreboard
            </Button>
          </Box>
        </>
      )}
    </Box>
  );
};