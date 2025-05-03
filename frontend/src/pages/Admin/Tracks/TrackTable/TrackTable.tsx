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
  TextField,
  InputAdornment,
  alpha,
  Chip,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import SearchIcon from "@mui/icons-material/Search";
import MusicNoteIcon from "@mui/icons-material/MusicNote";
import AudioPlayer from "react-h5-audio-player";
import "react-h5-audio-player/lib/styles.css";
import { useTranslation } from "react-i18next";
import { TranslationNamespace } from "../../../../i18n";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../../hooks/auth-context";
import { deleteTrack, getTracks, Track } from "../../../../api/tracks-api";
import {
  DialogConfirm,
  PaginatedTable,
  Table,
  TableActions,
} from "../../../../components";
import { motion } from "framer-motion";

type TableColumn<T> = {
  label: React.ReactNode;
  render: (row: T) => React.ReactNode;
  align?: "left" | "center" | "right";
};

export const TrackTable = () => {
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
  const [search, setSearch] = useState("");

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

  const handleDeleteConfirm = async (refresh: () => void) => {
    if (!trackToDelete || !user?.id) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteTrack(trackToDelete.id, user.id);
      setDeleteDialogOpen(false);
      setTrackToDelete(null);
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
    setTrackToDelete(null);
    setDeleteError(null);
  };

  const columns: TableColumn<Track>[] = [
    { 
      label: t("trackTitle"), 
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
      label: t("trackArtist"), 
      render: (row) => (
        <Chip
          icon={<MusicNoteIcon />}
          label={row.artist}
          variant="outlined"
          size="small"
          sx={{ 
            fontWeight: 500,
            borderRadius: "8px",
            backgroundColor: alpha(theme.palette.primary.light, 0.1),
            borderColor: alpha(theme.palette.primary.light, 0.3),
            color: theme.palette.text.primary
          }}
        />
      ) 
    },
    {
      label: t("trackUrl"),
      render: (row) => (
        <Box sx={{ mt: 2 , minWidth: 300, }}>
          <AudioPlayer
            src={row.url}
            onPlay={() => {}}
            style={{
              borderRadius: '0.75rem',
              background: theme.palette.background.paper,
            }}
            showJumpControls={false}
            customAdditionalControls={[]}
          />
        </Box>
      ),
    },
  ];

  const fetchData = (page: number, size: number) =>
    getTracks(page, size, search);

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <Card
        sx={{
          maxWidth: 1300,
          minWidth: 500,
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
            <MusicNoteIcon 
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
              {t("adminPanelTracksTitle")}
            </Typography>
          </Box>

          <Box sx={{ 
            mb: 3, 
            display: "flex", 
            justifyContent: "flex-end",
            width: "100%" 
          }}>
            <TextField
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder={t("searchTracks")}
              size="small"
              sx={{ 
                width: { xs: "100%", sm: 320 },
                "& .MuiOutlinedInput-root": {
                  borderRadius: "12px",
                  backgroundColor: alpha(theme.palette.background.paper, 0.8),
                  boxShadow: `0 2px 8px ${alpha(theme.palette.common.black, 0.05)}`,
                  transition: "all 0.2s ease",
                  "&:hover": {
                    boxShadow: `0 4px 12px ${alpha(theme.palette.common.black, 0.08)}`,
                  },
                  "&.Mui-focused": {
                    boxShadow: `0 4px 12px ${alpha(theme.palette.primary.main, 0.15)}`,
                  }
                }
              }}
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

          <PaginatedTable<Track> fetchData={fetchData} defaultRowsPerPage={10}>
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
                      actions={(track) => (
                        <Box sx={{ display: "flex", gap: 1 }}>
                          <Tooltip title={t("editTrack")} arrow>
                            <IconButton 
                              onClick={() => handleEdit(track)}
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
                          <Tooltip title={t("deleteTrack")} arrow>
                            <IconButton
                              color="error"
                              onClick={() => handleDeleteClick(track)}
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
                      emptyMessage={t("noTracks")}
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
                  title={t("deleteTrackTitle")}
                  confirmText={t("delete")}
                  cancelText={t("cancel")}
                  loadingText={t("deleting")}
                  dialogText={t("deleteTrackConfirm")}
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