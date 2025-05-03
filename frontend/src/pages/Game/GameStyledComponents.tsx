import { styled } from "@mui/material/styles";
import { Box, IconButton, Paper } from "@mui/material";
import { motion } from "framer-motion";

export const StyledPaper = styled(Paper)(({ theme }) => ({
    padding: theme.spacing(4),
    borderRadius: theme.spacing(2),
    boxShadow: "0 8px 32px rgba(0, 0, 0, 0.1)",
    background: theme.palette.background.paper,
    backdropFilter: "blur(8px)",
    transition: "all 0.3s ease",
    position: "relative",
    overflow: "hidden",
    "&::before": {
      content: '""',
      position: "absolute",
      top: 0,
      left: 0,
      right: 0,
      height: "8px",
      background: `linear-gradient(90deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
    },
  }));
  
  export const OptionButton = styled(motion.div)(({ theme }) => ({
    width: "100%",
    borderRadius: theme.spacing(1.5),
    overflow: "hidden",
    transition: "all 0.2s ease",
    cursor: "pointer",
  }));
  
  export const ScoreDisplay = styled(Box)(({ theme }) => ({
    display: "flex",
    justifyContent: "center",
    marginTop: theme.spacing(4),
    padding: theme.spacing(1),
    borderRadius: theme.spacing(1),
    background: theme.palette.background.default,
  }));
  
 export const AudioControlButton = styled(IconButton)(({ theme }) => ({
    position: "absolute",
    top: theme.spacing(2),
    right: theme.spacing(2),
    backgroundColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    width: "48px",
    height: "48px",
    "&:hover": {
      backgroundColor: theme.palette.primary.dark,
    },
  }));
