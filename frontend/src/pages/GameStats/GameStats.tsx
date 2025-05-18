import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Paper,
  Tabs,
  Tab,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Grid,
  Card,
  CardContent,
  Chip,
  Pagination,
  Button,
  Tooltip,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/auth-context";
import {
  getRecentGames,
  RecentGameDto,
  GameStatsDto,
  exportPlayerStats,
} from "../../api/game-stats-api";
import { useTranslation } from 'react-i18next';
import { TranslationNamespace } from '../../i18n';
import DownloadIcon from '@mui/icons-material/Download';
import JsonIcon from '@mui/icons-material/Code';
import CsvIcon from '@mui/icons-material/TableChart';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`stats-tabpanel-${index}`}
      aria-labelledby={`stats-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

export const GameStats: React.FC = () => {
  const { t } = useTranslation(TranslationNamespace.Common, { keyPrefix: 'pages.gameStats' });
  const { user } = useAuth();
  const navigate = useNavigate();
  const [tabValue, setTabValue] = useState(0);
  const [loading, setLoading] = useState(true);
  const [exportLoading, setExportLoading] = useState(false);

  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [recentGames, setRecentGames] = useState<RecentGameDto[]>([]);
  const [userStats, setUserStats] = useState<GameStatsDto | null>(null);
  
  // Export menu state
  const [exportAnchorEl, setExportAnchorEl] = useState<null | HTMLElement>(null);
  const exportMenuOpen = Boolean(exportAnchorEl);

  const getAccuracyLabel = (accuracy: number): string => {
    if (accuracy >= 80) return t('accuracyLabels.excellent');
    if (accuracy >= 60) return t('accuracyLabels.good');
    if (accuracy >= 40) return t('accuracyLabels.average');
    return t('accuracyLabels.poor');
  };
  
  const getAccuracyColor = (accuracy: number): string => {
    if (accuracy >= 80) return "success";
    if (accuracy >= 60) return "info";
    if (accuracy >= 40) return "warning";
    return "error";
  };

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    setPage(0);
  };

  const handlePageChange = (_: React.ChangeEvent<unknown>, value: number) => {
    setPage(value - 1);
  };
  
  // Export menu handlers
  const handleExportClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setExportAnchorEl(event.currentTarget);
  };
  
  const handleExportClose = () => {
    setExportAnchorEl(null);
  };
  
  const handleExportFormat = async (format: 'json' | 'csv') => {
    if (!user) return;
    
    setExportLoading(true);
    try {
      await exportPlayerStats(user.id!, format);
    } catch (error) {
      console.error(`Error exporting stats as ${format}:`, error);
    } finally {
      setExportLoading(false);
      handleExportClose();
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        if (tabValue === 0) {
          if (user) {
            const response = await getRecentGames(page, pageSize, user.id);
            setRecentGames(response.games);
            setTotalPages(response.totalPages);
          }
        } else if (tabValue === 1 && user) {
          const response = await getRecentGames(page, pageSize, user.id);
          setRecentGames(response.games);
          setTotalPages(response.totalPages);
          if (response.stats) {
            setUserStats(response.stats);
          }
        } else if ((tabValue === 2 && user) || (tabValue === 1 && !user)) {
          if (user) {
            const response = await getRecentGames(page, pageSize, user.id);
            setRecentGames(response.games);
            setTotalPages(response.totalPages);
          }
        }
      } catch (error) {
        console.error("Error fetching game stats:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [tabValue, page, pageSize, user]);

  const handleViewGame = (sessionId: string) => {
    navigate(`/game/player/${user?.id}/session/${sessionId}/results`);
  };

  const formatDate = (timestamp: number): string => {
    return new Intl.DateTimeFormat("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date(timestamp * 1000));
  };

  const renderStatsCards = (stats: GameStatsDto) => (
    <Grid container spacing={3} sx={{ mb: 4 }}>
      <Grid size={{ xs: 12, sm: 6, md: 4 }}>
        <Card>
          <CardContent sx={{ textAlign: "center" }}>
            <Typography color="textSecondary" gutterBottom>
              {t('stats.totalGames')}
            </Typography>
            <Typography variant="h3" component="div" color="primary">
              {stats.totalGames}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 4 }}>
        <Card>
          <CardContent sx={{ textAlign: "center" }}>
            <Typography color="textSecondary" gutterBottom>
              {t('stats.averageScore')}
            </Typography>
            <Typography variant="h3" component="div" color="primary">
              {stats.averageScore}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 4 }}>
        <Card>
          <CardContent sx={{ textAlign: "center" }}>
            <Typography color="textSecondary" gutterBottom>
              {t('stats.highestScore')}
            </Typography>
            <Typography variant="h3" component="div" color="primary">
              {stats.highestScore}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 4 }}>
        <Card>
          <CardContent sx={{ textAlign: "center" }}>
            <Typography color="textSecondary" gutterBottom>
              {t('stats.totalScore')}
            </Typography>
            <Typography variant="h3" component="div" color="primary">
              {stats.totalScore}
            </Typography>
          </CardContent>
        </Card>
      </Grid>

      <Grid size={{ xs: 12, sm: 6, md: 4 }}>
        <Card>
          <CardContent sx={{ textAlign: "center" }}>
            <Typography color="textSecondary" gutterBottom>
              {t('stats.accuracy')}
            </Typography>

            <Box
              sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
              }}
            >
              <Tooltip title={getAccuracyLabel(stats.accuracy)}>
                <Typography
                  variant="h3"
                  component="div"
                  color={getAccuracyColor(stats.accuracy)}
                >
                  {stats.accuracy}%
                </Typography>
              </Tooltip>
            </Box>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );

  const renderGamesTable = (games: RecentGameDto[]) => (
    <>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>{t('table.date')}</TableCell>
              <TableCell>{t('table.quiz', { id: '' })}</TableCell>
              <TableCell>{t('table.score')}</TableCell>
              <TableCell>{t('table.accuracy')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {games.map((game) => {
              const accuracy =
                game.totalRounds > 0
                  ? Math.round((game.correctAnswers / game.totalRounds) * 100)
                  : 0;

              const accuracyColor = getAccuracyColor(accuracy);

              return (
                <TableRow
                  key={game.sessionId}
                  hover
                  onClick={() => handleViewGame(game.sessionId)}
                  sx={{ cursor: "pointer" }}
                >
                  <TableCell>{formatDate(game.timestamp)}</TableCell>
                  <TableCell>{game.quizTitle ? game.quizTitle : t('table.quizExample', { id: game.quizId})}</TableCell>
                  <TableCell>{game.score}</TableCell>
                  <TableCell>
                    <Tooltip title={getAccuracyLabel(accuracy)}>
                      <Chip
                        label={`${accuracy}%`}
                        color={
                          accuracyColor as
                            | "success"
                            | "info"
                            | "warning"
                            | "error"
                        }
                        size="small"
                      />
                    </Tooltip>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>

      <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
        <Pagination
          count={totalPages}
          page={page + 1}
          onChange={handlePageChange}
          color="primary"
        />
      </Box>
    </>
  );

  if (!user) {
    return (
      <Box sx={{ maxWidth: 1200, mx: "auto", p: 3 }}>
        <Typography variant="h4" gutterBottom>
          {t('title')}
        </Typography>
        <Paper sx={{ p: 4, textAlign: "center" }}>
          <Typography variant="h6" gutterBottom>
            {t('loginRequired')}
          </Typography>
          <Button
            variant="contained"
            color="primary"
            onClick={() => navigate("/login", { state: { from: "/stats" } })}
          >
            {t('loginButton')}
          </Button>
        </Paper>
      </Box>
    );
  }

  if (loading && page === 0) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="80vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 1200, mx: "auto", p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">
          {t('title')}
        </Typography>
        
        {/* Export Button */}
        <Button
          variant="outlined"
          color="primary"
          startIcon={<DownloadIcon />}
          onClick={handleExportClick}
          disabled={exportLoading}
        >
          {exportLoading ? <CircularProgress size={24} /> : t('exportButton')}
        </Button>
        
        {/* Export Format Menu */}
        <Menu
          anchorEl={exportAnchorEl}
          open={exportMenuOpen}
          onClose={handleExportClose}
        >
          <MenuItem onClick={() => handleExportFormat('json')}>
            <ListItemIcon>
              <JsonIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>JSON</ListItemText>
          </MenuItem>
          <MenuItem onClick={() => handleExportFormat('csv')}>
            <ListItemIcon>
              <CsvIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>CSV</ListItemText>
          </MenuItem>
        </Menu>
      </Box>

      <Paper sx={{ width: "100%", mb: 4 }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          indicatorColor="primary"
          textColor="primary"
          centered
        >
          <Tab label={t('tabs.recentGames')} />
          <Tab label={t('tabs.myStatistics')} />
        </Tabs>

        <TabPanel value={tabValue} index={0}>
          {loading ? (
            <Box display="flex" justifyContent="center" py={4}>
              <CircularProgress />
            </Box>
          ) : recentGames.length > 0 ? (
            renderGamesTable(recentGames)
          ) : (
            <Typography variant="body1" color="textSecondary" align="center">
              {t('noGames')}
            </Typography>
          )}
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          {loading ? (
            <Box display="flex" justifyContent="center" py={4}>
              <CircularProgress />
            </Box>
          ) : userStats ? (
            <>{renderStatsCards(userStats)}</>
          ) : (
            <Typography variant="body1" color="textSecondary" align="center">
              {t('noStats')}
            </Typography>
          )}
        </TabPanel>
      </Paper>
    </Box>
  );
};