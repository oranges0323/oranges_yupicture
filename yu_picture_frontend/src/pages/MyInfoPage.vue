<template>
  <div id="myInfoPage">
    <h2 style="margin-bottom: 16px">个人信息</h2>

    <!-- 用户头像部分 -->
    <div class="avatar-section">
      <a-upload
        :show-upload-list="false"
        :before-upload="beforeUpload"
        :custom-request="handleUploadAvatar"
      >
        <a-avatar :size="100" :src="userInfo.userAvatar" class="avatar">
          <template #icon><UserOutlined /></template>
        </a-avatar>
        <div class="avatar-tip">点击更换头像</div>
      </a-upload>
    </div>

    <!-- 用户信息展示 -->
    <div class="my_info-display">
      <div class="my_info-item">
        <span class="my_info-label">ID：</span>
        <span class="my_info-value">{{ userInfo.id }}</span>
      </div>
      <div class="my_info-item">
        <span class="my_info-label">账号：</span>
        <span class="my_info-value">{{ userInfo.userAccount }}</span>
      </div>
      <div class="my_info-item">
        <span class="my_info-label">用户名：</span>
        <span class="my_info-value">{{ userInfo.userName }}</span>
      </div>
      <div class="my_info-item">
        <span class="my_info-label">用户角色：</span>
        <span class="my_info-value">{{ userInfo.userRole }}</span>
      </div>
      <div class="my_info-item">
        <span class="my_info-label">个人简介：</span>
        <span class="my_info-value">{{ userInfo.userProfile || "暂无简介" }}</span>
      </div>
      <div class="my_info-item">
        <span class="my_info-label">创建时间：</span>
        <span class="my_info-value">{{ formatTime(userInfo.createTime) }}</span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <a-button type="primary" @click="showEditProfileModal">编辑资料</a-button>
      <a-button @click="showChangePasswordModal">修改密码</a-button>
    </div>

    <!-- 编辑资料弹窗 -->
    <a-modal
      v-model:open="editProfileVisible"
      title="编辑个人资料"
      @ok="handleEditProfile"
      :confirm-loading="editProfileLoading"
    >
      <a-form
        :model="editForm"
        :rules="editRules"
        layout="vertical"
        ref="editFormRef"
      >
        <a-form-item name="userName" label="用户名">
          <a-input v-model:value="editForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item name="userProfile" label="个人简介">
          <a-textarea
            v-model:value="editForm.userProfile"
            placeholder="请输入个人简介"
            :auto-size="{ minRows: 2, maxRows: 5 }"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 修改密码弹窗 -->
    <a-modal
      v-model:open="changePasswordVisible"
      title="修改密码"
      @ok="handleChangePassword"
      :confirm-loading="changePasswordLoading"
    >
      <a-form
        :model="passwordForm"
        :rules="passwordRules"
        layout="vertical"
        ref="passwordFormRef"
      >
        <a-form-item name="newPassword" label="新密码">
          <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item name="confirmPassword" label="确认新密码">
          <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UserOutlined } from '@ant-design/icons-vue'
import type { FormInstance } from 'ant-design-vue'
import {
  getCurrentUserInfoUsingGet,
  updateUserInfoUsingPost,
  updateUserAvatarUsingPost
} from '@/api/userController'

// 用户信息
const userInfo = ref<API.UserVO>({})

// 编辑资料相关
const editProfileVisible = ref(false)
const editProfileLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive<API.UserUpdateInfoRequest>({
  userName: '',
  userProfile: ''
})

// 修改密码相关
const changePasswordVisible = ref(false)
const changePasswordLoading = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 编辑资料表单验证规则
const editRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userProfile: [
    { max: 200, message: '个人简介不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 修改密码表单验证规则
const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (value !== passwordForm.newPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ]
}

// 获取当前用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getCurrentUserInfoUsingGet()
    if (res.data.code === 0 && res.data.data) {
      userInfo.value = res.data.data
    } else {
      message.error('获取用户信息失败：' + (res.data.message || '未知错误'))
    }
  } catch (error) {
    message.error('获取用户信息失败：' + error)
  }
}

// 上传头像前的检查
const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传头像
const handleUploadAvatar = async (options: any) => {
  const { file } = options
  try {
    const res = await updateUserAvatarUsingPost({}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('头像更新成功')
      // 更新本地用户信息
      userInfo.value.userAvatar = res.data.data.userAvatar
    } else {
      message.error('头像更新失败：' + (res.data.message || '未知错误'))
    }
  } catch (error) {
    message.error('头像更新失败：' + error)
  }
}

// 显示编辑资料弹窗
const showEditProfileModal = () => {
  // 填充表单
  editForm.userName = userInfo.value.userName || ''
  editForm.userProfile = userInfo.value.userProfile || ''
  editProfileVisible.value = true
}

// 处理编辑资料
const handleEditProfile = async () => {
  try {
    await editFormRef.value?.validate()
    editProfileLoading.value = true

    const res = await updateUserInfoUsingPost(editForm)
    if (res.data.code === 0 && res.data.data) {
      message.success('个人信息更新成功')
      // 更新本地用户信息
      userInfo.value = res.data.data
      editProfileVisible.value = false
    } else {
      message.error('信息更新失败：' + (res.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    message.error('更新失败，请重试')
  } finally {
    editProfileLoading.value = false
  }
}

// 显示修改密码弹窗
const showChangePasswordModal = () => {
  // 重置表单
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  changePasswordVisible.value = true
}

// 处理修改密码 - 使用现有的updateUserInfo API
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value?.validate()
    changePasswordLoading.value = true

    // 使用现有的updateUserInfo API，只传递密码相关字段
    const res = await updateUserInfoUsingPost({
      newPassword: passwordForm.newPassword
    })

    if (res.data.code === 0) {
      message.success('密码修改成功')
      changePasswordVisible.value = false
    } else {
      message.error('密码修改失败：' + (res.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    message.error('修改失败，请重试')
  } finally {
    changePasswordLoading.value = false
  }
}

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
#myInfoPage {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.avatar {
  cursor: pointer;
  border: 1px dashed #d9d9d9;
  transition: all 0.3s;
}

.avatar:hover {
  border-color: #1890ff;
}

.avatar-tip {
  margin-top: 8px;
  color: #999;
  font-size: 14px;
}

.my_info-display {
  background-color: #f5f5f5;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 24px;
}

.my_info-item {
  margin-bottom: 12px;
  display: flex;
}

.my_info-item:last-child {
  margin-bottom: 0;
}

.my_info-label {
  font-weight: 500;
  width: 100px;
  color: #666;
}

.my_info-value {
  flex: 1;
  color: #333;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}
</style>
