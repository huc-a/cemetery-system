<template>
  <div class="page-card">
    <div class="page-title">👥 系统用户管理</div><div class="page-sub">管理员维护管理员 / 工作人员 / 家属三类账号；家属账号即客户档案（联系方式、住址）。</div>
    <div class="toolbar">
      <el-select v-model="role" placeholder="全部角色" clearable style="width:140px" @change="load"><el-option value="ADMIN" label="管理员" /><el-option value="STAFF" label="工作人员" /><el-option value="FAMILY" label="家属/客户" /></el-select>
      <el-input v-model="kw" placeholder="姓名/用户名搜索" clearable style="width:190px" @keyup.enter="load" @clear="load" /><el-button @click="load">查询</el-button>
      <span class="spacer"></span><el-button type="primary" @click="open()">＋ 新增用户</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="用户" min-width="170"><template #default="s">{{ s.row.avatarEmoji || '👤' }} <b>{{ s.row.realName }}</b>（{{ s.row.username }}）</template></el-table-column>
      <el-table-column label="角色" width="110"><template #default="s"><el-tag :type="s.row.role==='ADMIN'?'danger':s.row.role==='STAFF'?'warning':'success'">{{ {ADMIN:'管理员',STAFF:'工作人员',FAMILY:'家属'}[s.row.role] }}</el-tag></template></el-table-column>
      <el-table-column prop="phone" label="手机" /><el-table-column prop="email" label="邮箱" /><el-table-column prop="address" label="住址" show-overflow-tooltip />
      <el-table-column label="状态" width="86"><template #default="s"><el-tag :type="s.row.status===1?'success':'info'">{{ s.row.status===1?'启用':'禁用' }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="注册时间" min-width="150" />
      <el-table-column label="操作" width="210"><template #default="s">
        <el-button size="small" @click="open(s.row)">编辑</el-button>
        <el-button size="small" @click="toggle(s.row)">{{ s.row.status===1?'禁用':'启用' }}</el-button>
        <el-button size="small" type="danger" plain @click="del(s.row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" :title="form.id?'编辑用户':'新增用户'" width="480px">
      <el-form label-width="76px">
        <el-form-item label="用户名" v-if="!form.id"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" v-if="!form.id"><el-input v-model="form.password" placeholder="不填默认 123456" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.role"><el-option value="ADMIN" label="管理员" /><el-option value="STAFF" label="工作人员" /><el-option value="FAMILY" label="家属/客户" /></el-select></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="住址"><el-input v-model="form.address" /></el-form-item>
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
const rows = ref([]); const role = ref(''); const kw = ref(''); const dlg = ref(false); const form = reactive({})
async function load(){ const r = await request.get('/users', { params: { role: role.value, kw: kw.value } }); rows.value = r.data }
function open(u){ Object.assign(form, u ? { ...u } : { username: '', password: '', realName: '', role: 'FAMILY', phone: '', email: '', address: '' }); dlg.value = true }
async function save(){ try { if (form.id) await request.put('/users/' + form.id, form); else await request.post('/users', form); ElMessage.success('已保存'); dlg.value = false; load() } catch (e) {} }
async function toggle(u){ try { await request.put(`/users/${u.id}/toggle`); ElMessage.success('已更新'); load() } catch (e) {} }
async function del(u){ await ElMessageBox.confirm('确认删除用户 ' + u.realName + '？', '提示'); try { await request.delete('/users/' + u.id); ElMessage.success('已删除'); load() } catch (e) {} }
onMounted(load)
</script>
