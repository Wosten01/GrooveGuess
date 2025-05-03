
import { useState } from "react";
import {
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  useTheme,
  Box,
  IconButton,
  Tooltip,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import { useTranslation } from "react-i18next";
import { TranslationNamespace } from "../../../../i18n";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../../hooks/auth-context";
import { deleteQuiz, getQuizzes, Quiz } from "../../../../api/quiz-api";
import { DialogConfirm, PaginatedTable, Table, TableActions } from "../../../../components";
import { QuizTracksCell } from "./QuizTracksCell";

type TableColumn<T> = {
  label: React.ReactNode;
  render: (row: T) => React.ReactNode;
  align?: "left" | "center" | "right";
};

export const QuizTable = () => {
  const { t } = useTranslation(TranslationNamespace.Common, {
    keyPrefix: "pages.admin.quizzes.table",
  });
  const theme = useTheme();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [quizToDelete, setQuizToDelete] = useState<Quiz | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  const handleCreate = () => {
    navigate("/admin/quizzes/details");
  };

  const handleEdit = (quiz: Quiz) => {
    navigate(`/admin/quizzes/details?id=${quiz.id}`);
  };

  const handleDeleteClick = (quiz: Quiz) => {
    setQuizToDelete(quiz);
    setDeleteDialogOpen(true);
    setDeleteError(null);
  };

  // Обработчик удаления теперь принимает refresh как аргумент
  const handleDeleteConfirm = async (refresh: () => void) => {
    if (!quizToDelete || !user?.id) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteQuiz(quizToDelete.id, user.id);
      setDeleteDialogOpen(false);
      setQuizToDelete(null);
      refresh(); // обновляем таблицу после удаления
    } catch (e: unknown) {
      if (e instanceof Error) {
        setDeleteError(e.message || t("deleteError"));
      } else {
        setDeleteError(t("deleteError"));
      }
    } finally {
      setDeleting(false);
    }
  };

  const handleDeleteCancel = () => {
    setDeleteDialogOpen(false);
    setQuizToDelete(null);
    setDeleteError(null);
  };

  const columns: TableColumn<Quiz>[] = [
    { label: t("quizTitle"), render: (row) => row.title },
    { label: t("quizDescription"), render: (row) => row.description },
    { label: t("quizRoundCount"), render: (row) => row.roundCount },
    {
      label: t("quizTracks"),
      render: (row) => <QuizTracksCell quiz={row} />,
    },
  ];

  return (
      <Card
        sx={{
          maxWidth: 1200,
          width: "100%",
          borderRadius: "1.5rem",
          boxShadow: "0 8px 24px rgba(76, 175, 80, 0.10)",
          fontFamily: theme.typography.fontFamily,
        }}
      >
        <CardContent>
          <Typography
            variant="h4"
            align="center"
            sx={{
              color: theme.palette.primary.dark,
              marginBottom: "1rem",
            }}
          >
            {t("adminPanelQuizzesTitle")}
          </Typography>
          <PaginatedTable<Quiz>
            fetchData={getQuizzes}
            defaultRowsPerPage={10}
          >
            {({ data, loading, error, pagination, refresh }) => (
              <>
                <TableActions
                  onCreate={handleCreate}
                  onRefresh={refresh}
                  loading={loading}
                  createLabel={t("createQuiz")}
                  refreshLabel={t("refresh")}
                />
                {loading ? (
                  <Box sx={{ display: "flex", justifyContent: "center", margin: "2rem 0" }}>
                    <CircularProgress />
                  </Box>
                ) : error ? (
                  <Alert severity="error">{error}</Alert>
                ) : (
                  <Table
                    rows={data}
                    columns={columns}
                    actions={(quiz) => (
                      <>
                        <Tooltip title={t("editQuiz")}>
                          <IconButton onClick={() => handleEdit(quiz)}>
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={t("deleteQuiz")}>
                          <IconButton
                            color="error"
                            onClick={() => handleDeleteClick(quiz)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Tooltip>
                      </>
                    )}
                    emptyMessage={t("noQuizzes")}
                    pagination={pagination}
                  />
                )}
                <DialogConfirm
                  open={deleteDialogOpen}
                  onClose={handleDeleteCancel}
                  onConfirm={() => handleDeleteConfirm(refresh)}
                  loading={deleting}
                  error={deleteError}
                  title={t("deleteQuizTitle")}
                  confirmText={t("delete")}
                  cancelText={t("cancel")}
                  loadingText={t("deleting")}
                  dialogText={t("deleteQuizConfirm")}
                  confirmColor="error"
                />
              </>
            )}
          </PaginatedTable>
        </CardContent>
      </Card>
  );
};
