import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  Alert,
} from "@mui/material";
import { ReactNode } from "react";

export type DialogConfirmProps = {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  loading?: boolean;
  error?: string | null;
  title: ReactNode;
  confirmText: ReactNode;
  cancelText: ReactNode;
  loadingText?: ReactNode;
  dialogText: ReactNode;
  confirmColor?: "primary" | "error" | "secondary" | "success" | "info" | "warning";
  confirmVariant?: "contained" | "outlined" | "text";
};

export function DialogConfirm({
  open,
  onClose,
  onConfirm,
  loading = false,
  error,
  title,
  confirmText,
  cancelText,
  loadingText,
  dialogText,
  confirmColor = "primary",
  confirmVariant = "contained",
}: DialogConfirmProps) {
  return (
    <Dialog open={open} onClose={onClose} aria-labelledby="dialog-confirm-title">
      <DialogTitle id="dialog-confirm-title">{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{dialogText}</DialogContentText>
        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          {cancelText}
        </Button>
        <Button
          onClick={onConfirm}
          color={confirmColor}
          variant={confirmVariant}
          disabled={loading}
        >
          {loading ? loadingText || confirmText : confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
}