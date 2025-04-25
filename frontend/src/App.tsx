import { ThemeProvider } from '@mui/material/styles';
import { theme } from './theme';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { WelcomePage, LoginPage, RegisterPage } from './pages';
import { MainAppBar } from './components';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <BrowserRouter>
      <MainAppBar />
      <Routes>
        <Route path='/' element={<WelcomePage />} />
        <Route path='/login' element={<LoginPage />} />
        <Route path='/register' element={<RegisterPage />} />
      </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;