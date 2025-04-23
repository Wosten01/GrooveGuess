import React from 'react';
import { Button, ButtonProps } from '@mui/material';
import { useTheme } from '@mui/material/styles';

interface Props extends ButtonProps {
  loading?: boolean;
  label: string;
}

export const FormikSubmitButton: React.FC<Props> = ({
  loading = false,
  label,
  disabled,
  sx,
  ...props
}) => {
  const theme = useTheme();

  return (
    <Button
      type="submit"
      variant="contained"
      color="primary"
      fullWidth
      disabled={loading || disabled}
      sx={{
        mt: 3,
        borderRadius: '0.75rem',
        paddingY: '0.5rem',
        fontWeight: 600,
        fontFamily: theme.typography.fontFamily,
        fontSize: '1.1rem',
        background: `linear-gradient(90deg, ${theme.palette.primary.light}, ${theme.palette.secondary.light})`,
        color: theme.palette.primary.dark,
        boxShadow: '0 4px 16px 0 rgba(38, 166, 154, 0.10)',
        '&:hover': {
          background: `linear-gradient(90deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
          color: theme.palette.background.paper,
        },
        ...sx,
      }}
      {...props}
    >
      {loading ? (
        <span>{label}...</span>
      ) : (
        label
      )}
    </Button>
  );
};