import { Box, Button } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import RefreshIcon from "@mui/icons-material/Refresh";
import { ReactNode } from "react";

type Props = {
  onCreate: () => void;
  onRefresh: () => void;
  loading: boolean;
  createLabel: ReactNode;
  refreshLabel: ReactNode;
};

export function TableActions({
  onCreate,
  onRefresh,
  loading,
  createLabel,
  refreshLabel,
}: Props) {
  return (
    <Box sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}>
      <Button
        variant="contained"
        startIcon={<AddIcon />}
        onClick={onCreate}
        sx={{ borderRadius: "2rem" }}
        disabled={loading}
      >
        {createLabel}
      </Button>
      <Button
        variant="outlined"
        startIcon={<RefreshIcon />}
        onClick={onRefresh}
        disabled={loading}
      >
        {refreshLabel}
      </Button>
    </Box>
  );
}