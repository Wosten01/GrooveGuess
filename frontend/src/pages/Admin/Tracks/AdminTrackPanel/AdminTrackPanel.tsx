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
import LibraryMusicIcon from "@mui/icons-material/LibraryMusic";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import { useTranslation } from "react-i18next";
import { Formik, Form, FormikHelpers } from "formik";
import * as Yup from "yup";
import { useLocation, useNavigate } from "react-router-dom";

import AudioPlayer from "react-h5-audio-player";
import "react-h5-audio-player/lib/styles.css";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { TranslationNamespace } from "../../../../i18n";
import { useAuth } from "../../../../hooks/auth-context";
import { createTrack, getTracks, Track, updateTrack } from "../../../../api/tracks-api";
import { ApiResponse } from "../../../../api";

type TrackFormValues = {
  title: string;
  artist: string;
  url: string;
};

export const AdminTrackPanel = ({ onSuccess }: { onSuccess?: () => void }) => {
  const { t } = useTranslation(TranslationNamespace.Common, {
    keyPrefix: "pages.admin.tracks.details",
  });
  const theme = useTheme();
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [initialValues, setInitialValues] = useState<TrackFormValues>({
    title: "",
    artist: "",
    url: "",
  });
  const [loadingTrack, setLoadingTrack] = useState(false);

  const searchParams = new URLSearchParams(location.search);
  const trackId = searchParams.get("id");
  const isEdit = Boolean(trackId);

  useEffect(() => {
    if (isEdit && trackId) {
      setLoadingTrack(true);
      getTracks()
        .then((tracks) => {
          const track = tracks.content.find((t) => String(t.id) === String(trackId));
          if (track) {
            setInitialValues({
              title: track.title,
              artist: track.artist,
              url: track.url,
            });
          } else {
            setError(t("trackNotFound"));
          }
        })
        .catch(() => setError(t("fetchError")))
        .finally(() => setLoadingTrack(false));
    }
  }, [isEdit, t, trackId]);

  const validationSchema = Yup.object({
    title: Yup.string().required(t("fillAllFields")),
    artist: Yup.string().required(t("fillAllFields")),
    url: Yup.string().url(t("invalidUrl")).required(t("fillAllFields")),
  });

  const handleSubmit = async (
    values: TrackFormValues,
    actions: FormikHelpers<TrackFormValues>
  ) => {
    setSuccess(null);
    setError(null);

    if (!user?.id) {
      setError(t("noUserId"));
      actions.setSubmitting(false);
      return;
    }

    let response: ApiResponse<Track>;

    try {
      if (isEdit && trackId) {
        response = await updateTrack(
          trackId,
          {
            title: values.title,
            artist: values.artist,
            url: values.url,
          },
          user.id
        );
      } else {
        response = await createTrack(
          {
            title: values.title,
            artist: values.artist,
            url: values.url,
          },
          user.id
        );
      }

      if (response && response.status < 300) {
        setSuccess(isEdit ? t("trackUpdated") : t("trackAdded"));
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
        minHeight: "100vh",
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
            onClick={() => navigate("/admin/tracks/table")}
            sx={{ mr: 1 }}
          >
            <ArrowBackIcon />
          </IconButton>
        </Box>
        <CardContent
          sx={{
            padding: "2.5rem",
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
            <LibraryMusicIcon
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
            {isEdit ? t("adminPanelEditTitle") : t("adminPanelTitle")}
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
            {isEdit ? t("adminPanelEditDesc") : t("adminPanelDesc")}
          </Typography>
          <Divider
            sx={{
              width: 80,
              borderColor: theme.palette.accent.main,
              marginBottom: "1.5rem",
            }}
          />
          {loadingTrack && isEdit ? (
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
                    label={t("trackTitle")}
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
                    name="artist"
                    label={t("trackArtist")}
                    value={values.artist}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    fullWidth
                    required
                    variant="outlined"
                    error={touched.artist && Boolean(errors.artist)}
                    helperText={touched.artist && errors.artist}
                  />
                  <TextField
                    name="url"
                    label={t("trackUrl")}
                    value={values.url}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    fullWidth
                    required
                    variant="outlined"
                    type="url"
                    error={touched.url && Boolean(errors.url)}
                    helperText={touched.url && errors.url}
                  />
                  {values.url && (
                    <>
                      <Typography>{"Проверьте свой трек"}</Typography>
                      <AudioPlayer
                        src={values.url}
                        onPlay={() => console.log("Playing")}
                        showJumpControls={false}
                        customAdditionalControls={[]}
                        style={{
                          background: theme.palette.background.paper,
                        }}
                      />
                    </>
                  )}
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
                    {isEdit ? t("editTrack") : t("addTrack")}
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
