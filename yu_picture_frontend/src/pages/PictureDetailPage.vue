<template>
  <div id="pictureDetailPage">
    <a-row :gutter="[16, 16]">
      <!-- 图片预览 -->
      <a-col :sm="24" :md="16" :xl="18">
        <a-card title="图片预览">
          <a-image :src="picture.url" style="max-height: 600px; object-fit: contain" />
        </a-card>
      </a-col>
      <!-- 图片信息区域 -->
      <a-col :sm="24" :md="8" :xl="6">
        <a-card title="图片信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="作者">
              <a-space>
                <a-avatar :size="24" :src="picture.user?.userAvatar" />
                <div>{{ picture.user?.userName }}</div>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="名称">
              {{ picture.name ?? '未命名' }}
            </a-descriptions-item>
            <a-descriptions-item label="简介">
              {{ picture.introduction ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="分类">
              {{ picture.category ?? '默认' }}
            </a-descriptions-item>
            <a-descriptions-item label="标签">
              <a-tag v-for="tag in picture.tags" :key="tag">
                {{ tag }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="格式">
              {{ picture.picFormat ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="宽度">
              {{ picture.picWidth ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="高度">
              {{ picture.picHeight ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="宽高比">
              {{ picture.picScale ?? '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="大小">
              {{ formatSize(picture.picSize) }}
            </a-descriptions-item>
            <a-descriptions-item label="浏览量">
              <a-space>
                <EyeOutlined />
                {{ picture.viewCount || 0 }}
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="主色调">
              <a-space>
                {{ picture.picColor ?? '-' }}
                <div
                  v-if="picture.picColor"
                  :style="{
                    width: '16px',
                    height: '16px',
                    backgroundColor: toHexColor(picture.picColor),
                  }"
                />
              </a-space>
            </a-descriptions-item>
          </a-descriptions>
          <!-- 图片操作 -->
          <a-space wrap>
            <a-button type="primary" @click="doDownload">
              免费下载
              <template #icon>
                <DownloadOutlined />
              </template>
            </a-button>
            <a-button :icon="h(ShareAltOutlined)" type="primary" ghost @click="doShare">
              分享
            </a-button>
            <a-button v-if="canEdit" :icon="h(EditOutlined)" type="default" @click="doEdit">
              编辑
            </a-button>
            <a-button v-if="canDelete" :icon="h(DeleteOutlined)" danger @click="doDelete">
              删除
            </a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 推荐相似图片 -->
    <a-divider style="margin: 24px 0;" />
    <a-card title="相似图片推荐" style="margin-bottom: 16px;">
      <a-spin :spinning="recommendLoading">
        <a-empty v-if="!recommendLoading && (!recommendedPictures || recommendedPictures.length === 0)" description="暂无推荐图片" />
        <a-row v-else :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="8" v-for="(pic, index) in recommendedPictures.slice(0, 3)" :key="index">
            <a-card 
              hoverable 
              @click.stop="goToPictureDetail(pic.id)"
              style="cursor: pointer;"
            >
              <template #cover>
                <img :src="pic.thumbUrl || pic.url" :alt="pic.name" style="height: 200px; object-fit: cover; width: 100%;" @click.stop="goToPictureDetail(pic.id)" />
              </template>
              <a-card-meta :title="pic.name || '未命名'" />
            </a-card>
          </a-col>
        </a-row>
      </a-spin>
    </a-card>
    
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { deletePictureUsingPost, getPictureVoByIdUsingGet, getSimilarPictureUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  EyeOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { downloadImage, formatSize, toHexColor } from '@/utils'
import ShareModal from '@/components/ShareModal.vue'
import { SPACE_PERMISSION_ENUM } from '@/constants/space.ts'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const picture = ref<API.PictureVO>({})
const recommendedPictures = ref<API.PictureVO[]>([])
const recommendLoading = ref(false)

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (picture.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canEdit = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDelete = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
      // 获取推荐图片
      await fetchRecommendedPictures()
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败：' + e.message)
  }
}

// 获取推荐相似图片
const fetchRecommendedPictures = async () => {
  if (!picture.value.id) return;
  
  recommendLoading.value = true;
  try {
    console.log('正在获取推荐图片，图片ID：', picture.value.id);
    const res = await getSimilarPictureUsingGet({
      pictureId: picture.value.id,
      limit: 3,
    });
    console.log('获取推荐图片响应：', res);
    console.log('响应数据：', res.data);
    if (res.data.code === 0 && res.data.data) {
      console.log('推荐图片数据：', res.data.data);
      recommendedPictures.value = res.data.data;
    } else {
      console.error('获取推荐图片失败：', res.data.message);
    }
  } catch (e: any) {
    console.error('获取推荐图片异常：', e);
    console.error('错误详情：', e.response?.data);
    console.error('请求URL：', e.config?.baseURL + e.config?.url);
    console.error('请求参数：', e.config?.params);
  } finally {
    recommendLoading.value = false;
  }
};

onMounted(() => {
  fetchPictureDetail()
})

const router = useRouter()

// 跳转到图片详情页
const goToPictureDetail = (id: number) => {
  console.log('点击推荐图片，ID：', id);
  console.log('准备跳转到：', `/picture/${id}`);
  window.location.href = `/picture/${id}`;
}

// 编辑
const doEdit = () => {
  router.push({
    path: '/add_picture',
    query: {
      id: picture.value.id,
      spaceId: picture.value.spaceId,
    },
  })
}

// 删除数据
const doDelete = async () => {
  const id = picture.value.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
  } else {
    message.error('删除失败')
  }
}

// 下载图片
const doDownload = () => {
  downloadImage(picture.value.url)
}

// ----- 分享操作 ----
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>()
// 分享
const doShare = () => {
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.value.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped>
#pictureDetailPage {
  margin-bottom: 16px;
}
</style>
