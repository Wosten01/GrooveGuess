

import { useState, useCallback, useEffect, ReactNode } from "react";
import TablePagination from "@mui/material/TablePagination";
import { AxiosError } from "axios";

type PaginatedProps<T> = {
  fetchData: (page: number, size: number) => Promise<{
    content: T[];
    totalElements: number;
    totalPages?: number;
    number?: number;
  }>;
  defaultRowsPerPage?: number;
  children: (args: {
    data: T[];
    loading: boolean;
    error: string | null;
    pagination: ReactNode;
    refresh: () => void;
    page: number;
    rowsPerPage: number;
    totalElements: number;
    setPage: (page: number) => void;
    setRowsPerPage: (rows: number) => void;
  }) => ReactNode;
};

export function PaginatedTable<T>({
  fetchData,
  defaultRowsPerPage = 20,
  children,
}: PaginatedProps<T>) {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(defaultRowsPerPage);
  const [data, setData] = useState<T[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await fetchData(page, rowsPerPage);
      setData(result.content);
      setTotalElements(result.totalElements);
    } catch (e: unknown) {
      if (e instanceof AxiosError) {
        setError(e.message);
      } else {
        setError("Unknown error");
      }
    } finally {
      setLoading(false);
    }
  }, [fetchData, page, rowsPerPage]);

  useEffect(() => {
    load();
  }, [load]);

  const pagination = (
    <TablePagination
      component="div"
      count={totalElements}
      page={page}
      onPageChange={(_, newPage) => setPage(newPage)}
      rowsPerPage={rowsPerPage}
      onRowsPerPageChange={e => {
        setRowsPerPage(parseInt(e.target.value, 10));
        setPage(0);
      }}
    />
  );

  return (
    <>
      {children({
        data,
        loading,
        error,
        pagination,
        refresh: load,
        page,
        rowsPerPage,
        totalElements,
        setPage,
        setRowsPerPage,
      })}
    </>
  );
}
