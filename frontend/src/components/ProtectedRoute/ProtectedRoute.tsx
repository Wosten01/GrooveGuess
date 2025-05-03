import React from "react";
import { Link } from "react-router-dom";
import { Box, Button, Container, Typography, useTheme } from "@mui/material";
import { useTranslation } from "react-i18next";
import { TranslationNamespace } from "../../i18n";
import { motion } from "framer-motion";
import { useAuth } from "../../hooks/auth-context";

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
  const theme = useTheme();

  if (requireAdmin && user?.role !== 'ADMIN') {
    return (
      <Container maxWidth="md" sx={{ mt: 8, textAlign: 'center' }}>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          
          <Box sx={{ mb: 4 }}>
            <Typography 
              variant="h4" 
              component="h1" 
              gutterBottom
              sx={{ 
                fontWeight: 700,
                color: theme.palette.error.main
              }}
            >
              {t('adminRequired')}
            </Typography>
            <Typography 
              variant="body1" 
              color="text.secondary"
              sx={{ 
                maxWidth: "600px",
                mx: "auto",
                mb: 3
              }}
            >
              {t('adminOnlyPage')}
            </Typography>
          </Box>
          
          <Button 
            component={Link} 
            to="/" 
            variant="contained" 
            color="primary"
            sx={{ 
              mr: 2,
              px: 3,
              py: 1,
              borderRadius: "10px",
              fontWeight: 600
            }}
          >
            {t('returnHome')}
          </Button>
          
          <Button 
            component={Link} 
            to="/login" 
            variant="outlined"
            sx={{ 
              px: 3,
              py: 1,
              borderRadius: "10px",
              fontWeight: 600
            }}
          >
            {t('goToLogin')}
          </Button>
        </motion.div>
      </Container>
    );
  }

  if (!user) {
    return (
      <Container maxWidth="md" sx={{ mt: 8, textAlign: 'center' }}>
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          
          <Box sx={{ mb: 4 }}>
            <Typography 
              variant="h4" 
              component="h1" 
              gutterBottom
              sx={{ 
                fontWeight: 700,
                color: theme.palette.primary.main
              }}
            >
              {t('authenticationNeeded')}
            </Typography>
            <Typography 
              variant="body1" 
              color="text.secondary"
              sx={{ 
                maxWidth: "600px",
                mx: "auto",
                mb: 3
              }}
            >
              {t('pleaseLoginToAccess')}
            </Typography>
          </Box>
          
          <Button 
            component={Link} 
            to="/" 
            variant="contained" 
            color="primary"
            sx={{ 
              mr: 2,
              px: 3,
              py: 1,
              borderRadius: "10px",
              fontWeight: 600
            }}
          >
            {t('returnHome')}
          </Button>
          
          <Button 
            component={Link} 
            to="/login" 
            variant="outlined"
            color="primary"
            sx={{ 
              px: 3,
              py: 1,
              borderRadius: "10px",
              fontWeight: 600
            }}
          >
            {t('goToLogin')}
          </Button>
        </motion.div>
      </Container>
    );
  }

  return <>{children}</>;
};