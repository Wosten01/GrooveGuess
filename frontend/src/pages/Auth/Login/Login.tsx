import { LoginForm, LoginFormValues } from "../../../components";

export const LoginPage = () => {
    const handleLogin = async (values: LoginFormValues) => {
      alert(JSON.stringify(values, null, 2));
    };
  
    return <LoginForm onSubmit={handleLogin} />;
  };
  