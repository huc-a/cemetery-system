<template>
  <div class="page-card">
    <div class="page-title">⚱️ 安葬预约与登记</div><div class="page-sub">已售墓位方可预约安葬（同墓位同时段冲突将拒绝）；工作人员登记完成后墓位状态变为「已安葬」。</div>
    <div class="toolbar">
      <el-select v-model="status" placeholder="全部状态" clearable style="width:130px" @change="load"><el-option v-for="(v,k) in BURIAL_STATUS" :key="k" :value="k" :label="v" /></el-select>
      <el-button @click="load">刷新</el-button><span class="spacer"></span><el-button type="primary" @click="open()">＋ 预约安葬</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="deceasedName" label="逝者" /><el-table-column prop="plotNo" label="墓位" /><el-table-column prop="familyName" label="家属" />
      <el-table-column label="安葬时间"><template #default="s">{{ s.row.burialDate }} {{ s.row.timeSlot }}</template></el-table-column>
      <el-table-column prop="ceremonyType" label="仪式类型" />
      <el-table-column label="状态" width="96"><template #default="s"><el-tag :type="s.row.status==='COMPLETED'?'success':s.row.status==='CANCELLED'?'info':'warning'">{{ BURIAL_STATUS[s.row.status] }}</el-tag></template></el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column label="操作" width="190"><template #default="s">
        <el-button size="small" type="primary" v-if="auth.isStaffSide && s.row.status==='BOOKED'" @click="complete(s.row)">登记完成</el-button>
        <el-button size="small" v-if="s.row.status==='BOOKED'" @click="cancel(s.row)">取消</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" title="预约安葬" width="500px">
      <el-form label-width="86px">
        <el-form-item label="逝者档案"><el-select v-model="form.deceasedId" filterable style="width:100%"><el-option v-for="d in decList" :key="d.id" :value="d.id" :label="d.name + '（墓位 ' + (d.plotNo || '未绑定') + '）'" /></el-select></el-form-item>
        <el-form-item label="安葬墓位"><el-select v-model="form.plotId" filterable style="width:100%"><el-option v-for="p in soldPlots" :key="p.id" :value="p.id" :label="p.plotNo + '（' + p.areaName + '）'" /></el-select></el-form-item>
        <el-form-item label="安葬日期"><el-date-picker v-model="form.burialDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="时段"><el-select v-model="form.timeSlot"><el-option v-for="s in SLOTS" :key="s" :value="s" :label="s" /></el-select></el-form-item>
        <el-form-item label="仪式类型"><el-select v-model="form.ceremonyType"><el-option value="传统告别仪式" label="传统告别仪式" /><el-option value="简约安葬仪式" label="简约安葬仪式" /><el-option value="生态安葬仪式" label="生态安葬仪式" /></el-select></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">提交预约</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { BURIAL_STATUS, SLOTS } from '../utils/format'
const auth = useAuthStore()
const rows = ref([]); const status = ref(''); const dlg = ref(false)
const decList = ref([]); const soldPlots = ref([])
const form = reactive({ deceasedId: null, plotId: null, burialDate: '', timeSlot: '10:00-12:00', ceremonyType: '传统告别仪式', remark: '' })
async function load(){ const r = await request.get('/burials', { params: { status: status.value } }); rows.value = r.data }
function open(){ dlg.value = true }
async function save(){ try { await request.post('/burials', form); ElMessage.success('安葬预约成功'); dlg.value = false; load() } catch (e) {} }
async function complete(b){ await ElMessageBox.confirm('确认登记 ' + b.deceasedName + ' 安葬完成？墓位将变为已安葬。', '提示'); try { await request.post(`/burials/${b.id}/complete`); ElMessage.success('已登记完成'); load() } catch (e) {} }
async function cancel(b){ try { await request.post(`/burials/${b.id}/cancel`); ElMessage.success('已取消'); load() } catch (e) {} }
onMounted(async () => { load(); const d = await request.get('/deceased'); decList.value = d.data; const p = await request.get('/plots'); soldPlots.value = p.data.filter(x => ['SOLD', 'BURIED'].includes(x.status)) })
</script>
