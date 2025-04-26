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
import AudioPlayer from "react-h5-audio-player";
import "react-h5-audio-player/lib/styles.css";
import { useTranslation } from "react-i18next";
import { TranslationNamespace } from "../../../i18n";
import { getTracks, Track, deleteTrack } from "../../../api/tracks-api";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../hooks/auth-context";
import { DialogConfirm } from "../../../components/Dialog/DialogConfirm";
import { Table, TableActions } from "../../../components";
import { PaginatedTable } from "../../../components/Table/PaginatedTable";

type TableColumn<T> = {
  label: React.ReactNode;
  render: (row: T) => React.ReactNode;
  align?: "left" | "center" | "right";
};

export const AdminTrackTable = () => {
  const { t } = useTranslation(TranslationNamespace.Common, {
    keyPrefix: "pages.admin.tracks.table",
  });
  const theme = useTheme();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [trackToDelete, setTrackToDelete] = useState<Track | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  const handleCreate = () => {
    navigate("/admin/tracks/details");
  };

  const handleEdit = (track: Track) => {
    navigate(`/admin/tracks/details?id=${track.id}`);
  };

  const handleDeleteClick = (track: Track) => {
    setTrackToDelete(track);
    setDeleteDialogOpen(true);
    setDeleteError(null);
  };

  const handleDeleteConfirm = async () => {
    if (!trackToDelete || !user?.id) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteTrack(trackToDelete.id, user.id);
      setDeleteDialogOpen(false);
      setTrackToDelete(null);
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
    setTrackToDelete(null);
    setDeleteError(null);
  };

  const columns: TableColumn<Track>[] = [
    { label: t("trackTitle"), render: (row) => row.title },
    { label: t("trackArtist"), render: (row) => row.artist },
    {
      label: t("trackUrl"),
      render: (row) => (
        <Box sx={{ mt: 2 }}>
          <AudioPlayer
            src={row.url}
            onPlay={() => {}}
            style={{
              background: theme.palette.background.paper,
            }}
            showJumpControls={false}
            customAdditionalControls={[]}
          />
        </Box>
      ),
    },
  ];

  return (
    <div
      style={{
        display: "flex",
        alignItems: "flex-start",
        justifyContent: "center",
        minHeight: "100vh",
        background: `linear-gradient(135deg, ${theme.palette.accent.light} 0%, ${theme.palette.pastel.main} 100%)`,
        padding: "2rem",
        fontFamily: theme.typography.fontFamily,
      }}
    >
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
            {t("adminPanelTracksTitle")}
          </Typography>
          <PaginatedTable<Track>
            fetchData={getTracks}
            defaultRowsPerPage={10}
          >
            {({ data, loading, error, pagination, refresh }) => (
              <>
                <TableActions
                  onCreate={handleCreate}
                  onRefresh={refresh}
                  loading={loading}
                  createLabel={t("createTrack")}
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
                    actions={(track) => (
                      <>
                        <Tooltip title={t("editTrack")}>
                          <IconButton onClick={() => handleEdit(track)}>
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={t("deleteTrack")}>
                          <IconButton color="error" onClick={() => handleDeleteClick(track)}>
                            <DeleteIcon />
                          </IconButton>
                        </Tooltip>
                      </>
                    )}
                    emptyMessage={t("noTracks")}
                    pagination={pagination}
                  />
                )}
              </>
            )}
          </PaginatedTable>
        </CardContent>
      </Card>
      <DialogConfirm
        open={deleteDialogOpen}
        onClose={handleDeleteCancel}
        onConfirm={handleDeleteConfirm}
        loading={deleting}
        error={deleteError}
        title={t("deleteTrackTitle")}
        confirmText={t("delete")}
        cancelText={t("cancel")}
        loadingText={t("deleting")}
        dialogText={t("deleteTrackConfirm")}
        confirmColor="error"
      />
    </div>
  );
};