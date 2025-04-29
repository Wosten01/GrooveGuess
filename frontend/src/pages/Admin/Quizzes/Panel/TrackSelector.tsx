import React, { useEffect, useState } from "react";
import {
  Box,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Chip,
  Pagination,
  CircularProgress,
  Typography,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import { getTracks, Track } from "../../../../api/tracks-api";

interface TrackSelectorProps {
  selectedTracks: Track[];
  onChange: (tracks: Track[]) => void;
}

export const TrackSelector: React.FC<TrackSelectorProps> = ({
  selectedTracks,
  onChange,
}) => {
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [tracks, setTracks] = useState<Track[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    getTracks(page - 1, 10, search)
      .then((res) => {
        setTracks(res.content ?? []);
        setTotalPages(res.totalPages ?? 1);
      })
      .finally(() => setLoading(false));
  }, [search, page]);

  const handleAdd = (track: Track) => {
    if (!selectedTracks.some((t) => t.id === track.id)) {
      onChange([...selectedTracks, track]);
    }
  };

  const handleRemove = (track: Track) => {
    onChange(selectedTracks.filter((t) => t.id !== track.id));
  };

  const filteredTracks = tracks.filter(
    (track) => !selectedTracks.some((t) => t.id === track.id)
  );

  return (
    <Box sx={{ width: "100%" }}>
      <TextField
        label="Поиск треков"
        value={search}
        onChange={(e) => {
          setSearch(e.target.value);
          setPage(1);
        }}
        fullWidth
        sx={{ mb: 2 }}
      />

      <Box sx={{ mb: 1, display: "flex", flexWrap: "wrap", gap: 1 }}>
        {selectedTracks.map((track) => (
          <Chip
            key={track.id}
            label={
              track.artist
                ? `${track.title} — ${track.artist}`
                : track.title
            }
            onDelete={() => handleRemove(track)}
            sx={{ maxWidth: 220 }}
          />
        ))}
      </Box>

      <TableContainer component={Paper} sx={{ maxHeight: 260, mb: 1 }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell align="center">Название</TableCell>
              <TableCell align="center">Артист</TableCell>
              <TableCell align="center">Действие</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={3} align="center">
                  <CircularProgress size={24} />
                </TableCell>
              </TableRow>
            ) : filteredTracks.length === 0 ? (
              <TableRow>
                <TableCell colSpan={3} align="center">
                  <Typography variant="body2" color="text.secondary">
                    Нет треков
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredTracks.map((track) => (
                <TableRow key={track.id} hover>
                  <TableCell align="center">{track.title}</TableCell>
                  <TableCell align="center">{track.artist}</TableCell>
                  <TableCell align="center">
                    <IconButton
                      size="small"
                      color="primary"
                      onClick={() => handleAdd(track)}
                    >
                      <AddIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
      <Box sx={{ display: "flex", justifyContent: "center" }}>
        <Pagination
          count={totalPages}
          page={page}
          onChange={(_, value) => setPage(value)}
          size="small"
          color="primary"
        />
      </Box>
    </Box>
  );
};