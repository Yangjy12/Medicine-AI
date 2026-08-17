import request from '../utils/request'
import { mockCheckin, mockLevelRules, mockPointsAccount, mockPointsRules, mockUser } from '../mockData'

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

export const userApi = {
  login: (payload: { account: string; password: string; deviceId?: string }) =>
    request.post<unknown, LoginResult>('/api/user/login', payload),
  register: (payload: { username: string; phone?: string; password: string }) =>
    request.post<unknown, UserInfo>('/api/user/register', payload),
  me: () => withMock(request.get<unknown, UserInfo>('/api/user/me', mockConfig), () => mockUser),
  logout: () => withMock(request.post<unknown, void>('/api/user/logout', undefined, mockConfig), () => undefined),
  checkin: () => withMock(request.post<unknown, CheckinResult>('/api/user/checkin', undefined, mockConfig), () => mockCheckin),
  pointsAccount: () => withMock(request.get<unknown, PointsAccount>('/api/user/points/account', mockConfig), () => mockPointsAccount),
  pointsRules: () => withMock(request.get<unknown, PointsRule[]>('/api/user/admin/rules/points', mockConfig), () => mockPointsRules, emptyArray),
  savePointsRule: (payload: PointsRule) => withMock(request.post<unknown, PointsRule>('/api/user/admin/rules/points', payload, mockConfig), () => ({ id: payload.id || Date.now(), ...payload })),
  levelRules: () => withMock(request.get<unknown, LevelRule[]>('/api/user/admin/rules/levels', mockConfig), () => mockLevelRules, emptyArray),
  saveLevelRule: (payload: LevelRule) => withMock(request.post<unknown, LevelRule>('/api/user/admin/rules/levels', payload, mockConfig), () => ({ id: payload.id || Date.now(), ...payload }))
}
