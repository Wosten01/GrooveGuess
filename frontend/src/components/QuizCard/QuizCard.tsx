import { FC } from 'react';
import { Button, Card, CardContent, Typography } from '@mui/material';

interface Quiz {
  id: number;
  title: string;
  description?: string;
  roundCount: number;
}

interface QuizCardProps {
  quiz: Quiz;
  onStart: () => void;
}

export const QuizCard: FC<QuizCardProps> = ({ quiz, onStart }) => {
  return (
    <Card className="max-w-md mx-auto">
      <CardContent>
        <Typography variant="h5" className="font-semibold text-gray-800">
          {quiz.title}
        </Typography>
        <Typography variant="body2" className="text-gray-600 mt-2">
          {quiz.description || 'No description'}
        </Typography>
        <Typography variant="body2" className="text-gray-600 mt-1">
          Rounds: {quiz.roundCount}
        </Typography>
        <Button
          variant="contained"
          onClick={onStart}
          sx={{ mt: 2, textTransform: 'none' }}
          className="w-full"
        >
          Start Quiz
        </Button>
      </CardContent>
    </Card>
  );
};
