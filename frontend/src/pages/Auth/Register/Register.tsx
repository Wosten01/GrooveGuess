import { RegisterForm, RegisterFormValues } from '../../../components/RegisterForm/RegisterForm';

export const RegisterPage = () => {
  const handleRegister = async (values: RegisterFormValues) => {
    alert(JSON.stringify(values, null, 2));
  };

  return <RegisterForm onSubmit={handleRegister} />;
};