import request from '../utils/request'

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

export const forumApi = {
  boards: () => request.get<unknown, Board[]>('/api/forum/boards'),
  posts: (params: PostQuery) => request.get<unknown, PageResult<PostCard>>('/api/forum/posts', { params }),
  hotPosts: () => request.get<unknown, PostCard[]>('/api/forum/posts/hot'),
  detail: (id: number) => request.get<unknown, PostDetail>(`/api/forum/posts/${id}`),
  createPost: (payload: SavePostPayload) => request.post<unknown, PostDetail>('/api/forum/posts', payload),
  updatePost: (id: number, payload: SavePostPayload) => request.put<unknown, PostDetail>(`/api/forum/posts/${id}`, payload),
  deletePost: (id: number) => request.delete<unknown, void>(`/api/forum/posts/${id}`),
  myPosts: (params: PostQuery) => request.get<unknown, PageResult<PostCard>>('/api/forum/my/posts', { params }),
  myFavorites: (params: PostQuery) => request.get<unknown, PageResult<PostCard>>('/api/forum/my/favorites', { params }),
  comments: (postId: number, params: { page?: number; pageSize?: number }) =>
    request.get<unknown, PageResult<CommentItem>>(`/api/forum/posts/${postId}/comments`, { params }),
  createComment: (postId: number, payload: { parentId?: number; content: string }) =>
    request.post<unknown, CommentItem>(`/api/forum/posts/${postId}/comments`, payload),
  replies: (rootCommentId: number, params: { page?: number; pageSize?: number }) =>
    request.get<unknown, PageResult<CommentItem>>(`/api/forum/comments/${rootCommentId}/replies`, { params }),
  likePost: (id: number) => request.post<unknown, void>(`/api/forum/posts/${id}/like`),
  unlikePost: (id: number) => request.delete<unknown, void>(`/api/forum/posts/${id}/like`),
  favoritePost: (id: number) => request.post<unknown, void>(`/api/forum/posts/${id}/favorite`),
  unfavoritePost: (id: number) => request.delete<unknown, void>(`/api/forum/posts/${id}/favorite`),
  likeComment: (id: number) => request.post<unknown, void>(`/api/forum/comments/${id}/like`),
  unlikeComment: (id: number) => request.delete<unknown, void>(`/api/forum/comments/${id}/like`)
}
