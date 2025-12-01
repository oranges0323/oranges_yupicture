<template>
  <div class="space-user-analyze">
    <a-flex gap="middle">
      <a-card title="存储空间" style="width: 50%">
        <div style="height: 320px; text-align: center">
          <h3>
            {{ formatSize(data.usedSize) }} /
            {{ data.maxSize ? formatSize(data.maxSize) : '无限制' }}
          </h3>
          <a-progress type="dashboard" :percent="data.sizeUsageRatio ?? 0" />
        </div>
      </a-card>
      <a-card title="图片数量" style="width: 50%">
        <div style="height: 320px; text-align: center">
          <h3>
            {{ data.usedCount }} / {{ data.maxCount ?? '无限制' }}
          </h3>
          <a-progress type="dashboard" :percent="data.countUsageRatio ?? 0" />
        </div>
      </a-card>
    </a-flex>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { getSpaceUsageAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import { formatSize } from '@/utils'

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: string
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

// 图表数据
const data = ref<API.SpaceUsageAnalyzeResponse>({})
// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 先测试一个简单的请求
    try {
      const testRes = await fetch('http://localhost:8123/api/space/analyze/usage', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          queryAll: props.queryAll,
          queryPublic: props.queryPublic,
          spaceId: props.spaceId,
        }),
        credentials: 'include'
      })
      console.log('测试请求响应：', testRes)
      const testData = await testRes.json()
      console.log('测试请求数据：', testData)
    } catch (testError) {
      console.error('测试请求错误：', testError)
    }
    
    // 转换搜索参数
    console.log('请求参数：', {
      queryAll: props.queryAll,
      queryPublic: props.queryPublic,
      spaceId: props.spaceId,
    })
    const res = await getSpaceUsageAnalyzeUsingPost({
      queryAll: props.queryAll,
      queryPublic: props.queryPublic,
      spaceId: props.spaceId,
    })
    console.log('响应数据：', res)
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data
      console.log('更新数据：', data.value)
    } else {
      message.error('获取数据失败，' + (res.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('请求出错：', error)
    message.error('请求出错，' + error)
  } finally {
    loading.value = false
  }
}

/**
 * 监听变量，参数改变时触发数据的重新加载
 */
watchEffect(() => {
  fetchData()
})
</script>

<style scoped></style>
