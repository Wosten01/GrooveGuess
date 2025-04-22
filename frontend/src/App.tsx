import { ThemeProvider } from '@mui/material/styles';
import { theme } from './theme';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Home, LoginPage, RegisterPage } from './pages';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <BrowserRouter>
      <Routes>
        <Route path='/' element={<Home />} />
        <Route path='/login' element={<LoginPage />} />
        <Route path='/register' element={<RegisterPage />} />
      </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;