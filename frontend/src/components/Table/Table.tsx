import {
  Table as MUITable,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from "@mui/material";
import { ReactNode } from "react";

export type TableColumn<T> = {
  label: ReactNode;
  render: (row: T) => ReactNode;
  align?: "left" | "center" | "right";
};

type Props<T> = {
  rows: T[];
  columns: TableColumn<T>[];
  actions?: (row: T) => ReactNode;
  emptyMessage?: ReactNode;
  maxHeight?: number | string;
  minWidth?: number | string;
};

export function Table<T>({
  rows,
  columns,
  actions,
  emptyMessage,
  maxHeight = 500,
  minWidth = 700, // по умолчанию минимальная ширина для скролла
}: Props<T>) {
  return (
    <TableContainer
      component={Paper}
      sx={{
        borderRadius: "1rem",
        boxShadow: "none",
        overflowX: "auto",
        maxHeight,
        overflowY: "auto",
        width: "100%",
      }}
    >
      <MUITable stickyHeader sx={{ minWidth, width: "100%" }}>
        <TableHead>
          <TableRow>
            {columns.map((col, idx) => (
              <TableCell key={idx} align={col.align || "left"}>
                {col.label}
              </TableCell>
            ))}
            {actions && <TableCell align="center"> </TableCell>}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={columns.length + (actions ? 1 : 0)}
                align="center"
              >
                {emptyMessage}
              </TableCell>
            </TableRow>
          ) : (
            rows.map((row, idx) => (
              <TableRow key={idx}>
                {columns.map((col, cidx) => (
                  <TableCell key={cidx} align={col.align || "left"}>
                    {col.render(row)}
                  </TableCell>
                ))}
                {actions && (
                  <TableCell align="center">{actions(row)}</TableCell>
                )}
              </TableRow>
            ))
          )}
        </TableBody>
      </MUITable>
    </TableContainer>
  );
}