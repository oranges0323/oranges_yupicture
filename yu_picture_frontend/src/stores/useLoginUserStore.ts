import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUserUsingGet } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  /**
   * 获取登录用户信息的异步函数
   * 该函数用于从后端获取当前登录用户的信息并更新到本地状态
   */
  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet() // 调用后端API获取用户信息
    if (res.data.code === 0 && res.data.data) {
      // 检查API响应是否成功且包含数据
      loginUser.value = res.data.data // 将获取到的用户信息更新到本地状态
    }
    // //测试用户登录
    // setTimeout(()=>{
    //   loginUser.value = { userName: "测试用户",id : 1}  // 设置测试用户信息
    // },3000)  // 延迟3秒后执行测试用户设置
  }

  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
