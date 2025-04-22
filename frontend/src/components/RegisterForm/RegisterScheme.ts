import * as Yup from 'yup';

export const getRegisterSchema = (t: (key: string) => string) =>
  Yup.object().shape({
    username: Yup.string()
      .min(3, t('errors.usernameMin'))
      .required(t('errors.usernameRequired')),
    email: Yup.string()
      .email(t('errors.email'))
      .required(t('errors.emailRequired')),
    password: Yup.string()
      .min(6, t('errors.passwordMin'))
      .required(t('errors.passwordRequired')),
  });