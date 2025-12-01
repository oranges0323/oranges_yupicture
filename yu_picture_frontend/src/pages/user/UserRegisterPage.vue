<template>
  <div id="userRegisterPage">
    <h2 class="title">云端协同AI图库 - 用户注册</h2>
    <div class="desc">企业级云端协同AI图库</div>
    <a-form
      :model="formState"
      name="basic"
      label-align="left"
      autocomplete="off"
      @finish="handleSubmit"
    >
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请输入确认密码' },
          { min: 8, message: '确认密码不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" />
      </a-form-item>
      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { userRegisterUsingPost } from '@/api/userController'

export default {
  setup() {
    const formState = reactive<API.UserRegisterRequest>({
      userAccount: '',
      userPassword: '',
      checkPassword: '',
    })

    const router = useRouter()

    const handleSubmit = async (values: any) => {
      if (formState.userPassword !== formState.checkPassword) {
        message.error('二次输入的密码不一致')
        return
      }
      try {
        const res = await userRegisterUsingPost(values)
        if (res.data.code === 0 && res.data.data) {
          message.success('注册成功')
          // 使用 setTimeout 确保消息显示后再跳转
          setTimeout(() => {
            router.push({
              path: '/user/login',
              replace: true,
            })
          }, 1000)
        } else {
          message.error('注册失败，' + res.data.message)
        }
      } catch (error) {
        console.error('注册请求失败:', error)
        message.error('注册请求失败，请稍后重试')
      }
    }

    return {
      formState,
      handleSubmit,
    }
  },
}
</script>
