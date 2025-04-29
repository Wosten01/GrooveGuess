
import { useState, useEffect } from "react";
import {
  Card,
  CardContent,
  Typography,
  Button,
  Divider,
  TextField,
  useTheme,
  CircularProgress,
  Alert,
  Box,
  IconButton,
} from "@mui/material";
import QuizIcon from "@mui/icons-material/Quiz";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import { useTranslation } from "react-i18next";
import { TranslationNamespace } from "../../../../i18n";
import { Formik, Form, FormikHelpers } from "formik";
import * as Yup from "yup";
import { useAuth } from "../../../../hooks/auth-context";
import { useLocation, useNavigate } from "react-router-dom";
import {
  createQuiz,
  getQuizzes,
  Quiz,
  updateQuiz,
} from "./../../../../api/quiz-api";
import { ApiResponse } from "../../../../api";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { Track } from "../../../../api/tracks-api";
import { TrackSelector } from "./TrackSelector";

type QuizFormValues = {
  title: string;
  description: string;
  roundCount: number;
  tracks: Track[];
};

export const QuizPanel = ({ onSuccess }: { onSuccess?: () => void }) => {
  const { t } = useTranslation(TranslationNamespace.Common, {
    keyPrefix: "pages.admin.quizzes.details",
  });
  const theme = useTheme();
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [initialValues, setInitialValues] = useState<QuizFormValues>({
    title: "",
    description: "",
    roundCount: 2,
    tracks: [],
  });
  const [loadingQuiz, setLoadingQuiz] = useState(false);

  const searchParams = new URLSearchParams(location.search);
  const quizId = Number(searchParams.get("id"));
  const isEdit = Boolean(quizId);

  useEffect(() => {
    if (isEdit && quizId) {
      setLoadingQuiz(true);
      getQuizzes()
        .then((quizzes) => {
          const quiz = quizzes.content.find(
            (q) => String(q.id) === String(quizId)
          );
          if (quiz) {
            setInitialValues({
              title: quiz.title,
              description: quiz.description || "",
              roundCount: quiz.roundCount || 1,
              tracks: quiz.tracks ?? [],
            });
          } else {
            setError(t("quizNotFound"));
          }
        })
        .catch(() => setError(t("fetchError")))
        .finally(() => setLoadingQuiz(false));
    }
  }, [isEdit, t, quizId]);

  const validationSchema = Yup.object({
    title: Yup.string().required(t("fillAllFields")),
    description: Yup.string().required(t("fillAllFields")),
    roundCount: Yup.number()
      .min(1, t("invalidRoundCount"))
      .required(t("fillAllFields")),
    tracks: Yup.array().min(1, t("fillAllFields")),
  });

  const handleSubmit = async (
    values: QuizFormValues,
    actions: FormikHelpers<QuizFormValues>
  ) => {
    setSuccess(null);
    setError(null);

    if (!user?.id) {
      setError(t("noUserId"));
      actions.setSubmitting(false);
      return;
    }

    let response: ApiResponse<Quiz>;

    try {
      const trackIds = values.tracks.map((track) => Number(track.id));
      if (isEdit && quizId) {
        response = await updateQuiz(
          quizId,
          {
            title: values.title,
            description: values.description,
            roundCount: values.roundCount,
            trackIds,
          },
          user.id
        );
      } else {
        response = await createQuiz(
          {
            title: values.title,
            description: values.description,
            roundCount: values.roundCount,
            trackIds,
          },
          user.id
        );
      }

      if (response && response.status < 300) {
        setSuccess(isEdit ? t("quizUpdated") : t("quizAdded"));
        if (onSuccess) onSuccess();
      } else if (response.status === 403) {
        setError(t("adminOnly"));
      } else {
        setError(isEdit ? t("editError") : t("addError"));
      }
    } catch {
      setError(t("networkError"));
    } finally {
      actions.setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: `linear-gradient(135deg, ${theme.palette.accent.light} 0%, ${theme.palette.pastel.main} 100%)`,
        padding: "2rem",
        fontFamily: theme.typography.fontFamily,
      }}
    >
      <Card
        sx={{
          maxWidth: 520,
          width: "100%",
          borderRadius: "1.5rem",
          boxShadow: "0 8px 24px rgba(76, 175, 80, 0.10)",
          transition: "transform 0.3s ease, box-shadow 0.3s ease",
          fontFamily: theme.typography.fontFamily,
          "&:hover": {
            transform: "translateY(-8px)",
            boxShadow: "0 12px 32px rgba(76, 175, 80, 0.15)",
          },
        }}
      >
        <Box
          sx={{ width: "100%", display: "flex", alignItems: "center", m: 2 }}
        >
          <IconButton
            onClick={() => navigate("/admin/quizzes/table")}
            sx={{ mr: 1 }}
          >
            <ArrowBackIcon />
          </IconButton>
        </Box>
        <CardContent
          sx={{
            padding: "2.5rem",
            paddingTop: "0.5rem",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: 2,
            fontFamily: theme.typography.fontFamily,
          }}
        >
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              width: 90,
              height: 90,
              borderRadius: "50%",
              background: `linear-gradient(45deg, ${theme.palette.accent.main}, ${theme.palette.primary.light})`,
              boxShadow: "0 4px 12px rgba(76, 175, 80, 0.08)",
              marginBottom: "1rem",
            }}
            className="icon-container"
          >
            <QuizIcon
              sx={{
                fontSize: { xs: 36, sm: 44, md: 50, lg: 60 },
                color: theme.palette.primary.main,
              }}
            />
          </div>
          <Typography
            variant="h4"
            align="center"
            sx={{
              color: theme.palette.primary.dark,
              marginBottom: "0.5rem",
            }}
          >
            {isEdit ? t("adminPanelEditQuizTitle") : t("adminPanelQuizTitle")}
          </Typography>
          <Typography
            variant="body1"
            align="center"
            sx={{
              color: theme.palette.secondary.dark,
              maxWidth: 380,
              marginBottom: "1rem",
            }}
          >
            {isEdit ? t("adminPanelEditQuizDesc") : t("adminPanelQuizDesc")}
          </Typography>
          <Divider
            sx={{
              width: 80,
              borderColor: theme.palette.accent.main,
              marginBottom: "1.5rem",
            }}
          />
          {loadingQuiz && isEdit ? (
            <CircularProgress />
          ) : (
            <Formik
              initialValues={initialValues}
              enableReinitialize
              validationSchema={validationSchema}
              onSubmit={handleSubmit}
            >
              {({
                isSubmitting,
                errors,
                touched,
                handleChange,
                handleBlur,
                values,
                setFieldValue,
              }) => (
                <Form
                  style={{
                    width: "100%",
                    display: "flex",
                    flexDirection: "column",
                    gap: "1.2rem",
                    alignItems: "center",
                  }}
                  autoComplete="off"
                >
                  <TextField
                    name="title"
                    label={t("quizTitle")}
                    value={values.title}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    fullWidth
                    autoFocus
                    required
                    variant="outlined"
                    error={touched.title && Boolean(errors.title)}
                    helperText={touched.title && errors.title}
                  />
                  <TextField
                    name="description"
                    label={t("quizDescription")}
                    value={values.description}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    fullWidth
                    required
                    variant="outlined"
                    error={touched.description && Boolean(errors.description)}
                    helperText={touched.description && errors.description}
                  />
                  <TextField
                    name="roundCount"
                    label={t("quizRoundCount")}
                    value={values.roundCount}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    fullWidth
                    required
                    variant="outlined"
                    type="number"
                    inputProps={{ min: 1 }}
                    error={touched.roundCount && Boolean(errors.roundCount)}
                    helperText={touched.roundCount && errors.roundCount}
                  />

                  <TrackSelector
                    selectedTracks={values.tracks}
                    onChange={(tracks) => setFieldValue("tracks", tracks)}
                  />

                  <Button
                    type="submit"
                    variant="contained"
                    endIcon={
                      isSubmitting ? (
                        <CircularProgress size={20} color="inherit" />
                      ) : isEdit ? (
                        <EditIcon />
                      ) : (
                        <AddIcon />
                      )
                    }
                    size="large"
                    sx={{
                      padding: "0.75rem 2rem",
                      borderRadius: "2rem",
                      background: `linear-gradient(45deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
                      boxShadow: "0 4px 12px rgba(38, 166, 154, 0.18)",
                      fontFamily: theme.typography.fontFamily,
                      "&:hover": {
                        background: `linear-gradient(45deg, ${theme.palette.primary.dark}, ${theme.palette.secondary.dark})`,
                        transform: "scale(1.05)",
                      },
                      marginTop: "0.5rem",
                    }}
                    disabled={isSubmitting}
                  >
                    {isEdit ? t("editQuiz") : t("addQuiz")}
                  </Button>
                </Form>
              )}
            </Formik>
          )}
          {success && (
            <Alert severity="success" sx={{ mt: 2, width: "100%" }}>
              {success}
            </Alert>
          )}
          {error && (
            <Alert severity="error" sx={{ mt: 2, width: "100%" }}>
              {error}
            </Alert>
          )}
        </CardContent>
      </Card>
    </div>
  );
};
