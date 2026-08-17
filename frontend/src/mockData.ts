import type { Board, CommentItem, PageResult as ForumPageResult, PostCard, PostDetail, PostQuery } from './api/forum'
import type { LevelRule, PointsAccount, PointsRule, UserInfo } from './api/user'
import type {
  Category,
  HomeData,
  PageResult as VideoPageResult,
  ProgressInfo,
  QueryParams,
  VideoCard,
  VideoDetail
} from './api/video'

const now = '2026-08-17T09:30:00'

export const mockCategories: Category[] = [
  { id: 1, name: '中医基础', icon: 'book-open', sort: 1, videoCount: 9, status: 1 },
  { id: 2, name: '针灸推拿', icon: 'activity', sort: 2, videoCount: 7, status: 1 },
  { id: 3, name: '方剂中药', icon: 'leaf', sort: 3, videoCount: 8, status: 1 },
  { id: 4, name: '经典研读', icon: 'scroll-text', sort: 4, videoCount: 6, status: 1 },
  { id: 5, name: '临证思维', icon: 'stethoscope', sort: 5, videoCount: 5, status: 1 },
  { id: 6, name: '养生康复', icon: 'heart-pulse', sort: 6, videoCount: 4, status: 1 }
]

export const mockVideos: VideoCard[] = [
  video(101, '阴阳五行入门：从四时气候理解辨证', 1, '中医基础', '秦知远', ['阴阳', '五行', '基础理论'], 2860, 12840, 842, 613, 72),
  video(102, '藏象学说精讲：脾胃为后天之本', 1, '中医基础', '林青禾', ['藏象', '脾胃', '气血津液'], 3540, 9801, 701, 542, 46),
  video(103, '八纲辨证案例拆解：寒热虚实怎么分', 1, '中医基础', '周明川', ['八纲辨证', '案例', '问诊'], 3120, 7610, 522, 438, 0),
  video(104, '经络循行速记：十二经脉学习地图', 2, '针灸推拿', '沈若岚', ['经络', '针灸', '腧穴'], 2980, 15320, 1098, 782, 64),
  video(105, '常用腧穴定位：合谷、足三里、三阴交', 2, '针灸推拿', '陆景行', ['腧穴定位', '实操', '针灸'], 2480, 18942, 1260, 920, 88),
  video(106, '推拿基础手法：揉、按、拿、滚的发力细节', 2, '针灸推拿', '唐书宁', ['推拿', '康复', '手法'], 2260, 6842, 405, 336, 28),
  video(107, '中药四气五味：药性与配伍的第一课', 3, '方剂中药', '许半夏', ['中药学', '四气五味', '配伍'], 3320, 11230, 803, 691, 51),
  video(108, '方剂组成逻辑：君臣佐使如何落到处方', 3, '方剂中药', '孟景明', ['方剂学', '君臣佐使', '处方'], 3760, 12330, 902, 744, 36),
  video(109, '桂枝汤精讲：从营卫不和到临床加减', 4, '经典研读', '方砚秋', ['伤寒论', '桂枝汤', '经方'], 4020, 16440, 1321, 1045, 59),
  video(110, '黄帝内经导读：气、形、神的整体观', 4, '经典研读', '卫南星', ['内经', '整体观', '经典'], 3880, 8610, 648, 512, 0),
  video(111, '咳嗽辨证训练：风寒、风热与痰湿', 5, '临证思维', '顾怀瑾', ['咳嗽', '辨证', '临床'], 3190, 7421, 566, 403, 22),
  video(112, '失眠调理思路：心脾两虚与肝郁化火', 5, '临证思维', '宋清和', ['失眠', '调理', '案例'], 3340, 9160, 711, 608, 0),
  video(113, '节气养生：立秋后的饮食与起居', 6, '养生康复', '温竹影', ['节气', '养生', '饮食'], 1960, 13320, 830, 760, 93),
  video(114, '办公室肩颈调理：穴位按揉与拉伸组合', 6, '养生康复', '韩知微', ['肩颈', '康复', '穴位'], 2120, 10204, 689, 577, 40)
]

