import { useState } from "react";
import {
  Box,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  List,
  ListItem,
  ListItemText,
} from "@mui/material";
import { Quiz } from "../../../../api/quiz-api";
import { useTranslation } from "react-i18next";
import { TranslationNamespace } from "../../../../i18n";

interface QuizTracksCellProps {
  quiz: Quiz;
  onEditTracks?: (quiz: Quiz) => void;
}

const MAX_VISIBLE_CHIPS = 2;

export const QuizTracksCell: React.FC<QuizTracksCellProps> = ({ quiz }) => {
  const { t } = useTranslation(TranslationNamespace.Common, {
    keyPrefix: "pages.admin.quizzes.table",
  });
  const [open, setOpen] = useState(false);
  const [showAll, setShowAll] = useState(false);

  const handleClose = () => setOpen(false);

  const tracks = quiz.tracks ?? [];
  const visibleTracks = showAll ? tracks : tracks.slice(0, MAX_VISIBLE_CHIPS);

  return (
    <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
      <Box sx={{ display: "flex", flexWrap: "wrap", gap: 0.5, maxWidth: 180 }}>
        {tracks.length === 0 ? (
          <span style={{ color: "#888" }}>{t("noTracks")}</span>
        ) : (
          <>
            {visibleTracks.map((track) => (
              <Chip
                key={track.id}
                label={`${track.title} - ${track.artist}`}
                size="small"
                sx={{ mb: 0.5, maxWidth: 120 }}
              />
            ))}
            {tracks.length > MAX_VISIBLE_CHIPS && (
              <Button
                size="small"
                onClick={() => setShowAll((prev) => !prev)}
                sx={{ minWidth: 0, ml: 1, fontSize: 12, p: 0.5 }}
              >
                {showAll
                  ? t("hideTracks", { count: tracks.length })
                  : t("showAllTracks", { count: tracks.length })}
              </Button>
            )}
          </>
        )}
      </Box>
      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogTitle>{t("editTracksTitle")}</DialogTitle>
        <DialogContent>
          <List dense>
            {tracks.length > 0 ? (
              tracks.map((track) => (
                <ListItem key={track.id}>
                  <ListItemText
                    primary={track.title}
                    secondary={track.artist}
                  />
                </ListItem>
              ))
            ) : (
              <ListItem>
                <ListItemText primary={t("noTracks")} />
              </ListItem>
            )}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>{t("close")}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};