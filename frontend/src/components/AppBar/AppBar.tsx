import React from "react";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Avatar from "@mui/material/Avatar";
import Button from "@mui/material/Button";
import MusicNoteIcon from "@mui/icons-material/MusicNote";
import { useNavigate, useLocation } from "react-router-dom";
import { Box, useTheme } from "@mui/material";
import { useTranslation } from "react-i18next";

const mockUser = {
  isAuthenticated: true,
  name: "Иван",
  avatarUrl: "",
};

export const MainAppBar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const user = mockUser;
  const { t } = useTranslation();
  const theme = useTheme();

  const isLogin = location.pathname === "/login";
  const isRegister = location.pathname === "/register";

  return (
    <AppBar
      color="transparent"
      sx={{ background: theme.palette.background.paper }}
    >
      <Toolbar sx={{ justifyContent: "space-between" }}>
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            gap: 1,
            cursor: "pointer",
            userSelect: "none",
          }}
          onClick={() => navigate("/")}
        >
          <MusicNoteIcon color="primary" sx={{ fontSize: 32 }} />
          <Typography variant="h6" color="primary" sx={{ fontWeight: 700 }}>
            {t("features.logo.name")}
          </Typography>
        </Box>
        {user.isAuthenticated ? (
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              gap: 1,
              cursor: "pointer",
              userSelect: "none",
            }}
          >
            <Typography variant="body1" color="primary">
              {user.name}
            </Typography>
            <Avatar src={user.avatarUrl}>{user.name[0]}</Avatar>
          </Box>
        ) : (
          <Box sx={{ display: "flex", gap: 1 }}>
            <Button
              color="primary"
              variant={isLogin ? "contained" : "outlined"}
              onClick={() => navigate("/login")}
              sx={
                isLogin
                  ? {
                      backgroundColor: theme.palette.pastel.main,
                      "&:hover": {
                        backgroundColor: theme.palette.pastel.dark,
                      },
                    }
                  : {}
              }
            >
              {t("pages.login.submit")}
            </Button>
            <Button
              color="primary"
              variant={isRegister ? "contained" : "outlined"}
              onClick={() => navigate("/register")}
              sx={
                isRegister
                  ? {
                      backgroundColor: theme.palette.pastel.main,
                      color: theme.palette.primary.main,
                      "&:hover": {
                        backgroundColor: theme.palette.pastel.light,
                      },
                    }
                  : {}
              }
            >
              {t("pages.register.submit")}
            </Button>
          </Box>
        )}
      </Toolbar>
    </AppBar>
  );
};
