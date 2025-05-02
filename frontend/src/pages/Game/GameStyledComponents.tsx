import { Box, IconButton, Paper, styled } from "@mui/material";
import { motion } from "framer-motion";

export const StyledPaper = styled(Paper)({
    padding: "32px",
    borderRadius: 16,
    boxShadow: "0 8px 32px rgba(0, 0, 0, 0.1)",
    background: "rgba(255, 255, 255, 0.95)",
    backdropFilter: "blur(8px)",
    transition: "all 0.3s ease",
    overflow: "hidden",
  });
  
  export  const OptionButton = styled(motion.div)({
    width: "100%",
    borderRadius: 12,
    overflow: "hidden",
    transition: "all 0.2s ease",
    cursor: "pointer",
  });
  
  export const ScoreDisplay = styled(Box)({
    display: "flex",
    justifyContent: "space-between",
    marginTop: "32px",
    padding: "16px",
    borderRadius: 8,
    background: "rgba(0, 0, 0, 0.03)",
  });
  
 export const AudioControlButton = styled(IconButton)({
    position: "absolute",
    top: "16px",
    right: "16px",
    background: "rgba(0, 0, 0, 0.05)",
    "&:hover": {
      background: "rgba(0, 0, 0, 0.1)",
    },
  });