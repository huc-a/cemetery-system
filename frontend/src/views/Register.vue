<template>
  <div class="wrap"><div class="card">
    <h1>🌿 家属注册</h1><div class="sub">注册后即可在线选墓、祭扫预约、云祭扫与管理费续缴</div>
    <el-form label-width="72px" @submit.prevent="submit">
      <el-form-item label="用户名"><el-input v-model="form.username" placeholder="登录用户名" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" type="password" placeholder="至少6位" show-password /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.realName" placeholder="真实姓名" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="手机号" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="选填" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" placeholder="选填" /></el-form-item>
      <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">注 册</el-button>
    </el-form>
    <div class="link" @click="$router.push('/login')">已有账号？去登录 →</div>
  </div></div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
const form = reactive({ username: '', password: '', realName: '', phone: '', email: '', address: '' }); const loading = ref(false)
const router = useRouter()
async function submit(){
  if (!form.username || !form.password || !form.realName) return ElMessage.warning('用户名、密码、姓名必填')
  if (form.password.length < 6) return ElMessage.warning('密码至少6位')
  loading.value = true
  try { await request.post('/auth/register', form); ElMessage.success('注册成功，请登录'); router.push('/login') }
  catch (e) {} finally { loading.value = false }
}
</script>
<style scoped>
.wrap{ min-height:100vh; display:flex; align-items:center; justify-content:center; background:url('/login-wallpaper.jpg') center/cover no-repeat, linear-gradient(135deg,#123128,#1f5747); padding:20px }
.card{ width:400px; max-width:94vw; background:rgba(255,255,255,.96); border-radius:16px; padding:32px 30px; box-shadow:0 18px 50px rgba(0,0,0,.35) }
h1{ margin:0 0 4px; color:#173f35; font-size:23px } .sub{ color:#8a8f8c; font-size:13px; margin-bottom:20px }
.demo{ background:#f7f4ec; border:1px dashed #c9a45c; border-radius:8px; padding:9px 12px; font-size:12px; color:#6b6250; margin-top:14px; line-height:1.7 }
.link{ text-align:center; margin-top:14px; font-size:13px; color:#1f5747; cursor:pointer }
</style>