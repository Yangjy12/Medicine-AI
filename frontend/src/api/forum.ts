import request from '../utils/request'
import { mockBoards, mockCommentPage, mockForumPage, mockPostDetail, mockPosts } from '../mockData'

export interface PageResult<T> {
  records: T[]
  page: number
  pageSize: number
  total: number
}

export interface Board {
  id: number
  name: string
  description: string
  icon: string
}

export interface PostCard {
  id: number
  boardId: number
  boardName: string
  userId: number
  authorName: string
  title: string
  summary: string
  coverUrl?: string
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  topFlag: boolean
  essenceFlag: boolean
  status: string
  publishTime: string
  createdAt: string
}

export interface PostDetail extends PostCard {
  content: string
  liked: boolean
  favorited: boolean
}

export interface CommentItem {
  id: number
  postId: number
  userId: number
  authorName: string
  parentId: number
  rootId: number
  replyToUserId?: number
  content: string
  likeCount: number
  liked: boolean
  createdAt: string
  replies: CommentItem[]
}

export interface PostQuery {
  boardId?: number
  keyword?: string
  sort?: string
  page?: number
  pageSize?: number
  status?: string
}

export interface SavePostPayload {
  boardId?: number
  title: string
  content: string
  coverUrl?: string
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

export const forumApi = {
  boards: () =>
    withMock(request.get<unknown, Board[]>('/api/forum/boards', mockConfig), () => mockBoards, emptyArray),
  posts: (params: PostQuery) =>
    withMock(request.get<unknown, PageResult<PostCard>>('/api/forum/posts', { params, ...mockConfig }), () => mockForumPage(params), emptyPage),
  hotPosts: () =>
    withMock(request.get<unknown, PostCard[]>('/api/forum/posts/hot', mockConfig), () => [...mockPosts].sort((a, b) => b.viewCount - a.viewCount).slice(0, 8), emptyArray),
  detail: (id: number) =>
    withMock(request.get<unknown, PostDetail>(`/api/forum/posts/${id}`, mockConfig), () => mockPostDetail(id)),
  createPost: (payload: SavePostPayload) =>
    withMock(request.post<unknown, PostDetail>('/api/forum/posts', payload, mockConfig), () => mockPostDetail(301)),
  updatePost: (id: number, payload: SavePostPayload) =>
    withMock(request.put<unknown, PostDetail>(`/api/forum/posts/${id}`, payload, mockConfig), () => ({ ...mockPostDetail(id), ...payload })),
  deletePost: (id: number) => withMock(request.delete<unknown, void>(`/api/forum/posts/${id}`, mockConfig), () => undefined),
  myPosts: (params: PostQuery) =>
    withMock(request.get<unknown, PageResult<PostCard>>('/api/forum/my/posts', { params, ...mockConfig }), () => mockForumPage(params, mockPosts.slice(0, 4)), emptyPage),
  myFavorites: (params: PostQuery) =>
    withMock(request.get<unknown, PageResult<PostCard>>('/api/forum/my/favorites', { params, ...mockConfig }), () => mockForumPage(params, mockPosts.filter((item) => item.favoriteCount > 80)), emptyPage),
  comments: (postId: number, params: { page?: number; pageSize?: number }) =>
    withMock(request.get<unknown, PageResult<CommentItem>>(`/api/forum/posts/${postId}/comments`, { params, ...mockConfig }), () => mockCommentPage(postId, params.page, params.pageSize), emptyPage),
  createComment: (postId: number, payload: { parentId?: number; content: string }) =>
    withMock(request.post<unknown, CommentItem>(`/api/forum/posts/${postId}/comments`, payload, mockConfig), () => ({
      id: Date.now(),
      postId,
      userId: 2,
      authorName: '浅芷芽',
      parentId: payload.parentId || 0,
      rootId: payload.parentId || Date.now(),
      content: payload.content,
      likeCount: 0,
      liked: false,
      createdAt: new Date().toISOString(),
      replies: []
    })),
  replies: (rootCommentId: number, params: { page?: number; pageSize?: number }) =>
    withMock(request.get<unknown, PageResult<CommentItem>>(`/api/forum/comments/${rootCommentId}/replies`, { params, ...mockConfig }), () => mockCommentPage(301, params.page, params.pageSize), emptyPage),
  likePost: (id: number) => withMock(request.post<unknown, void>(`/api/forum/posts/${id}/like`, undefined, mockConfig), () => undefined),
  unlikePost: (id: number) => withMock(request.delete<unknown, void>(`/api/forum/posts/${id}/like`, mockConfig), () => undefined),
  favoritePost: (id: number) => withMock(request.post<unknown, void>(`/api/forum/posts/${id}/favorite`, undefined, mockConfig), () => undefined),
  unfavoritePost: (id: number) => withMock(request.delete<unknown, void>(`/api/forum/posts/${id}/favorite`, mockConfig), () => undefined),
  likeComment: (id: number) => withMock(request.post<unknown, void>(`/api/forum/comments/${id}/like`, undefined, mockConfig), () => undefined),
  unlikeComment: (id: number) => withMock(request.delete<unknown, void>(`/api/forum/comments/${id}/like`, mockConfig), () => undefined)
}
