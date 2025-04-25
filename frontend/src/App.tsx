import { ThemeProvider } from '@mui/material/styles';
import { theme } from './theme';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { WelcomePage, LoginPage, RegisterPage } from './pages';
import { MainAppBar } from './components';
import { AuthProvider } from './context/AuthContext';

function App() {
  return (
    <AuthProvider>
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
    </AuthProvider>  
  );
}

export default App;