export const mockUploadVideos: VideoCard[] = [
  { ...video(201, '用户共建：艾灸安全操作与禁忌', 2, '针灸推拿', '浅芷芽', ['艾灸', '安全', '共建课程'], 1800, 0, 0, 0, 0), status: 'DRAFT' },
  { ...video(202, '用户共建：本草笔记之陈皮', 3, '方剂中药', '浅芷芽', ['陈皮', '本草', '学习笔记'], 1540, 328, 26, 41, 0), status: 'ONLINE' },
  { ...video(203, '用户共建：晨间八段锦练习记录', 6, '养生康复', '浅芷芽', ['八段锦', '运动养生'], 1220, 94, 12, 17, 0), status: 'OFFLINE' }
]

export const mockHomeData: HomeData = {
  categories: mockCategories,
  recommended: mockVideos.slice(0, 6),
  hot: [...mockVideos].sort((a, b) => b.playCount - a.playCount).slice(0, 10),
  latest: [...mockVideos].reverse().slice(0, 8),
  continueLearning: mockVideos.filter((item) => item.progressPercent > 0).slice(0, 5)
}

export const mockProgress: ProgressInfo = {
  currentSecond: 680,
  progressPercent: 38,
  finished: false
}

export const mockBoards: Board[] = [
  { id: 1, name: '学习打卡', description: '记录每日学习与复盘', icon: 'calendar-check' },
  { id: 2, name: '经典共读', description: '内经、伤寒、金匮条文讨论', icon: 'book-marked' },
  { id: 3, name: '方药讨论', description: '中药与方剂学习交流', icon: 'leaf' },
  { id: 4, name: '针灸推拿', description: '经络腧穴与手法讨论', icon: 'activity' },
  { id: 5, name: '临证问答', description: '案例思路与辨证训练', icon: 'message-circle-question' },
  { id: 6, name: '养生康复', description: '节气养生与日常调理', icon: 'heart-pulse' }
]

export const mockPosts: PostCard[] = [
  post(301, 2, '《伤寒论》太阳病篇第一周共读笔记', '整理了桂枝汤证、麻黄汤证的鉴别点，以及我自己容易混淆的脉象描述。', 12862, 342, 96, 205, true, true),
  post(302, 5, '咳嗽辨证时，怎样把外感和内伤分清？', '最近做案例练习时发现病程、痰色、舌脉信息很关键，想和大家核对思路。', 8421, 218, 74, 136, false, true),
  post(303, 3, '方剂学习卡片：四君子汤为什么是补气基础方', '从君臣佐使、药性和临床加减三个角度复盘，附一张自制记忆表。', 7140, 186, 52, 110, false, false),
  post(304, 4, '足三里定位总是不稳，有没有靠谱练习方法？', '我把骨度分寸和体表标志结合了一下，欢迎指正。', 6921, 143, 48, 90, false, false),
  post(305, 1, '第 21 天打卡：把八纲辨证做成流程图以后清楚多了', '今天复盘寒热虚实、表里阴阳之间的层级关系，感觉终于能串起来。', 5310, 121, 39, 76, false, false),
  post(306, 6, '立秋后总觉得口干，饮食起居应该怎么调？', '结合课程里的节气养生内容，整理了一些润燥但不伤脾胃的做法。', 4820, 108, 31, 68, false, false),
  post(307, 2, '黄帝内经里“治未病”的学习笔记', '不只是预防疾病，更像是一套动态调整身心状态的方法论。', 4320, 97, 26, 51, false, false),
  post(308, 5, '失眠案例练习：心脾两虚和肝郁化火怎么辨？', '两个证型都有睡眠问题，但兼症和舌脉完全不同，欢迎大家补充。', 3965, 84, 22, 44, false, false)
]

