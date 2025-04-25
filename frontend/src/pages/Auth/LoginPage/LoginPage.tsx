import { useNavigate } from "react-router-dom";
import { loginUser } from "../../../api/auth-api";
import { LoginForm, LoginFormValues } from "../../../components";
import { theme } from "../../../theme";
import { useAuth } from "../../../hooks/auth-context";

export const LoginPage = () => {
  const navigate = useNavigate()
  const {fetchUser} = useAuth()


  const handleLogin = async (values: LoginFormValues) => {
    const response = await loginUser(values);
    
    console.debug(response)

    if (response.status === 200) {
      navigate("/");
      fetchUser()
    } else {
      //  handle error 
    }
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
