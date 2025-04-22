import * as Yup from 'yup';

export const getLoginSchema = (t: (key: string) => string) =>
  Yup.object().shape({
    email: Yup.string()
      .email(t('errors.email'))
      .required(t('errors.emailRequired')),
    password: Yup.string()
      .min(6, t('errors.passwordMin'))
      .required(t('errors.passwordRequired')),
  });