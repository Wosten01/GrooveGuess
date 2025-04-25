import { useNavigate } from "react-router-dom";
import { registerUser } from "../../../api/auth-api";
import {
  RegisterForm,
  RegisterFormValues,
} from "../../../components/RegisterForm/RegisterForm";
import { theme } from "../../../theme";

export const RegisterPage = () => {
  const navigate = useNavigate()

  const handleRegister = async (values: RegisterFormValues) => {
    const response = await registerUser(values);
    
    console.debug(response)

    if (response.status === 200) {
      navigate("/login");
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
      <RegisterForm onSubmit={handleRegister} />
    </div>
  );
};
