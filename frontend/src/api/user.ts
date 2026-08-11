import request from '../utils/request'

export interface UserInfo {
  id: number
  username: string
  phoneMasked?: string
  nickname: string
  avatar?: string
  level: number
  levelName: string
  availablePoints: number
  totalPoints: number
  status: string
  roles: string[]
  learningDirection?: string
  city?: string
  bio?: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserInfo
}

export interface CheckinResult {
  checked: boolean
  alreadyChecked: boolean
  rewardPoints: number
  streakDays: number
  totalCheckedDays: number
}

export interface PointsAccount {
  availablePoints: number
  totalPoints: number
  level: number
  levelName: string
  nextLevelPoints: number
}

export interface PointsRule {
  id?: number
  bizType: string
  points: number
  description?: string
  enabled: number
}

export interface LevelRule {
  id?: number
  level: number
  levelName: string
  minTotalPoints: number
  enabled: number
}

export const userApi = {
  login: (payload: { account: string; password: string; deviceId?: string }) =>
    request.post<unknown, LoginResult>('/api/user/login', payload),
  register: (payload: { username: string; phone?: string; password: string }) =>
    request.post<unknown, UserInfo>('/api/user/register', payload),
  me: () => request.get<unknown, UserInfo>('/api/user/me'),
  logout: () => request.post<unknown, void>('/api/user/logout'),
  checkin: () => request.post<unknown, CheckinResult>('/api/user/checkin'),
  pointsAccount: () => request.get<unknown, PointsAccount>('/api/user/points/account'),
  pointsRules: () => request.get<unknown, PointsRule[]>('/api/user/admin/rules/points'),
  savePointsRule: (payload: PointsRule) => request.post<unknown, PointsRule>('/api/user/admin/rules/points', payload),
  levelRules: () => request.get<unknown, LevelRule[]>('/api/user/admin/rules/levels'),
  saveLevelRule: (payload: LevelRule) => request.post<unknown, LevelRule>('/api/user/admin/rules/levels', payload)
}
