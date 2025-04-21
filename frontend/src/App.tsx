// src/App.tsx
import { useEffect, useState } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import theme from './theme';
import axios from 'axios';
import { QuizCard, QuizPlayer } from './components';

interface Quiz {
  id: number;
  title: string;
  description?: string;
  roundCount: number;
}

function App() {
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [selectedQuizId, setSelectedQuizId] = useState<number | null>(null);
  const userId = 1; 

  useEffect(() => {
    axios.get('http://localhost:8080/api/quizzes')
      .then(response => setQuizzes(response.data))
      .catch(error => console.error('Error fetching quizzes:', error));
  }, []);

  const startQuiz = (quizId: number) => {
    setSelectedQuizId(quizId);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <div className="p-4">
        <h1 className="text-3xl font-bold text-blue-600">GrooveGuess</h1>
        {selectedQuizId ? (
          <QuizPlayer quizId={selectedQuizId} userId={userId} />
        ) : (
          <div className="mt-4 space-y-4">
            {quizzes.map(quiz => (
              <QuizCard
                key={quiz.id}
                quiz={quiz}
                onStart={() => startQuiz(quiz.id)} // Обновим QuizCard
              />
            ))}
          </div>
        )}
      </div>
    </ThemeProvider>
  );
}

export default App;