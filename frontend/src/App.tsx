
import { ThemeProvider } from "@mui/material/styles";
import { theme } from "./theme";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import {
  WelcomePage,
  LoginPage,
  RegisterPage,
  TrackPanel,
  TrackTable,
  QuizPanel,
  QuizTable,
  Game,
  GameResults,
} from "./pages";
import { AppBar } from "./components";
import { AuthProvider } from "./context/AuthContext";
import { Toolbar } from "@mui/material";
import { Scoreboard } from "./pages/Scoreboard";
import { QuizFeed } from "./pages"; 
import { Profile } from "./pages/Profile/Profile";


function App() {
  return (
    <AuthProvider>
      <ThemeProvider theme={theme}>
        <BrowserRouter>
          <AppBar />
          <Toolbar />
          <Routes>
            <Route path="/" element={<WelcomePage />} />
            <Route>
              <Route path="login" element={<LoginPage />} />
              <Route path="register" element={<RegisterPage />} />
            </Route>

            <Route path="quizzes">
              <Route index element={<QuizFeed />} />
              <Route path="details" element={<QuizPanel />} />
              <Route path="table" element={<QuizTable />} />
            </Route>

            <Route path="game">
              <Route path="player/:userId/session/:sessionId" element={<Game />} />
              <Route path="player/:userId/session/:sessionId/results" element={<GameResults />} />
              {/* <Route path="player/:userId/session/:sessionId/lobby" element={<GameLobby />} /> */}
            </Route>

            <Route path="admin">
              <Route path="tracks">
                <Route path="details" element={<TrackPanel />} />
                <Route path="table" element={<TrackTable />} />
              </Route>

              <Route path="quizzes">
                <Route path="details" element={<QuizPanel />} />
                <Route path="table" element={<QuizTable />} />
              </Route>

            </Route>
            <Route path="/scoreboard" element={<Scoreboard />} />
            <Route path="/profile" element={<Profile />} />
          </Routes>
        </BrowserRouter>
      </ThemeProvider>
    </AuthProvider>
  );
}

export default App;
