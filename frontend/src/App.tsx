
import { ThemeProvider } from "@mui/material/styles";
import { theme } from "./theme";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { WelcomePage, LoginPage, RegisterPage } from "./pages";
import { AppBar } from "./components";
import { AuthProvider } from "./context/AuthContext";
import { AdminTrackPanel } from "./pages/Admin/AdminTrackPanel/AdminTrackPanel";
import { Toolbar } from "@mui/material";
import { AdminTrackTable } from "./pages/Admin/AdminTrackTable/AdminTrackTable";

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
          </Routes>
        </BrowserRouter>
      </ThemeProvider>
    </AuthProvider>
  );
}

export default App;