export const mockComments: Record<number, CommentItem[]> = {
  301: [
    comment(4101, 301, '望山同学', '桂枝汤证那里我也容易忽略“汗出恶风”，这个点很关键。', 18, [
      comment(4102, 301, '浅芷芽', '是的，我后来把“汗出”和“无汗”单独标红了。', 6)
    ]),
    comment(4103, 301, '青囊笔记', '建议再补一个营卫不和的小结，后面看加减会更顺。', 12)
  ],
  302: [
    comment(4201, 302, '方寸之间', '我一般先看起病急缓，再看是否夹杂长期体质因素。', 11),
    comment(4202, 302, '云门', '痰湿咳嗽的舌苔信息很有帮助，别只看咳声。', 8)
  ]
}

export const mockPointsRules: PointsRule[] = [
  { id: 1, bizType: 'CHECKIN_DAILY', points: 5, description: '每日签到奖励', enabled: 1 },
  { id: 2, bizType: 'VIDEO_EFFECTIVE_PLAY', points: 10, description: '有效观看课程', enabled: 1 },
  { id: 3, bizType: 'VIDEO_FINISH', points: 20, description: '完成课程学习', enabled: 1 },
  { id: 4, bizType: 'POST_CREATE', points: 8, description: '发布论坛帖子', enabled: 1 },
  { id: 5, bizType: 'COMMENT_CREATE', points: 3, description: '参与评论讨论', enabled: 1 }
]

export const mockLevelRules: LevelRule[] = [
  { id: 1, level: 1, levelName: '初入杏林', minTotalPoints: 0, enabled: 1 },
  { id: 2, level: 2, levelName: '辨证入门', minTotalPoints: 100, enabled: 1 },
  { id: 3, level: 3, levelName: '经方研习', minTotalPoints: 300, enabled: 1 },
  { id: 4, level: 4, levelName: '临证精进', minTotalPoints: 800, enabled: 1 }
]

export const mockUser: UserInfo = {
  id: 2,
  username: 'qianzhiya',
  phoneMasked: '155****1769',
  nickname: '浅芷芽',
  level: 3,
  levelName: '经方研习',
  availablePoints: 268,
  totalPoints: 426,
  status: 'NORMAL',
  roles: ['USER'],
  learningDirection: '中医基础与方剂',
  city: '天津',
  bio: '正在系统学习中医基础、方剂与经典条文。'
}

export const mockPointsAccount: PointsAccount = {
  availablePoints: 268,
  totalPoints: 426,
  level: 3,
  levelName: '经方研习',
  nextLevelPoints: 800
}

export const mockCheckin = {
  checked: true,
  alreadyChecked: false,
  rewardPoints: 5,
  streakDays: 12,
  totalCheckedDays: 48
}

export const mockAiPresets = [
  '帮我制定中医基础学习计划',
  '解释阴阳五行和藏象的关系',
  '用案例讲清楚八纲辨证',
  '推荐适合初学者的经方课程',
  '整理针灸经络学习顺序',
  '生成今天的课程复盘提纲'
]

export const mockAiMessages = [
  {
    id: 1,
    role: 'assistant' as const,
    content: '你好，我是杏林助手。可以帮你做课程推荐、概念解释、学习计划和论坛讨论总结。'
  },
  {
    id: 2,
    role: 'user' as const,
    content: '我刚开始学中医，应该先看哪些内容？'
  },
  {
    id: 3,
    role: 'assistant' as const,
    content: '建议先按“中医基础 -> 八纲辨证 -> 中药四气五味 -> 方剂组成逻辑”的顺序学习。每天看 1 节课，配合 10 分钟笔记复盘，第二周再进入经络和经典导读。'
  }
]

