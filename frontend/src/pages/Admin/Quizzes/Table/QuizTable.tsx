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
  Chip,
  alpha,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import ViewListIcon from "@mui/icons-material/ViewList";
import { useTranslation } from "react-i18next";
import { TranslationNamespace } from "../../../../i18n";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../../hooks/auth-context";
import { deleteQuiz, getQuizzes, Quiz } from "../../../../api/quiz-api";
import { DialogConfirm, PaginatedTable, Table, TableActions } from "../../../../components";
import { QuizTracksCell } from "./QuizTracksCell";
import { motion } from "framer-motion";

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

  const handleDeleteConfirm = async (refresh: () => void) => {
    if (!quizToDelete || !user?.id) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteQuiz(quizToDelete.id, user.id);
      setDeleteDialogOpen(false);
      setQuizToDelete(null);
      refresh();
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
    { 
      label: t("quizTitle"), 
      render: (row) => (
        <Typography 
          variant="subtitle1" 
          sx={{ 
            fontWeight: 600,
            color: theme.palette.primary.main
          }}
        >
          {row.title}
        </Typography>
      ) 
    },
    { 
      label: t("quizDescription"), 
      render: (row) => (
        <Tooltip title={row.description} placement="top-start">
          <Typography 
            variant="body2" 
            sx={{ 
              maxWidth: 250,
              overflow: "hidden",
              textOverflow: "ellipsis",
              whiteSpace: "nowrap",
              color: alpha(theme.palette.text.primary, 0.8)
            }}
          >
            {row.description}
          </Typography>
        </Tooltip>
      ) 
    },
    { 
      label: t("quizRoundCount"), 
      align: "center",
      render: (row) => (
        <Chip 
          label={row.roundCount} 
          size="small"
          color="primary"
          variant="outlined"
          sx={{ 
            fontWeight: 600,
            minWidth: "60px"
          }}
        />
      ) 
    },
    {
      label: t("quizTracks"),
      render: (row) => <QuizTracksCell quiz={row} />,
    },
  ];

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <Card
        sx={{
          maxWidth: 1200,
          width: "100%",
          borderRadius: "1.5rem",
          boxShadow: `0 10px 30px ${alpha(theme.palette.primary.main, 0.1)}`,
          fontFamily: theme.typography.fontFamily,
          overflow: "visible",
          background: `linear-gradient(to bottom, ${alpha(theme.palette.background.paper, 0.9)}, ${theme.palette.background.paper})`,
          backdropFilter: "blur(10px)",
          border: `1px solid ${alpha(theme.palette.primary.main, 0.1)}`,
        }}
      >
        <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
          <Box sx={{ 
            display: "flex", 
            alignItems: "center", 
            justifyContent: "center",
            mb: 3,
            pb: 2,
            borderBottom: `1px solid ${alpha(theme.palette.divider, 0.6)}`
          }}>
            <ViewListIcon 
              sx={{ 
                color: theme.palette.primary.main, 
                fontSize: "2rem",
                mr: 1.5 
              }} 
            />
            <Typography
              variant="h4"
              sx={{
                color: theme.palette.primary.main,
                fontWeight: 700,
                fontSize: { xs: "1.5rem", sm: "1.75rem", md: "2rem" }
              }}
            >
              {t("adminPanelQuizzesTitle")}
            </Typography>
          </Box>
          
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
                    <CircularProgress size={40} thickness={4} />
                  </Box>
                ) : error ? (
                  <Alert 
                    severity="error" 
                    variant="filled"
                    sx={{ 
                      borderRadius: "0.75rem",
                      mb: 2
                    }}
                  >
                    {error}
                  </Alert>
                ) : (
                  <Box sx={{ 
                    borderRadius: "1rem", 
                    overflow: "hidden",
                    boxShadow: `0 4px 20px ${alpha(theme.palette.common.black, 0.05)}`
                  }}>
                    <Table
                      rows={data}
                      columns={columns}
                      actions={(quiz) => (
                        <Box sx={{ display: "flex", gap: 1 }}>
                          <Tooltip title={t("editQuiz")} arrow>
                            <IconButton 
                              onClick={() => handleEdit(quiz)}
                              sx={{ 
                                color: theme.palette.primary.main,
                                backgroundColor: alpha(theme.palette.primary.main, 0.1),
                                '&:hover': {
                                  backgroundColor: alpha(theme.palette.primary.main, 0.2),
                                }
                              }}
                              size="small"
                            >
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title={t("deleteQuiz")} arrow>
                            <IconButton
                              color="error"
                              onClick={() => handleDeleteClick(quiz)}
                              sx={{ 
                                backgroundColor: alpha(theme.palette.error.main, 0.1),
                                '&:hover': {
                                  backgroundColor: alpha(theme.palette.error.main, 0.2),
                                }
                              }}
                              size="small"
                            >
                              <DeleteIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        </Box>
                      )}
                      emptyMessage={t("noQuizzes")}
                      pagination={pagination}
                    />
                  </Box>
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
    </motion.div>
  );
};