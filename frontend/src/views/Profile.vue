<template>
  <div class="page-card" style="max-width:640px">
    <div class="page-title">⚙️ 个人中心</div><div class="page-sub"><img v-if="auth.user?.avatarUrl" class="avatar" :src="auth.user.avatarUrl" /><span v-else>{{ auth.user?.avatarEmoji }} </span>{{ auth.user?.realName }} · {{ {ADMIN:'管理员',STAFF:'园区工作人员',FAMILY:'家属'}[auth.role] }}（用户名 {{ auth.user?.username }}）</div>
    <el-form label-width="76px">
      <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
      <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      <el-form-item label="住址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="头像"><ImageUpload v-model="form.avatarUrl" :size="80" /></el-form-item>
      <el-button type="primary" @click="save">保存资料</el-button>
    </el-form>
    <el-divider />
    <div class="page-title" style="font-size:16px">修改密码</div>
    <el-form label-width="76px" style="margin-top:12px">
      <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
      <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
      <el-button @click="changePwd">修改密码</el-button>
    </el-form>
  </div>
</template>
<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import ImageUpload from '../components/ImageUpload.vue'
const auth = useAuthStore()
const form = reactive({ realName: '', phone: '', email: '', address: '', avatarEmoji: '', avatarUrl: '' })
const pwd = reactive({ oldPassword: '', newPassword: '' })
async function save(){ const r = await request.put('/auth/profile', form); auth.user = r.data; localStorage.setItem('cem_user', JSON.stringify(r.data)); ElMessage.success('已保存') }
async function changePwd(){ if (pwd.newPassword.length < 6) return ElMessage.warning('新密码至少6位')
  await request.post('/auth/change-password', pwd); ElMessage.success('密码已修改'); pwd.oldPassword = ''; pwd.newPassword = '' }
onMounted(async () => { const r = await request.get('/auth/me'); Object.assign(form, r.data) })
</script>

<style scoped>
.avatar{ width:26px; height:26px; border-radius:50%; object-fit:cover; vertical-align:-7px; margin-right:4px }
</style>