export const mockConversations = [
  {
    id: 1,
    name: '中医基础学习群',
    preview: '今晚 8 点复盘八纲辨证',
    online: 18,
    messages: [
      { id: 1, side: 'peer' as const, content: '今天大家重点看表里寒热，别急着背结论，先看症状怎么归类。' },
      { id: 2, side: 'self' as const, content: '我把寒热虚实做成表格了，等会发到论坛一起改。' },
      { id: 3, side: 'peer' as const, content: '可以顺便加上舌象和脉象，案例题会更好用。' }
    ]
  },
  {
    id: 2,
    name: '针灸推拿研习',
    preview: '足三里定位练习打卡',
    online: 9,
    messages: [
      { id: 1, side: 'peer' as const, content: '足三里定位先找犊鼻，再向下三寸，别只凭感觉。' },
      { id: 2, side: 'self' as const, content: '我今天练了 5 次，左右腿距离感还是有点差。' }
    ]
  },
  {
    id: 3,
    name: '经方读书会',
    preview: '桂枝汤证和麻黄汤证对照',
    online: 12,
    messages: [
      { id: 1, side: 'peer' as const, content: '今晚读桂枝汤条文，大家先标出“汗出”和“恶风”。' },
      { id: 2, side: 'self' as const, content: '我想重点问营卫不和这个点，之前一直有点模糊。' }
    ]
  },
  {
    id: 4,
    name: '方药速记小组',
    preview: '陈皮、半夏、茯苓的配伍区别',
    online: 7,
    messages: [
      { id: 1, side: 'peer' as const, content: '二陈汤的药味可以先按“燥湿化痰、理气和中”理解。' },
      { id: 2, side: 'self' as const, content: '这样记比单背组成清楚多了。' }
    ]
  }
]

export function mockVideoPage(params: QueryParams = {}, source: VideoCard[] = mockVideos, onlyOnline = true): VideoPageResult<VideoCard> {
  const filtered = filterVideos(params, source, onlyOnline)
  return pageOf(filtered, params.page, params.pageSize || 12)
}

export function mockVideoDetail(id: number): VideoDetail {
  const base = [...mockVideos, ...mockUploadVideos].find((item) => item.id === id) || mockVideos[0]
  return {
    ...base,
    description: `${base.title}。课程围绕核心概念、学习路径和案例练习展开，适合在视频学习中心配合笔记反复复盘。`,
    videoUrl: 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
    liked: base.likeCount > 700,
    collected: base.collectCount > 600,
    progress: {
      currentSecond: Math.round(base.duration * ((base.progressPercent || 0) / 100)),
      progressPercent: base.progressPercent || 0,
      finished: Boolean(base.finished)
    },
    relatedVideos: mockVideos.filter((item) => item.categoryId === base.categoryId && item.id !== base.id).slice(0, 4)
  }
}

export function mockForumPage(params: PostQuery = {}, source: PostCard[] = mockPosts): ForumPageResult<PostCard> {
  let list = [...source]
  if (params.boardId) {
    list = list.filter((item) => item.boardId === params.boardId)
  }
  if (params.keyword?.trim()) {
    const keyword = params.keyword.trim().toLowerCase()
    list = list.filter((item) => `${item.title} ${item.summary} ${item.boardName}`.toLowerCase().includes(keyword))
  }
  if (params.sort === 'hot') {
    list.sort((a, b) => b.viewCount + b.likeCount * 20 - (a.viewCount + a.likeCount * 20))
  } else if (params.sort === 'mostViewed') {
    list.sort((a, b) => b.viewCount - a.viewCount)
  } else if (params.sort === 'mostCommented') {
    list.sort((a, b) => b.commentCount - a.commentCount)
  } else if (params.sort === 'mostLiked') {
    list.sort((a, b) => b.likeCount - a.likeCount)
  }
  return pageOf(list, params.page, params.pageSize || 12)
}

export function mockPostDetail(id: number): PostDetail {
  const base = mockPosts.find((item) => item.id === id) || mockPosts[0]
  return {
    ...base,
    content: `${base.summary}\n\n学习要点：\n1. 先确认核心概念，再回到课程案例里验证。\n2. 把舌脉、症状、病程拆开记录，减少凭感觉判断。\n3. 每次讨论后补充一条自己的辨证依据。`,
    liked: base.likeCount > 100,
    favorited: base.favoriteCount > 80
  }
}

