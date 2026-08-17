import request from '../utils/request'
import {
  mockCategories,
  mockHomeData,
  mockProgress,
  mockUploadVideos,
  mockVideoDetail,
  mockVideoPage,
  mockVideos
} from '../mockData'

export interface Category {
  id: number
  name: string
  icon: string
  sort: number
  videoCount: number
  status: number
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
  status: string
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

export interface SaveCategoryPayload {
  id?: number
  name: string
  icon?: string
  sort: number
  status: number
}

export interface SaveVideoPayload {
  id?: number
  title: string
  description?: string
  categoryId?: number
  lecturer?: string
  coverUrl: string
  videoUrl: string
  duration: number
  tags?: string
  status: string
}

const mockConfig = { mockFallback: true }

const withMock = async <T>(promise: Promise<T>, fallback: () => T, empty?: (value: T) => boolean) => {
  try {
    const value = await promise
    return empty?.(value) ? fallback() : value
  } catch {
    return fallback()
  }
}

const emptyArray = <T>(value: T[]) => value.length === 0
const emptyPage = <T>(value: PageResult<T>) => value.records.length === 0

export const videoApi = {
  home: () =>
    withMock(request.get<unknown, HomeData>('/api/video/home', mockConfig), () => mockHomeData, (value) => value.recommended.length === 0 && value.hot.length === 0),
  categories: () =>
    withMock(request.get<unknown, Category[]>('/api/video/categories', mockConfig), () => mockCategories, emptyArray),
  list: (params: QueryParams) =>
    withMock(request.get<unknown, PageResult<VideoCard>>('/api/video/list', { params, ...mockConfig }), () => mockVideoPage(params), emptyPage),
  search: (params: QueryParams) =>
    withMock(request.get<unknown, PageResult<VideoCard>>('/api/video/search', { params, ...mockConfig }), () => mockVideoPage(params), emptyPage),
  detail: (id: number) =>
    withMock(request.get<unknown, VideoDetail>(`/api/video/${id}`, mockConfig), () => mockVideoDetail(id)),
  recordPlay: (id: number, payload: { playedSecond: number; duration: number }) =>
    withMock(request.post<unknown, void>(`/api/video/${id}/play`, payload, mockConfig), () => undefined),
  progress: (id: number, payload: { currentSecond: number; duration: number }) =>
    withMock(request.post<unknown, ProgressInfo>(`/api/video/${id}/progress`, payload, mockConfig), () => ({
      ...mockProgress,
      currentSecond: payload.currentSecond,
      progressPercent: Math.min(100, Math.round((payload.currentSecond / Math.max(payload.duration, 1)) * 100)),
      finished: payload.currentSecond >= payload.duration
    })),
  like: (id: number) => withMock(request.post<unknown, void>(`/api/video/${id}/like`, undefined, mockConfig), () => undefined),
  unlike: (id: number) => withMock(request.delete<unknown, void>(`/api/video/${id}/like`, mockConfig), () => undefined),
  favorite: (id: number) => withMock(request.post<unknown, void>(`/api/video/${id}/favorite`, undefined, mockConfig), () => undefined),
  unfavorite: (id: number) => withMock(request.delete<unknown, void>(`/api/video/${id}/favorite`, mockConfig), () => undefined),
  history: (params: QueryParams) =>
    withMock(request.get<unknown, PageResult<VideoCard>>('/api/video/learning/history', { params, ...mockConfig }), () => mockVideoPage(params, mockVideos.filter((item) => item.progressPercent > 0)), emptyPage),
  favorites: (params: QueryParams) =>
    withMock(request.get<unknown, PageResult<VideoCard>>('/api/video/favorites', { params, ...mockConfig }), () => mockVideoPage(params, mockVideos.filter((item) => item.collectCount > 550)), emptyPage),
  related: (id: number, limit = 6) =>
    withMock(request.get<unknown, VideoCard[]>(`/api/video/${id}/related`, { params: { limit }, ...mockConfig }), () => mockVideoDetail(id).relatedVideos.slice(0, limit), emptyArray),
  adminCategories: () =>
    withMock(request.get<unknown, Category[]>('/api/video/admin/categories', mockConfig), () => mockCategories, emptyArray),
  saveCategory: (payload: SaveCategoryPayload) =>
    withMock(request.post<unknown, Category>('/api/video/admin/categories', payload, mockConfig), () => ({ id: payload.id || Date.now(), icon: payload.icon || 'book-open', videoCount: 0, ...payload })),
  enableCategory: (id: number) => withMock(request.post<unknown, void>(`/api/video/admin/categories/${id}/enable`, undefined, mockConfig), () => undefined),
  disableCategory: (id: number) => withMock(request.post<unknown, void>(`/api/video/admin/categories/${id}/disable`, undefined, mockConfig), () => undefined),
  adminVideos: (params: QueryParams) =>
    withMock(request.get<unknown, PageResult<VideoCard>>('/api/video/admin/videos', { params, ...mockConfig }), () => mockVideoPage(params, [...mockVideos, ...mockUploadVideos], false), emptyPage),
  adminVideoDetail: (id: number) =>
    withMock(request.get<unknown, VideoDetail>(`/api/video/admin/videos/${id}`, mockConfig), () => mockVideoDetail(id)),
  saveVideo: (payload: SaveVideoPayload) =>
    withMock(request.post<unknown, VideoDetail>('/api/video/admin/videos', payload, mockConfig), () => mockVideoDetail(payload.id || 101)),
  myUploads: (params: QueryParams) =>
    withMock(request.get<unknown, PageResult<VideoCard>>('/api/video/uploads', { params, ...mockConfig }), () => mockVideoPage(params, mockUploadVideos, false), emptyPage),
  uploadVideo: (payload: SaveVideoPayload) =>
    withMock(request.post<unknown, VideoDetail>('/api/video/uploads', payload, mockConfig), () => mockVideoDetail(201)),
  deleteUpload: (id: number) => withMock(request.delete<unknown, void>(`/api/video/uploads/${id}`, mockConfig), () => undefined),
  onlineVideo: (id: number) => withMock(request.post<unknown, void>(`/api/video/admin/videos/${id}/online`, undefined, mockConfig), () => undefined),
  offlineVideo: (id: number) => withMock(request.post<unknown, void>(`/api/video/admin/videos/${id}/offline`, undefined, mockConfig), () => undefined)
}
