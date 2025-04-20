import { ThemeProvider } from '@mui/material/styles';
import theme from './theme';
import { CssBaseline } from '@mui/material';
import { QuizCard } from './components';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <div className="p-4">
        <h1 className="text-3xl font-bold text-blue-600">GrooveGuess</h1>
      </div>
      <QuizCard quiz={{title: "hello", description: "desc", id: "1123", roundCount:4}} />
    </ThemeProvider>
  );
}

export default App;