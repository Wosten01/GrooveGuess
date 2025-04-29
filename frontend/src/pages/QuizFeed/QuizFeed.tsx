import React, { useEffect, useState, useRef, useCallback } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  CircularProgress,
  Fade,

} from "@mui/material";
import { useTheme } from "@mui/material/styles";
import { getQuizzes } from "../../api/quiz-api";
import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";
import { TranslationNamespace } from "../../i18n";
import { useNavigate } from "react-router-dom";

interface Quiz {
  id: number;
  title: string;
  description: string;
  creator: {
    username: string;
  };
}

const PAGE_SIZE = 10;

export const QuizCard: React.FC<{ quiz: Quiz; index: number }> = ({
  quiz,
  index,
}) => {
  const theme = useTheme();
  const { t } = useTranslation(TranslationNamespace.Common);
  const navigate = useNavigate();
  const handlePlayClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigate(`/quizzes/play/${quiz.id}`);
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 40, scale: 0.95 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{
        delay: index * 0.07,
        type: "spring",
        stiffness: 120,
        damping: 18,
      }}
      whileHover={{
        scale: 1.03,
        boxShadow: "0 12px 32px rgba(76, 175, 80, 0.18)",
      }}
      style={{ height: "100%" }}
      onClick={handlePlayClick}
    >
      <Card
        sx={{
          height: "100%",
          display: "flex",
          flexDirection: "column",
          borderRadius: "1.5rem",
          boxShadow: "0 8px 24px rgba(60, 60, 60, 0.18)",
          transition: "box-shadow 0.3s",
          background: theme.palette.secondary.contrastText,
          border: `1.5px solid ${theme.palette.primary.main}`,
          cursor: "pointer",
          position: "relative",
          overflow: "visible",
        }}
      >
        <CardContent sx={{ flexGrow: 1 }}>
          <Typography
            variant="h4"
            sx={{
              color: theme.palette.primary.dark,
              fontWeight: 700,
              mb: 2,
              fontSize: { xs: "1.3rem", sm: "1.7rem", md: "2.1rem" },
              lineHeight: 1.2,
            }}
          >
            {quiz.title}
          </Typography>
          <Typography
            variant="body1"
            sx={{
              color: theme.palette.secondary.dark,
              mb: 3,
              fontSize: { xs: "1rem", sm: "1.1rem", md: "1.15rem" },
              minHeight: 60,
            }}
          >
            {quiz.description}
          </Typography>
          <Box
            sx={{
              display: "flex",
              justifyContent: "flex-end",
              alignItems: "center",
              width: "100%",
              mt: "auto",
              opacity: 0.8,
              gap: 1,
            }}
          >
            <Typography
              variant="caption"
              sx={{
                color: theme.palette.primary.main,
                fontWeight: 500,
                fontSize: "0.95rem",
              }}
            >
              {t("pages.quizFeed.creator")}: {quiz.creator.username}
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </motion.div>
  );
};

export const QuizFeed: React.FC = () => {
  const { t } = useTranslation(TranslationNamespace.Common, {
    keyPrefix: "pages.quizFeed",
  });
  const theme = useTheme();

  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);

  const observer = useRef<IntersectionObserver | null>(null);
  const lastQuizRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (loading) return;
      if (observer.current) observer.current.disconnect();
      observer.current = new window.IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          setPage((prev) => prev + 1);
        }
      });
      if (node) observer.current.observe(node);
    },
    [loading, hasMore]
  );

  useEffect(() => {
    setLoading(true);
    getQuizzes(page, PAGE_SIZE)
      .then((res) => {
        const newQuizzes = (res.content || []) as unknown as Quiz[];
        setQuizzes((prev) => {
          const ids = new Set(prev.map(q => q.id));
          const filtered = newQuizzes.filter(q => !ids.has(q.id));
          return [...prev, ...filtered];
        });
        setHasMore(newQuizzes.length === PAGE_SIZE);
      })
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <Box
      sx={{
        minHeight: "100vh",
        background: `linear-gradient(135deg, ${theme.palette.accent.light} 0%, ${theme.palette.pastel.main} 100%)`,
        padding: { xs: "1rem", sm: "2rem" },
        fontFamily: theme.typography.fontFamily,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
      }}
    >
      <Typography
        variant="h3"
        align="center"
        sx={{
          color: theme.palette.primary.dark,
          fontWeight: 800,
          mb: 4,
          mt: 2,
          fontSize: { xs: "2rem", sm: "2.5rem", md: "3rem" },
          letterSpacing: "-1px",
        }}
      >
        {t("quizFeedTitle")}
      </Typography>
      <Grid
        container
        spacing={4}
        sx={{
          width: "100%",
          maxWidth: 1400,
          margin: "0 auto",
        }}
      >
        {quizzes.map((quiz, idx) => {
          const isLast = idx === quizzes.length - 1;
          return (
            <Grid
              size={{ xs: 12, sm: 6, md: 4 }}
              ref={isLast ? lastQuizRef : undefined}
              component="div"
              key={`${quiz.id}-${idx}`}
              sx={{
                display: "flex",
                flexDirection: "column",
              }}
            >
              <QuizCard quiz={quiz} index={idx % 6} />
            </Grid>
          );
        })}
      </Grid>
      <Fade in={loading}>
        <Box sx={{ mt: 4 }}>
          <CircularProgress />
        </Box>
      </Fade>
      {!hasMore && !loading && quizzes.length === 0 && (
        <Typography variant="h6" color="text.secondary" sx={{ mt: 6 }}>
          {t("noQuizzes")}
        </Typography>
      )}
    </Box>
  );
};
