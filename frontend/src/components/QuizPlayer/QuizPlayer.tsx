import { FC, useState } from 'react';
import { Button, Typography, Box } from '@mui/material';
import axios from 'axios';

interface Track {
  id: number;
  title: string;
  artist: string;
  url: string;
}

interface GameRound {
  roundNumber: number;
  tracks: Track[];
  correctTrackId: number;
}

interface QuizPlayerProps {
  quizId: number;
  userId: number;
}

export const QuizPlayer: FC<QuizPlayerProps> = ({ quizId, userId }) => {
  const [rounds, setRounds] = useState<GameRound[]>([]);
  const [currentRound, setCurrentRound] = useState(0);
  const [score, setScore] = useState(0);
  const [message, setMessage] = useState('');

  const fetchRounds = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/quizzes/${quizId}/game-rounds`, {
        params: { tracksPerRound: 4 },
      });
      setRounds(response.data);
    } catch (error) {
      console.error('Error fetching rounds:', error);
    }
  };

  const submitAnswer = async (selectedTrackId: number) => {
    if (!rounds[currentRound]) return;

    try {
      const response = await axios.post(
        `http://localhost:8080/api/quizzes/${quizId}/answer`,
        null,
        {
          params: {
            userId,
            selectedTrackId,
            correctTrackId: rounds[currentRound].correctTrackId,
          },
        }
      );
      const { status, score: newScore, correctTrackId } = response.data;
      setScore(newScore);
      setMessage(
        status === 'CORRECT'
          ? 'Correct! +10 points'
          : `Incorrect. Correct track was ID ${correctTrackId}.`
      );

      // Перейти к следующему раунду через 2 секунды
      setTimeout(() => {
        setCurrentRound(prev => prev + 1);
        setMessage('');
      }, 2000);
    } catch (error) {
      console.error('Error submitting answer:', error);
    }
  };

  // Загрузить раунды при монтировании
  useState(() => {
    fetchRounds();
  });

  if (!rounds.length) {
    return <Typography>Loading rounds...</Typography>;
  }

  if (currentRound >= rounds.length) {
    return (
      <Box className="p-4">
        <Typography variant="h5">Quiz Completed!</Typography>
        <Typography>Your final score: {score}</Typography>
      </Box>
    );
  }

  const round = rounds[currentRound];

  return (
    <Box className="p-4 max-w-md mx-auto">
      <Typography variant="h5">Round {round.roundNumber}</Typography>
      <Typography variant="body1">Score: {score}</Typography>
      {message && <Typography color={message.includes('Correct') ? 'green' : 'red'}>{message}</Typography>}
      <Box className="mt-4">
        {round.tracks.map(track => (
          <Button
            key={track.id}
            variant="outlined"
            onClick={() => submitAnswer(track.id)}
            className="w-full mb-2"
          >
            {track.title} by {track.artist}
          </Button>
        ))}
      </Box>
    </Box>
  );
};
