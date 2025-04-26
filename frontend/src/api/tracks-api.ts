import { apiDelete, apiGet, apiPost, apiPut, ApiResponse } from "./api";

export type Track = {
  id: string | number;
  title: string;
  artist: string;
  url: string;
};

const authPrefix = "/tracks"

export async function getTracks(): Promise<Track[]> {
  const response: ApiResponse<Track[]> = await apiGet<Track[]>(authPrefix);
  return response.data;
}

export async function updateTrack(
  id: string | number,
  data: { title: string; artist: string; url: string },
  userId: string | number
):  Promise< ApiResponse<Track>> {
  const response: ApiResponse<Track> = await apiPut<Track>(authPrefix +
    `/${id}?userId=${encodeURIComponent(userId)}`,
    data
  );
  return response;
}


export async function createTrack(
  data: { title: string; artist: string; url: string },
  userId: string | number
): Promise< ApiResponse<Track>> {
  const response: ApiResponse<Track> = await apiPost<Track>(
    authPrefix + `?creatorId=${encodeURIComponent(userId)}`,
    data
  );
  return response;
}

export async function deleteTrack(
  id: string | number,
  userId: string | number
): Promise<ApiResponse<void>> {
  return apiDelete<void>(authPrefix + `/${id}?userId=${encodeURIComponent(userId)}`);
}
