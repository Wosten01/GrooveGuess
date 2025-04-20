import { Button } from '@mui/material';
import { FC } from 'react';

export interface Quiz {
  id: string;
  title: string;
  description?: string;
  roundCount: number;
}

interface QuizCardProps {
  quiz: Quiz;
}

export const QuizCard: FC<QuizCardProps> = ({ quiz }) => {
  const startQuiz = () => {
    console.log('Quiz started');
  };

  return (
    <div className="p-4 bg-white rounded-lg shadow-md max-w-md mx-auto">
      <h2 className="text-xl font-semibold text-gray-800">{quiz.title}</h2>
      <p className="text-gray-600">{quiz.description}</p>
      <Button variant="contained" onClick={startQuiz} sx={{ mt: 2, textTransform: 'none' }}>
        Start Quiz
      </Button>
    </div>
  );
};
