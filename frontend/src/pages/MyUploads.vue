<template>
  <section class="admin-page">
    <el-result v-if="!loggedIn" title="请先登录" sub-title="登录后可以上传并管理自己的课程视频">
      <template #extra>
        <el-button type="primary" @click="router.push('/login')">去登录</el-button>
      </template>
    </el-result>

    <template v-else>
      <header class="simple-header">
        <p>课程共建</p>
        <h1>我的上传</h1>
      </header>

      <section class="admin-tabs">
        <div class="admin-toolbar">
          <el-button type="primary" @click="openUploadDialog">上传课程</el-button>
          <el-button @click="loadUploads">刷新状态</el-button>
        </div>

        <el-table :data="uploads" v-loading="loading" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" width="130" />
          <el-table-column prop="lecturer" label="讲师" width="130" />
          <el-table-column label="时长" width="100">
            <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="videoStatusTag(row.status)">{{ videoStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="数据" width="150">
            <template #default="{ row }">{{ row.playCount }} 播放 / {{ row.likeCount }} 赞</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 'ONLINE'" size="small" @click="router.push(`/videos/${row.id}`)">查看</el-button>
              <el-button size="small" type="danger" @click="deleteUpload(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && uploads.length === 0" description="暂无上传课程" />

        <el-pagination
          class="admin-pagination"
          layout="prev, pager, next, total"
          :page-size="query.pageSize"
          :current-page="query.page"
          :total="total"
          @current-change="changePage"
        />
      </section>

      <el-dialog v-model="dialogVisible" title="上传课程" width="760px">
        <el-form :model="uploadForm" label-position="top" class="video-form-grid">
          <el-form-item label="标题"><el-input v-model="uploadForm.title" /></el-form-item>
          <el-form-item label="分类">
            <el-select v-model="uploadForm.categoryId" placeholder="请选择分类">
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="讲师"><el-input v-model="uploadForm.lecturer" /></el-form-item>
          <el-form-item label="时长秒数"><el-input-number v-model="uploadForm.duration" :min="1" /></el-form-item>
          <el-form-item label="封面地址" class="form-wide"><el-input v-model="uploadForm.coverUrl" /></el-form-item>
          <el-form-item label="视频地址" class="form-wide"><el-input v-model="uploadForm.videoUrl" /></el-form-item>
          <el-form-item label="标签" class="form-wide"><el-input v-model="uploadForm.tags" placeholder="多个标签用逗号分隔" /></el-form-item>
          <el-form-item label="简介" class="form-wide"><el-input v-model="uploadForm.description" type="textarea" :rows="4" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitUpload">提交审核</el-button>
        </template>
      </el-dialog>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { type Category, type SaveVideoPayload, type VideoCard, videoApi } from '../api/video'
import { useUserStore } from '../stores/user'
import { formatDuration } from '../utils/format'

const router = useRouter()
const userStore = useUserStore()
const loggedIn = computed(() => userStore.loggedIn)

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const categories = ref<Category[]>([])
const uploads = ref<VideoCard[]>([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10 })

const emptyUploadForm = (): SaveVideoPayload => ({
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

const uploadForm = reactive<SaveVideoPayload>(emptyUploadForm())

const loadCategories = async () => {
  categories.value = await videoApi.categories()
}

const loadUploads = async () => {
  loading.value = true
  try {
    const result = await videoApi.myUploads(query)
    uploads.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

const openUploadDialog = () => {
  Object.assign(uploadForm, emptyUploadForm())
  dialogVisible.value = true
}

const submitUpload = async () => {
  saving.value = true
  try {
    await videoApi.uploadVideo({ ...uploadForm, status: 'DRAFT' })
    ElMessage.success('课程已提交，等待管理员审核')
    dialogVisible.value = false
    query.page = 1
    await loadUploads()
  } finally {
    saving.value = false
  }
}

const deleteUpload = async (id: number) => {
  try {
    await ElMessageBox.confirm('删除后该课程及相关学习记录会被移除，确认删除？', '删除课程', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await videoApi.deleteUpload(id)
    ElMessage.success('已删除')
    await loadUploads()
  } catch {
    // 用户取消删除时不需要额外提示。
  }
}

const changePage = (page: number) => {
  query.page = page
  loadUploads()
}

const videoStatusText = (status: string) => {
  if (status === 'ONLINE') return '已上线'
  if (status === 'OFFLINE') return '已下架'
  return '待审核'
}

const videoStatusTag = (status: string) => {
  if (status === 'ONLINE') return 'success'
  if (status === 'OFFLINE') return 'warning'
  return 'info'
}

onMounted(async () => {
  if (!loggedIn.value) {
    return
  }
  await Promise.all([loadCategories(), loadUploads()])
})
</script>
