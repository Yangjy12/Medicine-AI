<template>
  <section class="admin-page">
    <el-result v-if="!loggedIn" title="请先登录" sub-title="登录后可以上传课程视频">
      <template #extra>
        <el-button type="primary" @click="router.push('/login')">去登录</el-button>
      </template>
    </el-result>
    <el-result v-else-if="!isAdmin" title="无管理员权限" sub-title="普通用户请到我的上传管理自己提交的课程">
      <template #extra>
        <el-button type="primary" @click="router.push('/uploads')">去我的上传</el-button>
      </template>
    </el-result>

    <template v-else>
    <header class="simple-header">
      <p>运营后台</p>
      <h1>数据维护中心</h1>
    </header>

    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane v-if="isAdmin" label="视频分类" name="categories">
        <div class="admin-toolbar">
          <el-button type="primary" @click="openCategoryDialog()">新增分类</el-button>
          <el-button @click="loadCategories">刷新</el-button>
        </div>
        <el-table :data="categories" v-loading="categoryLoading" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="分类名称" min-width="160" />
          <el-table-column prop="icon" label="图标" min-width="160" />
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openCategoryDialog(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleCategory(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="课程视频" name="videos">
        <div class="admin-toolbar">
          <el-input v-model="videoQuery.keyword" clearable placeholder="搜索标题、讲师、标签" class="toolbar-input" @keyup.enter="loadVideos" />
          <el-select v-model="videoQuery.categoryId" clearable placeholder="全部分类" class="toolbar-select">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-button type="primary" @click="loadVideos">查询</el-button>
          <el-button @click="openVideoDialog()">新增课程</el-button>
        </div>
        <el-table :data="videos" v-loading="videoLoading" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" width="130" />
          <el-table-column prop="lecturer" label="讲师" width="130" />
          <el-table-column label="时长" width="100">
            <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="videoStatusTag(row.status)">{{ videoStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="播放/点赞/收藏" width="160">
            <template #default="{ row }">{{ row.playCount }} / {{ row.likeCount }} / {{ row.collectCount }}</template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openVideoDialog(row)">编辑</el-button>
              <el-button size="small" type="success" :disabled="row.status === 'ONLINE'" @click="setVideoOnline(row.id)">上线</el-button>
              <el-button size="small" type="warning" :disabled="row.status === 'OFFLINE'" @click="setVideoOffline(row.id)">下架</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="admin-pagination"
          layout="prev, pager, next, total"
          :page-size="videoQuery.pageSize"
          :current-page="videoQuery.page"
          :total="videoTotal"
          @current-change="changeVideoPage"
        />
      </el-tab-pane>

      <el-tab-pane v-if="isAdmin" label="用户规则" name="rules">
        <div class="rule-grid">
          <section class="admin-block">
            <div class="admin-block-title">
              <h2>积分规则</h2>
              <el-button type="primary" @click="openPointsRuleDialog()">新增规则</el-button>
            </div>
            <el-table :data="pointsRules" v-loading="ruleLoading" border>
              <el-table-column prop="bizType" label="业务类型" min-width="150" />
              <el-table-column prop="points" label="积分" width="90" />
              <el-table-column prop="description" label="说明" min-width="160" />
              <el-table-column label="状态" width="90">
                <template #default="{ row }">{{ row.enabled === 1 ? '启用' : '禁用' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="90">
                <template #default="{ row }">
                  <el-button size="small" @click="openPointsRuleDialog(row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </section>

          <section class="admin-block">
            <div class="admin-block-title">
              <h2>等级规则</h2>
              <el-button type="primary" @click="openLevelRuleDialog()">新增等级</el-button>
            </div>
            <el-table :data="levelRules" v-loading="ruleLoading" border>
              <el-table-column prop="level" label="等级" width="80" />
              <el-table-column prop="levelName" label="名称" min-width="130" />
              <el-table-column prop="minTotalPoints" label="最低累计积分" min-width="140" />
              <el-table-column label="状态" width="90">
                <template #default="{ row }">{{ row.enabled === 1 ? '启用' : '禁用' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="90">
                <template #default="{ row }">
                  <el-button size="small" @click="openLevelRuleDialog(row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </section>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="categoryDialogVisible" title="分类信息" width="520px">
      <el-form :model="categoryForm" label-position="top">
        <el-form-item label="分类名称"><el-input v-model="categoryForm.name" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="categoryForm.icon" placeholder="如 leaf、book-open" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="categoryForm.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="categoryForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="videoDialogVisible" title="课程信息" width="760px">
      <el-form :model="videoForm" label-position="top" class="video-form-grid">
        <el-form-item label="标题"><el-input v-model="videoForm.title" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="videoForm.categoryId" placeholder="请选择分类">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="讲师"><el-input v-model="videoForm.lecturer" /></el-form-item>
        <el-form-item label="时长秒数"><el-input-number v-model="videoForm.duration" :min="1" /></el-form-item>
        <el-form-item label="封面地址" class="form-wide"><el-input v-model="videoForm.coverUrl" /></el-form-item>
        <el-form-item label="视频地址" class="form-wide"><el-input v-model="videoForm.videoUrl" /></el-form-item>
        <el-form-item label="标签" class="form-wide"><el-input v-model="videoForm.tags" placeholder="多个标签用逗号分隔" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="videoForm.status">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="上线" value="ONLINE" />
            <el-option label="下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="简介" class="form-wide"><el-input v-model="videoForm.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="videoDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveVideo">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pointsRuleDialogVisible" title="积分规则" width="520px">
      <el-form :model="pointsRuleForm" label-position="top">
        <el-form-item label="业务类型"><el-input v-model="pointsRuleForm.bizType" /></el-form-item>
        <el-form-item label="积分"><el-input-number v-model="pointsRuleForm.points" :min="0" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="pointsRuleForm.description" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="pointsRuleForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsRuleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePointsRule">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="levelRuleDialogVisible" title="等级规则" width="520px">
      <el-form :model="levelRuleForm" label-position="top">
        <el-form-item label="等级"><el-input-number v-model="levelRuleForm.level" :min="1" /></el-form-item>
        <el-form-item label="等级名称"><el-input v-model="levelRuleForm.levelName" /></el-form-item>
        <el-form-item label="最低累计积分"><el-input-number v-model="levelRuleForm.minTotalPoints" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="levelRuleForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="levelRuleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveLevelRule">保存</el-button>
      </template>
    </el-dialog>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { type Category, type SaveCategoryPayload, type SaveVideoPayload, type VideoCard, videoApi } from '../api/video'
import { type LevelRule, type PointsRule, userApi } from '../api/user'
import { formatDuration } from '../utils/format'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loggedIn = computed(() => userStore.loggedIn)
const isAdmin = computed(() => userStore.isAdmin)
const activeTab = ref('categories')
const categoryLoading = ref(false)
const videoLoading = ref(false)
const ruleLoading = ref(false)
const saving = ref(false)

const categories = ref<Category[]>([])
const videos = ref<VideoCard[]>([])
const videoTotal = ref(0)
const pointsRules = ref<PointsRule[]>([])
const levelRules = ref<LevelRule[]>([])

const categoryDialogVisible = ref(false)
const videoDialogVisible = ref(false)
const pointsRuleDialogVisible = ref(false)
const levelRuleDialogVisible = ref(false)

const videoQuery = reactive({ keyword: '', categoryId: undefined as number | undefined, page: 1, pageSize: 10 })

const emptyCategoryForm = (): SaveCategoryPayload => ({ name: '', icon: '', sort: 0, status: 1 })
const emptyVideoForm = (): SaveVideoPayload => ({
  title: '',
  description: '',
  categoryId: undefined,
  lecturer: '',
  coverUrl: '',
  videoUrl: '',
  duration: 1,
  tags: '',
  status: 'DRAFT'
})
const emptyPointsRuleForm = (): PointsRule => ({ bizType: '', points: 0, description: '', enabled: 1 })
const emptyLevelRuleForm = (): LevelRule => ({ level: 1, levelName: '', minTotalPoints: 0, enabled: 1 })

const categoryForm = reactive<SaveCategoryPayload>(emptyCategoryForm())
const videoForm = reactive<SaveVideoPayload>(emptyVideoForm())
const pointsRuleForm = reactive<PointsRule>(emptyPointsRuleForm())
const levelRuleForm = reactive<LevelRule>(emptyLevelRuleForm())

const loadCategories = async () => {
  categoryLoading.value = true
  try {
    categories.value = await videoApi.adminCategories()
  } finally {
    categoryLoading.value = false
  }
}

const loadVideos = async () => {
  videoLoading.value = true
  try {
    const result = await videoApi.adminVideos(videoQuery)
    videos.value = result.records
    videoTotal.value = result.total
  } finally {
    videoLoading.value = false
  }
}

const loadRules = async () => {
  ruleLoading.value = true
  try {
    const [points, levels] = await Promise.all([userApi.pointsRules(), userApi.levelRules()])
    pointsRules.value = points
    levelRules.value = levels
  } finally {
    ruleLoading.value = false
  }
}

const openCategoryDialog = (row?: Category) => {
  Object.assign(categoryForm, emptyCategoryForm(), row ? { id: row.id, name: row.name, icon: row.icon, sort: row.sort, status: row.status } : {})
  categoryDialogVisible.value = true
}

const saveCategory = async () => {
  saving.value = true
  try {
    await videoApi.saveCategory(categoryForm)
    ElMessage.success('分类已保存')
    categoryDialogVisible.value = false
    await loadCategories()
  } finally {
    saving.value = false
  }
}

const toggleCategory = async (row: Category) => {
  if (row.status === 1) {
    await videoApi.disableCategory(row.id)
  } else {
    await videoApi.enableCategory(row.id)
  }
  await loadCategories()
}

const openVideoDialog = async (row?: VideoCard) => {
  Object.assign(videoForm, emptyVideoForm())
  if (row?.id) {
    const detail = await videoApi.adminVideoDetail(row.id)
    Object.assign(videoForm, {
      id: detail.id,
      title: detail.title,
      description: detail.description || '',
      categoryId: detail.categoryId,
      lecturer: detail.lecturer || '',
      coverUrl: detail.coverUrl || '',
      videoUrl: detail.videoUrl || '',
      duration: detail.duration || 1,
      tags: detail.tags?.join(',') || '',
      status: detail.status || 'DRAFT'
    })
  }
  videoDialogVisible.value = true
}

const saveVideo = async () => {
  saving.value = true
  try {
    await videoApi.saveVideo(videoForm)
    ElMessage.success('课程已保存')
    videoDialogVisible.value = false
    await loadVideos()
  } finally {
    saving.value = false
  }
}

const setVideoOnline = async (id: number) => {
  await videoApi.onlineVideo(id)
  await loadVideos()
}

const setVideoOffline = async (id: number) => {
  await videoApi.offlineVideo(id)
  await loadVideos()
}

const openPointsRuleDialog = (row?: PointsRule) => {
  Object.assign(pointsRuleForm, emptyPointsRuleForm(), row || {})
  pointsRuleDialogVisible.value = true
}

const savePointsRule = async () => {
  saving.value = true
  try {
    await userApi.savePointsRule(pointsRuleForm)
    ElMessage.success('积分规则已保存')
    pointsRuleDialogVisible.value = false
    await loadRules()
  } finally {
    saving.value = false
  }
}

const openLevelRuleDialog = (row?: LevelRule) => {
  Object.assign(levelRuleForm, emptyLevelRuleForm(), row || {})
  levelRuleDialogVisible.value = true
}

const saveLevelRule = async () => {
  saving.value = true
  try {
    await userApi.saveLevelRule(levelRuleForm)
    ElMessage.success('等级规则已保存')
    levelRuleDialogVisible.value = false
    await loadRules()
  } finally {
    saving.value = false
  }
}

const changeVideoPage = (page: number) => {
  videoQuery.page = page
  loadVideos()
}

const videoStatusText = (status: string) => {
  if (status === 'ONLINE') return '上线'
  if (status === 'OFFLINE') return '下架'
  return '草稿'
}

const videoStatusTag = (status: string) => {
  if (status === 'ONLINE') return 'success'
  if (status === 'OFFLINE') return 'warning'
  return 'info'
}

onMounted(async () => {
  if (!loggedIn.value || !isAdmin.value) {
    return
  }
  await Promise.all([loadCategories(), loadRules()])
  await loadVideos()
})
</script>
