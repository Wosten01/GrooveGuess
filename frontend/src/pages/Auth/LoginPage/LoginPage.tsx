import { LoginForm, LoginFormValues } from "../../../components";
import { theme } from "../../../theme";

export const LoginPage = () => {
  const handleLogin = async (values: LoginFormValues) => {
    alert(JSON.stringify(values, null, 2));
  };

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        minHeight: "100vh",
        background: `linear-gradient(135deg, ${theme.palette.accent.light} 0%, ${theme.palette.pastel.main} 100%)`,
        fontFamily: theme.typography.fontFamily,
      }}
    >
      <LoginForm onSubmit={handleLogin} />
    </div>
  );
};
