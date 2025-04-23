import { createTheme } from '@mui/material/styles';

declare module '@mui/material/styles' {
  interface Palette {
    accent: Palette['primary'];
    pastel: Palette['primary'];
  }
  interface PaletteOptions {
    accent?: PaletteOptions['primary'];
    pastel?: PaletteOptions['primary'];
  }
}

export const theme = createTheme({
  palette: {
    primary: {
      main: '#43a047', // насыщенный зелёный
      light: '#a5d6a7',
      dark: '#388e3c',
      contrastText: '#fff',
    },
    secondary: {
      main: '#26a69a', // бирюзовый
      light: '#b2dfdb',
      dark: '#00897b',
      contrastText: '#fff',
    },
    accent: {
      main: '#b2dfdb', // пастельный бирюзовый
      light: '#e0f7fa',
      dark: '#00897b',
      contrastText: '#388e3c',
    },
    pastel: {
      main: '#e8f5e9', // пастельный зелёный
      light: '#f1f8e9',
      dark: '#a5d6a7',
      contrastText: '#388e3c',
    },
    background: {
      default: '#e0f7fa',
      paper: '#fff',
    },
  },
  typography: {
    fontFamily: "'Montserrat', 'Roboto', sans-serif",
    h4: {
      fontWeight: 700,
      fontSize: '2.2rem',
      letterSpacing: '-0.5px',
    },
    body1: {
      fontSize: '1.15rem',
      color: '#43a047',
    },
    button: {
      fontWeight: 600,
      fontSize: '1.1rem',
      textTransform: 'none',
    },
  },
});