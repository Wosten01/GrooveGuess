import { useTheme } from '@mui/material/styles';
import { Card, CardContent, Typography, Box } from '@mui/material';
import { Formik, Form } from 'formik';
import { useTranslation } from 'react-i18next';
import { getRegisterSchema } from './RegisterScheme';
import { FormikTextField , FormikSubmitButton} from '../../components';

export type RegisterFormValues = {
  username: string;
  email: string;
  password: string;
};

export type RegisterFormProps = {
  onSubmit: (values: RegisterFormValues) => Promise<void> | void;
  initialValues?: RegisterFormValues;
  title?: string;
  submitLabel?: string;
};

export const RegisterForm = ({
  onSubmit,
  initialValues = { username: '', email: '', password: '' },
  title,
  submitLabel,
}: RegisterFormProps) => {
  const theme = useTheme();
  const { t } = useTranslation(undefined, { keyPrefix: 'pages.register' });

  const validationSchema = getRegisterSchema(t);

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
                  name="username"
                  label={t('username')}
                  autoComplete="username"
                  aria-label={t('username')}
                  aria-describedby="username-helper-text"
                />
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
                  autoComplete="new-password"
                  aria-label={t('password')}
                  aria-describedby="password-helper-text"
                />
                <FormikSubmitButton
                  loading={isSubmitting}
                  label={submitLabel || t('submit')}
                />
              </Form>
            )}
          </Formik>
        </CardContent>
      </Card>
    </Box>
  );
};