import React, { ReactNode } from 'react';
import { Box } from '@mui/material';
import { useTheme } from '@mui/material/styles';

interface PageBackgroundProps {
  children: ReactNode;
}

export const PageBackground: React.FC<PageBackgroundProps> = ({ children }) => {
  const theme = useTheme();

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: `linear-gradient(135deg, ${theme.palette.accent.light} 0%, ${theme.palette.pastel.main} 100%)`,
        padding: { xs: '1rem', sm: '2rem' },
        fontFamily: theme.typography.fontFamily,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
      }}
    >
      {children}
    </Box>
  );
};

export default PageBackground;