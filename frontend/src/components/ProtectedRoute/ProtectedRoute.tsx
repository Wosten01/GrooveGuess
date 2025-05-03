import React from 'react';
import { useAuth } from '../../hooks/auth-context';
import { Alert, Box, Container, Typography, Button } from '@mui/material';
import { Link } from 'react-router-dom';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAdmin?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  requireAdmin = false 
}) => {
  const { user } = useAuth();

  if (requireAdmin && user?.role !== 'ADMIN') {
    return (
      <Container maxWidth="md" sx={{ mt: 8, textAlign: 'center' }}>
        <Alert severity="error" sx={{ mb: 3 }}>
          Доступ запрещен. Для просмотра этой страницы требуются права администратора.
        </Alert>
        
        <Box sx={{ mb: 4 }}>
          <Typography variant="h5" component="h1" gutterBottom>
            Требуются права администратора
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Эта страница доступна только для пользователей с правами администратора.
            Если вы считаете, что это ошибка, пожалуйста, свяжитесь с администратором системы.
          </Typography>
        </Box>
        
        <Button 
          component={Link} 
          to="/" 
          variant="contained" 
          color="primary"
          sx={{ mr: 2 }}
        >
          Вернуться на главную
        </Button>
        
        <Button 
          component={Link} 
          to="/login" 
          variant="outlined"
        >
          Перейти к авторизации 
        </Button>
      </Container>
    );
  }

  return <>{children}</>;
};