import {
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  InputAdornment,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import { useTranslation } from "react-i18next";
import { TranslationNamespace } from "../../i18n";
import { useState } from "react";
import { PaginatedTable } from "../../components";
import { getScoreboardUsers } from "../../api/user-api";
import { useAuth } from "../../hooks/auth-context";

interface User {
  id: number;
  username: string;
  email: string;
  score: number;
}

type TableColumn<T> = {
  label: React.ReactNode;
  render: (row: T, idx: number) => React.ReactNode;
  align?: "left" | "center" | "right";
};

export const Scoreboard = () => {
  const { t } = useTranslation(TranslationNamespace.Common, {
    keyPrefix: "pages.scoreboard",
  });

  const [search, setSearch] = useState("");
  const { user: currentUser } = useAuth();

  const columns: TableColumn<User>[] = [
    {
      label: t("place"),
      render: (_row, idx) => <span>{idx + 1}</span>,
    },
    {
      label: t("username"),
      render: (row) => (
        <span style={{ display: "flex", alignItems: "center", gap: 4 }}>
          {row.username}
          {currentUser?.id === row.id && (
            <AccountCircleIcon
              fontSize="small"
              color="primary"
              titleAccess={t("you")}
              sx={{ ml: 0.5 }}
            />
          )}
        </span>
      ),
    },
    { label: t("email"), render: (row) => row.email },
    { label: t("score"), render: (row) => row.score, align: "right" },
  ];

  const fetchData = (page: number, size: number) =>
    getScoreboardUsers(page, size, search);

  return (
    <>
      <Card
        sx={{
          maxWidth: 1200,
          width: "100%",
          borderRadius: "1.5rem",
          boxShadow: "0 8px 24px rgba(76, 175, 80, 0.10)",
          fontFamily: (theme) => theme.typography.fontFamily,
        }}
      >
        <CardContent>
          <Typography
            variant="h4"
            align="center"
            sx={{
              color: (theme) => theme.palette.primary.dark,
              marginBottom: "1rem",
            }}
          >
            {t("scoreboardTitle")}
          </Typography>
          <Box sx={{ mb: 2, display: "flex", justifyContent: "flex-end" }}>
            <TextField
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder={t("searchUser")}
              size="small"
              sx={{ width: 320 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon color="action" />
                  </InputAdornment>
                ),
              }}
              variant="outlined"
            />
          </Box>
          <PaginatedTable<User> fetchData={fetchData} defaultRowsPerPage={10}>
            {({ data, loading, error, pagination, page, rowsPerPage }) => (
              <>
                {loading ? (
                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "center",
                      margin: "2rem 0",
                    }}
                  >
                    <CircularProgress />
                  </Box>
                ) : error ? (
                  <Alert severity="error">{error}</Alert>
                ) : (
                  <TableContainer component={Paper} sx={{ borderRadius: 2 }}>
                    <Table>
                      <TableHead>
                        <TableRow>
                          {columns.map((col, idx) => (
                            <TableCell key={idx} align={col.align || "center"}>
                              {col.label}
                            </TableCell>
                          ))}
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {!data || data.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={columns.length} align="center">
                              {t("noUsers")}
                            </TableCell>
                          </TableRow>
                        ) : (
                          (data || []).map((row, idx) => (
                            <TableRow key={row.id}>
                              {columns.map((col, colIdx) => (
                                <TableCell
                                  key={colIdx}
                                  align={col.align || "center"}
                                >
                                  {col.render(row, page * rowsPerPage + idx)}
                                </TableCell>
                              ))}
                            </TableRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
                <Box sx={{ display: "flex", justifyContent: "center", mt: 2 }}>
                  {pagination}
                </Box>
              </>
            )}
          </PaginatedTable>
        </CardContent>
      </Card>
    </>
  );
};

export default Scoreboard;
