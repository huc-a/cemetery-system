<template>
  <div class="page-card">
    <div class="page-title">📢 通知公告</div><div class="page-sub">园区祭扫安排、安全提示、收费提醒等官方公告。</div>
    <div class="toolbar" v-if="auth.isStaffSide"><span class="spacer"></span><el-button type="primary" @click="open()">＋ 发布公告</el-button></div>
    <div v-for="n in rows" :key="n.id" class="notice">
      <div class="nhead"><el-tag v-if="n.pinned" type="danger" size="small">置顶</el-tag><el-tag size="small" type="success">{{ typeName(n.type) }}</el-tag>
        <b>{{ n.title }}</b><span class="ntime">{{ n.createTime }} · {{ n.publisherName }}</span>
        <span class="spacer"></span>
        <el-button v-if="auth.isStaffSide" size="small" text @click="open(n)">编辑</el-button>
        <el-button v-if="auth.isAdmin" size="small" text type="danger" @click="del(n)">删除</el-button></div>
      <p class="nbody">{{ n.content }}</p>
    </div>
    <el-empty v-if="!rows.length" description="暂无公告" />
    <el-dialog v-model="dlg" :title="form.id?'编辑公告':'发布公告'" width="540px">
      <el-form label-width="70px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type"><el-option value="NOTICE" label="园区公告" /><el-option value="SAFETY" label="安全提示" /><el-option value="FEE" label="收费提醒" /><el-option value="SERVICE" label="服务动态" /></el-select>
          <el-checkbox v-model="form.pinned" :true-value="1" :false-value="0" style="margin-left:14px">置顶</el-checkbox></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
const auth = useAuthStore()
const rows = ref([]); const dlg = ref(false); const form = reactive({})
const typeName = t => ({ NOTICE: '园区公告', SAFETY: '安全提示', FEE: '收费提醒', SERVICE: '服务动态' }[t] || t)
async function load(){ const r = await request.get(auth.user ? '/notices' : '/public/notices'); rows.value = r.data }
function open(n){ Object.assign(form, n ? { ...n } : { title: '', type: 'NOTICE', pinned: 0, content: '' }); dlg.value = true }
async function save(){ try { if (form.id) await request.put('/notices/' + form.id, form); else await request.post('/notices', form); ElMessage.success('已保存'); dlg.value = false; load() } catch (e) {} }
async function del(n){ await ElMessageBox.confirm('确认删除公告？', '提示'); try { await request.delete('/notices/' + n.id); ElMessage.success('已删除'); load() } catch (e) {} }
onMounted(load)
</script>
<style scoped>
.notice{ border:1px solid #eee7d5; border-radius:12px; padding:14px 16px; margin-bottom:12px; background:#fffdf7 }
.nhead{ display:flex; align-items:center; gap:8px } .ntime{ color:#9aa39d; font-size:12px } .nbody{ color:#555; line-height:1.8; margin:8px 0 0; white-space:pre-wrap }
.spacer{ flex:1 }
</style>
