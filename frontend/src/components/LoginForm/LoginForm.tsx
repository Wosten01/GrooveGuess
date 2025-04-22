import { useTheme } from '@mui/material/styles';
import { Card, CardContent, Typography, Button, Box } from '@mui/material';
import { Formik, Form } from 'formik';
import { useTranslation } from 'react-i18next';
import { getLoginSchema } from './LoginScheme'
import { FormikTextField } from '../../components';

export type LoginFormValues = {
  email: string;
  password: string;
}

export type LoginFormProps = {
  onSubmit: (values: LoginFormValues) => Promise<void> | void;
  initialValues?: LoginFormValues;
  title?: string;
  submitLabel?: string;
}

export const LoginForm = ({
  onSubmit,
  initialValues = { email: '', password: '' },
  title,
  submitLabel,
}: LoginFormProps) => {
  const theme = useTheme();
  const { t } = useTranslation(undefined, { keyPrefix: 'pages.login' });

  const validationSchema = getLoginSchema(t);

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: `linear-gradient(120deg, ${theme.palette.pastel.light}, ${theme.palette.accent.light} 80%)`,
        fontFamily: theme.typography.fontFamily,
      }}
    >
      <Card sx={{
        maxWidth: 400,
        width: '100%',
        borderRadius: '1.5rem',
        boxShadow: '0 8px 32px 0 rgba(38, 166, 154, 0.10)',
        fontFamily: theme.typography.fontFamily,
        background: theme.palette.background.paper,
        border: `1.5px solid ${theme.palette.accent.light}`,
      }}>
        <CardContent>
          <Typography
            variant="h5"
            align="center"
            sx={{
              fontWeight: 700,
              color: theme.palette.primary.dark,
              mb: 3,
              fontFamily: theme.typography.fontFamily,
              letterSpacing: '-0.5px',
            }}
          >
            {title || t('title')}
          </Typography>
          <Formik
            initialValues={initialValues}
            validationSchema={validationSchema}
            onSubmit={async (values, actions) => {
              try {
                await onSubmit(values);
              } finally {
                actions.setSubmitting(false);
              }
            }}
          >
            {({ isSubmitting }) => (
              <Form>
                <FormikTextField
                    name="email"
                    label={t('email')}
                    autoComplete="email"
                    aria-label={t('email')}
                    aria-describedby="email-helper-text"
                />
                <FormikTextField
                    name="password"
                    label={t('password')}
                    type="password"
                    autoComplete="current-password"
                    aria-label={t('password')}
                    aria-describedby="password-helper-text"
                />
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  fullWidth
                  disabled={isSubmitting}
                  sx={{
                    mt: 3,
                    borderRadius: '2rem',
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
                  }}
                >
                  {submitLabel || t('submit')}
                </Button>
              </Form>
            )}
          </Formik>
        </CardContent>
      </Card>
    </Box>
  );
};