export function mockCommentPage(postId: number, page = 1, pageSize = 20): ForumPageResult<CommentItem> {
  return pageOf(mockComments[postId] || mockComments[301], page, pageSize)
}

function video(
  id: number,
  title: string,
  categoryId: number,
  categoryName: string,
  lecturer: string,
  tags: string[],
  duration: number,
  playCount: number,
  likeCount: number,
  collectCount: number,
  progressPercent: number
): VideoCard {
  return {
    id,
    title,
    coverUrl: `https://images.unsplash.com/photo-${coverSeed(id)}?auto=format&fit=crop&w=900&q=80`,
    lecturer,
    categoryId,
    categoryName,
    tags,
    duration,
    playCount,
    likeCount,
    collectCount,
    progressPercent,
    finished: progressPercent >= 90,
    status: 'ONLINE',
    publishTime: now
  }
}

function post(
  id: number,
  boardId: number,
  title: string,
  summary: string,
  viewCount: number,
  likeCount: number,
  commentCount: number,
  favoriteCount: number,
  topFlag: boolean,
  essenceFlag: boolean
): PostCard {
  const board = mockBoards.find((item) => item.id === boardId) || mockBoards[0]
  return {
    id,
    boardId,
    boardName: board.name,
    userId: 2 + (id % 5),
    authorName: ['浅芷芽', '青囊笔记', '方寸之间', '云门', '望山同学'][id % 5],
    title,
    summary,
    viewCount,
    likeCount,
    commentCount,
    favoriteCount,
    topFlag,
    essenceFlag,
    status: 'PUBLISHED',
    publishTime: now,
    createdAt: now
  }
}

function comment(id: number, postId: number, authorName: string, content: string, likeCount: number, replies: CommentItem[] = []): CommentItem {
  return {
    id,
    postId,
    userId: id % 10,
    authorName,
    parentId: 0,
    rootId: id,
    content,
    likeCount,
    liked: likeCount > 10,
    createdAt: now,
    replies
  }
}

function filterVideos(params: QueryParams, source: VideoCard[], onlyOnline: boolean) {
  let list = onlyOnline ? source.filter((item) => item.status === 'ONLINE') : [...source]
  if (params.categoryId) {
    list = list.filter((item) => item.categoryId === params.categoryId)
  }
  if (params.keyword?.trim()) {
    const keyword = params.keyword.trim().toLowerCase()
    list = list.filter((item) => `${item.title} ${item.lecturer} ${item.categoryName} ${item.tags.join(' ')}`.toLowerCase().includes(keyword))
  }
  if (params.sort === 'latest') {
    list = [...list].reverse()
  } else if (params.sort === 'hottest') {
    list = [...list].sort((a, b) => b.playCount - a.playCount)
  } else if (params.sort === 'mostLiked') {
    list = [...list].sort((a, b) => b.likeCount - a.likeCount)
  } else if (params.sort === 'mostCollected') {
    list = [...list].sort((a, b) => b.collectCount - a.collectCount)
  }
  return list
}

function pageOf<T>(list: T[], pageValue = 1, sizeValue = 12) {
  const page = Math.max(1, pageValue || 1)
  const pageSize = Math.max(1, sizeValue || 12)
  const start = (page - 1) * pageSize
  const records = list.slice(start, start + pageSize)
  return {
    records,
    page,
    pageSize,
    total: list.length,
    pages: Math.max(1, Math.ceil(list.length / pageSize))
  }
}

function coverSeed(id: number) {
  const seeds = [
    '1511174511562-5f7f18b874f8',
    '1505751172876-fa1923c5c528',
    '1576091160550-2173dba999ef',
    '1526256262350-7da7584cf5eb',
    '1532938911079-1b06ac7ceec7',
    '1544161515-4ab6ce6db874'
  ]
  return seeds[id % seeds.length]
}
