<template>
  <div class="page-card">
    <div class="page-title">🛠️ 巡查养护工单</div><div class="page-sub">园区巡查、设施维修、绿化养护、墓位养护统一派单：待派单 → 已派单 → 处理中 → 已完成（完成必须填写处理结果）。</div>
    <div class="toolbar">
      <el-select v-model="status" placeholder="全部状态" clearable style="width:130px" @change="load"><el-option v-for="(v,k) in WO_STATUS" :key="k" :value="k" :label="v" /></el-select>
      <el-button @click="load">刷新</el-button><span class="spacer"></span><el-button type="primary" @click="dlg=true">＋ 新建工单</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="orderNo" label="工单号" min-width="145" />
      <el-table-column prop="title" label="标题" min-width="200" /><el-table-column prop="type" label="类型" />
      <el-table-column label="位置"><template #default="s">{{ s.row.areaName || '' }} {{ s.row.plotNo || '' }}</template></el-table-column>
      <el-table-column label="优先级" width="90"><template #default="s"><el-tag :type="s.row.priority==='HIGH'?'danger':s.row.priority==='LOW'?'info':'warning'">{{ {HIGH:'高',NORMAL:'中',LOW:'低'}[s.row.priority] }}</el-tag></template></el-table-column>
      <el-table-column prop="reporterName" label="上报人" /><el-table-column prop="assigneeName" label="处理人" />
      <el-table-column label="状态" width="96"><template #default="s"><el-tag :type="s.row.status==='COMPLETED'?'success':s.row.status==='CANCELLED'?'info':'warning'">{{ WO_STATUS[s.row.status] }}</el-tag></template></el-table-column>
      <el-table-column prop="resultNote" label="处理结果" min-width="170" show-overflow-tooltip />
      <el-table-column label="操作" width="230"><template #default="s">
        <el-button size="small" type="primary" v-if="auth.isStaffSide && s.row.status==='PENDING'" @click="assign(s.row)">派单</el-button>
        <el-button size="small" v-if="auth.isStaffSide && s.row.status==='ASSIGNED'" @click="start(s.row)">开始处理</el-button>
        <el-button size="small" type="success" v-if="auth.isStaffSide && (s.row.status==='PROCESSING'||s.row.status==='ASSIGNED')" @click="complete(s.row)">完成</el-button>
        <el-button size="small" v-if="auth.isStaffSide && s.row.status!=='COMPLETED' && s.row.status!=='CANCELLED'" @click="cancel(s.row)">取消</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" title="新建工单" width="500px">
      <el-form label-width="76px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type"><el-option v-for="t in types" :key="t" :value="t" :label="t" /></el-select></el-form-item>
        <el-form-item label="关联墓位"><el-select v-model="form.plotId" filterable clearable style="width:100%"><el-option v-for="p in plots" :key="p.id" :value="p.id" :label="p.plotNo + '（' + p.areaName + '）'" /></el-select></el-form-item>
        <el-form-item label="优先级"><el-radio-group v-model="form.priority"><el-radio value="HIGH">高</el-radio><el-radio value="NORMAL">中</el-radio><el-radio value="LOW">低</el-radio></el-radio-group></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">创建工单</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { WO_STATUS } from '../utils/format'
const auth = useAuthStore()
const types = ['环境巡查', '设施维修', '绿化养护', '墓位养护', '安全巡查', '家属报修']
const rows = ref([]); const status = ref(''); const dlg = ref(false); const plots = ref([]); const staffList = ref([])
const form = reactive({ title: '', type: '环境巡查', plotId: null, priority: 'NORMAL', description: '' })
async function load(){ const r = await request.get('/work-orders', { params: { status: status.value } }); rows.value = r.data }
async function save(){ try { await request.post('/work-orders', form); ElMessage.success('工单已创建'); dlg.value = false; load() } catch (e) {} }
async function assign(o){ const { value } = await ElMessageBox.prompt('输入处理人员用户ID', '派单', { inputValue: staffList.value.find(s => s.role === 'STAFF')?.id || '' })
  try { await request.post(`/work-orders/${o.id}/assign`, { assigneeId: Number(value) }); ElMessage.success('已派单'); load() } catch (e) {} }
async function start(o){ try { await request.post(`/work-orders/${o.id}/start`); ElMessage.success('已开始处理'); load() } catch (e) {} }
async function complete(o){ const { value } = await ElMessageBox.prompt('填写处理结果（必填）', '完成工单')
  try { await request.post(`/work-orders/${o.id}/complete`, { resultNote: value }); ElMessage.success('工单已完成'); load() } catch (e) {} }
async function cancel(o){ try { await request.post(`/work-orders/${o.id}/cancel`); ElMessage.success('已取消'); load() } catch (e) {} }
onMounted(async () => { load(); const p = await request.get('/plots').catch(() => null); if (p) plots.value = p.data
  if (auth.isStaffSide) { const st = await request.get('/users/staff'); staffList.value = st.data } })
</script>
