import React from "react";
import MUIAppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Avatar from "@mui/material/Avatar";
import Button from "@mui/material/Button";
import MusicNoteIcon from "@mui/icons-material/MusicNote";
import LogoutOutlinedIcon from "@mui/icons-material/LogoutOutlined";
import IconButton from "@mui/material/IconButton";
import Tooltip from "@mui/material/Tooltip";
import { useNavigate, useLocation } from "react-router-dom";
import { Box, useTheme } from "@mui/material";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../hooks/auth-context";
import { NavButton } from "./NavButton";
import TableChartIcon from "@mui/icons-material/TableChart";

export const AppBar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();
  const theme = useTheme();

  const { logout, user } = useAuth();

  const isLogin = location.pathname === "/login";
  const isRegister = location.pathname === "/register";

  const handleLogout = () => {
    logout();
  };

  return (
    <MUIAppBar
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
        {user ? (
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              gap: 1.5,
              userSelect: "none",
            }}
          >
            <Box sx={{ display: "flex", alignItems: "center", gap: 1.5 }}>
              <NavButton
                allowedRoles={["ADMIN"]}
                to="/admin/tracks/table"
                label={t("pages.admin.tracks.table.adminPanelTracksTitle")}
                icon={<TableChartIcon />}
              />
            </Box>

            <Tooltip
              title={
                <Typography variant="body1" color="info">
                  {user.username}
                </Typography>
              }
            >
              <Avatar src={""}>{user.username[0]}</Avatar>
            </Tooltip>

            <Tooltip title={t("features.logout.title")}>
              <IconButton
                aria-label={t("features.logout.title")}
                onClick={handleLogout}
                color="primary"
                sx={{ ml: 1 }}
              >
                <LogoutOutlinedIcon />
              </IconButton>
            </Tooltip>
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
    </MUIAppBar>
  );
};
