import request from '../utils/request'

export interface Category {
  id: number
  name: string
  icon: string
  sort: number
  videoCount: number
}

export interface ProgressInfo {
  currentSecond: number
  progressPercent: number
  finished: boolean
}

export interface VideoCard {
  id: number
  title: string
  coverUrl: string
  lecturer: string
  categoryId: number
  categoryName: string
  tags: string[]
  duration: number
  playCount: number
  likeCount: number
  collectCount: number
  progressPercent: number
  finished: boolean
  publishTime: string
}

export interface VideoDetail extends VideoCard {
  description: string
  videoUrl: string
  liked: boolean
  collected: boolean
  progress: ProgressInfo
  status: string
  relatedVideos: VideoCard[]
}

export interface PageResult<T> {
  records: T[]
  page: number
  pageSize: number
  total: number
  pages: number
}

export interface HomeData {
  categories: Category[]
  recommended: VideoCard[]
  hot: VideoCard[]
  latest: VideoCard[]
  continueLearning: VideoCard[]
}

export interface QueryParams {
  keyword?: string
  categoryId?: number
  sort?: string
  page?: number
  pageSize?: number
}

export const videoApi = {
  home: () => request.get<unknown, HomeData>('/api/video/home'),
  list: (params: QueryParams) => request.get<unknown, PageResult<VideoCard>>('/api/video/list', { params }),
  search: (params: QueryParams) => request.get<unknown, PageResult<VideoCard>>('/api/video/search', { params }),
  detail: (id: number) => request.get<unknown, VideoDetail>(`/api/video/${id}`),
  recordPlay: (id: number, payload: { playedSecond: number; duration: number }) =>
    request.post<unknown, void>(`/api/video/${id}/play`, payload),
  progress: (id: number, payload: { currentSecond: number; duration: number }) =>
    request.post<unknown, ProgressInfo>(`/api/video/${id}/progress`, payload),
  like: (id: number) => request.post<unknown, void>(`/api/video/${id}/like`),
  unlike: (id: number) => request.delete<unknown, void>(`/api/video/${id}/like`),
  favorite: (id: number) => request.post<unknown, void>(`/api/video/${id}/favorite`),
  unfavorite: (id: number) => request.delete<unknown, void>(`/api/video/${id}/favorite`),
  history: (params: QueryParams) => request.get<unknown, PageResult<VideoCard>>('/api/video/learning/history', { params }),
  favorites: (params: QueryParams) => request.get<unknown, PageResult<VideoCard>>('/api/video/favorites', { params }),
  related: (id: number, limit = 6) => request.get<unknown, VideoCard[]>(`/api/video/${id}/related`, { params: { limit } })
}
