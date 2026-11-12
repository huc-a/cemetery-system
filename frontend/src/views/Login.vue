<template>
  <div class="wrap"><div class="card">
    <h1>🕯️ 家属登录</h1><div class="sub">青松陵园 · 云祭扫 / 选墓 / 预约 / 续费 一站式办理</div>
    <el-form @submit.prevent="submit">
      <el-form-item><el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" /></el-form-item>
      <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password @keyup.enter="submit" /></el-form-item>
      <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">登 录</el-button>
    </el-form>
    <div class="demo">演示账号（家属）：family / family123<br/>工作人员请使用右下角员工入口登录</div>
    <div class="link" @click="$router.push('/register')">没有账号？立即注册 →</div>
    <div class="link" @click="$router.push('/home')">← 先逛逛园区（游客浏览）</div>
  </div></div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
const form = reactive({ username: '', password: '' }); const loading = ref(false)
const auth = useAuthStore(); const router = useRouter(); const route = useRoute()
async function submit(){
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try { const u = await auth.login(form.username, form.password)
    if (u.role !== 'FAMILY') { auth.logout(); return ElMessage.error('该账号为工作人员账号，请使用员工入口登录') }
    ElMessage.success('欢迎回来，' + u.realName); router.push(route.query.redirect || '/home')
  } catch (e) {} finally { loading.value = false }
}
</script>
<style scoped>
.wrap{ min-height:100vh; display:flex; align-items:center; justify-content:center; background:url('/login-wallpaper.jpg') center/cover no-repeat, linear-gradient(135deg,#123128,#1f5747); padding:20px }
.card{ width:400px; max-width:94vw; background:rgba(255,255,255,.96); border-radius:16px; padding:32px 30px; box-shadow:0 18px 50px rgba(0,0,0,.35) }
h1{ margin:0 0 4px; color:#173f35; font-size:23px } .sub{ color:#8a8f8c; font-size:13px; margin-bottom:20px }
.demo{ background:#f7f4ec; border:1px dashed #c9a45c; border-radius:8px; padding:9px 12px; font-size:12px; color:#6b6250; margin-top:14px; line-height:1.7 }
.link{ text-align:center; margin-top:14px; font-size:13px; color:#1f5747; cursor:pointer }
</style>