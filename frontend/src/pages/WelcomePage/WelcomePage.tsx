import { Card, CardContent, Typography, Button, Divider, useTheme } from '@mui/material';
import MusicNoteIcon from '@mui/icons-material/MusicNote';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { useTranslation } from 'react-i18next';
import './WelcomePage.css';
import { TranslationNamespace } from '../../i18n';
import { useNavigate } from 'react-router-dom';

export const WelcomePage = () => {
  const { t } = useTranslation(TranslationNamespace.Common, { keyPrefix: 'pages.home' }) 
  const theme = useTheme();
  const navigate = useNavigate();

  const handleClick = () => {
    navigate("/quizzes")
  }

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        background: `linear-gradient(135deg, ${theme.palette.accent.light} 0%, ${theme.palette.pastel.main} 100%)`,
        padding: '2rem',
        fontFamily: theme.typography.fontFamily,
      }}
    >
      <Card
        sx={{
          maxWidth: 500,
          width: '100%',
          borderRadius: '1.5rem',
          boxShadow: '0 8px 24px rgba(76, 175, 80, 0.10)',
          transition: 'transform 0.3s ease, box-shadow 0.3s ease',
          fontFamily: theme.typography.fontFamily,
          '&:hover': {
            transform: 'translateY(-8px)',
            boxShadow: '0 12px 32px rgba(76, 175, 80, 0.15)',
          },
        }}
      >
        <CardContent
          sx={{
            padding: '3rem',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 2,
            fontFamily: theme.typography.fontFamily,
          }}
        >
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 90,
              height: 90,
              borderRadius: '50%',
              background: `linear-gradient(45deg, ${theme.palette.accent.main}, ${theme.palette.primary.light})`,
              boxShadow: '0 4px 12px rgba(76, 175, 80, 0.08)',
              transition: 'transform 0.3s ease',
            }}
            className="icon-container"
          >
            <MusicNoteIcon
              className='sway-note'
              sx={{
                fontSize: { xs: 36, sm: 44, md: 50, lg: 60 },
                color: theme.palette.primary.main,
              }}
            />
          </div>
          <Typography
            variant="h4"
            align="center"
            sx={{
              color: theme.palette.primary.dark,
              marginTop: '1rem',
            }}
          >
            {t('welcome')}
          </Typography>
          <Typography
            variant="body1"
            align="center"
            sx={{
              color: theme.palette.secondary.dark,
              maxWidth: 360,
            }}
          >
            {t('description')}
          </Typography>
          <Divider sx={{ width: 80, borderColor: theme.palette.accent.main }} />
          <Button
            variant="contained"
            endIcon={<PlayArrowIcon />}
            size="large"
            sx={{
              padding: '0.75rem 2rem',
              borderRadius: '2rem',
              background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
              boxShadow: '0 4px 12px rgba(38, 166, 154, 0.18)',
              fontFamily: theme.typography.fontFamily,
              '&:hover': {
                background: `linear-gradient(45deg, ${theme.palette.primary.dark}, ${theme.palette.secondary.dark})`,
                transform: 'scale(1.05)',
              },
            }}
            onClick={()=> handleClick()}
          >
            {t('start')}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};