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
  GameStats,
  Profile,
  Scoreboard,
} from "./pages";
import { AppBar, ProtectedRoute } from "./components";
import { AuthProvider } from "./context/AuthContext";
import { Toolbar } from "@mui/material";
import { QuizFeed } from "./pages";

function App() {
  return (
    <AuthProvider>
      <ThemeProvider theme={theme}>
        <BrowserRouter>
          <AppBar />
          <Toolbar />
          <Routes>
            <Route path="/" element={<WelcomePage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />

            <Route path="game">
              <Route
                path="player/:userId/session/:sessionId"
                element={
                  <ProtectedRoute>
                    <Game />
                  </ProtectedRoute>
                }
              />
              <Route
                path="player/:userId/session/:sessionId/results"
                element={
                  <ProtectedRoute>
                    <GameResults />
                  </ProtectedRoute>
                }
              />
            </Route>

            <Route path="quizzes">
              <Route
                index
                element={
                  <ProtectedRoute>
                    <QuizFeed />
                  </ProtectedRoute>
                }
              />
            </Route>

            <Route path="admin">
              <Route path="tracks">
                <Route
                  path="details"
                  element={
                    <ProtectedRoute requireAdmin={true}>
                      <TrackPanel />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="table"
                  element={
                    <ProtectedRoute requireAdmin={true}>
                      <TrackTable />
                    </ProtectedRoute>
                  }
                />
                </Route>
                <Route path="quizzes">
                  <Route
                    path="details"
                    element={
                      <ProtectedRoute requireAdmin={true}>
                        <QuizPanel />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="table"
                    element={
                      <ProtectedRoute requireAdmin={true}>
                        <QuizTable />
                      </ProtectedRoute>
                    }
                  />
                </Route>
              </Route>

            <Route path="/scoreboard" element={<Scoreboard />} />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <Profile />
                </ProtectedRoute>
              }
            />
            <Route
              path="/results"
              element={
                <ProtectedRoute>
                  <GameStats />
                </ProtectedRoute>
              }
            />
          </Routes>
        </BrowserRouter>
      </ThemeProvider>
    </AuthProvider>
  );
}

export default App;
