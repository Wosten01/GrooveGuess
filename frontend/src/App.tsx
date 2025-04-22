import { ThemeProvider } from '@mui/material/styles';
import { theme } from './theme';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Home } from './pages';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <BrowserRouter>
      <Routes>
        <Route path='/' element={<Home />} />
      </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;