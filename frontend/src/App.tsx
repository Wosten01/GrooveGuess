
import { ThemeProvider } from "@mui/material/styles";
import { theme } from "./theme";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { WelcomePage, LoginPage, RegisterPage, AdminTrackPanel, AdminTrackTable, QuizPanel, QuizTable } from "./pages";
import { AppBar } from "./components";
import { AuthProvider } from "./context/AuthContext";
import { Toolbar } from "@mui/material";

function App() {

  return (
    <AuthProvider>
      <ThemeProvider theme={theme}>
        <BrowserRouter>
          <AppBar />
          <Toolbar />
          <Routes>
            <Route path="/" element={<WelcomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/admin/tracks">
              <Route path="details" element={<AdminTrackPanel />} />
              <Route path="table" element={<AdminTrackTable />} />
            </Route>
            <Route path="/admin/quizzes">
              <Route path="details" element={<QuizPanel />} />
              <Route path="table" element={<QuizTable />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </ThemeProvider>
    </AuthProvider>
  );
}

export default App;
