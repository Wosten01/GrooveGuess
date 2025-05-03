import React from 'react';
import { useAuth } from '../../hooks/auth-context';
import { Alert, Box, Container, Typography, Button } from '@mui/material';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { TranslationNamespace } from '../../i18n';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAdmin?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  requireAdmin = false 
}) => {
  const { user } = useAuth();
  const { t } = useTranslation(TranslationNamespace.Common, { keyPrefix: 'pages.protectedRoute' });

  if (requireAdmin && user?.role !== 'ADMIN') {
    return (
      <Container maxWidth="md" sx={{ mt: 8, textAlign: 'center' }}>
        <Alert severity="error" sx={{ mb: 3 }}>
          {t('accessDenied')}
        </Alert>
        
        <Box sx={{ mb: 4 }}>
          <Typography variant="h5" component="h1" gutterBottom>
            {t('adminRequired')}
          </Typography>
          <Typography variant="body1" color="text.secondary">
            {t('adminOnlyPage')}
          </Typography>
        </Box>
        
        <Button 
          component={Link} 
          to="/" 
          variant="contained" 
          color="primary"
          sx={{ mr: 2 }}
        >
          {t('returnHome')}
        </Button>
        
        <Button 
          component={Link} 
          to="/login" 
          variant="outlined"
        >
          {t('goToLogin')}
        </Button>
      </Container>
    );
  }

  return <>{children}</>;
};