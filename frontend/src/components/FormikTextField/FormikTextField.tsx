import React from 'react';
import { TextField, TextFieldProps } from '@mui/material';
import { useField } from 'formik';
import { useTheme } from '@mui/material/styles';

interface Props extends Omit<TextFieldProps, 'name'> {
  name: string;
  label: string;
  autoComplete?: string;
  'aria-label'?: string;
  'aria-describedby'?: string;
}

export const FormikTextField: React.FC<Props> = ({
  name,
  label,
  autoComplete,
  ...props
}) => {
  const [field, meta] = useField(name);
  const theme = useTheme();

  return (
    <TextField
      fullWidth
      margin="normal"
      id={name}
      label={label}
      variant="outlined"
      {...field}
      {...props}
      autoComplete={autoComplete}
      error={Boolean(meta.touched && meta.error)}
      helperText={meta.touched && meta.error}
      sx={{
        borderRadius: '0.75rem',
        '& .MuiOutlinedInput-root': {
          '& fieldset': {
            borderColor: theme.palette.accent.main,
          },
          '&:hover fieldset': {
            borderColor: theme.palette.primary.light,
          },
          '&.Mui-focused fieldset': {
            borderColor: theme.palette.primary.main,
          },
        },
        ...props.sx,
      }}
    />
  );
};