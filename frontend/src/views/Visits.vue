<template>
  <div class="page-card">
    <div class="page-title">🌼 祭扫预约</div><div class="page-sub">分时预约、限流接待：每个时段容量上限 20 人，约满自动拒绝，请错峰祭扫。工作人员可扫码核销入园。</div>
    <div class="toolbar">
      <el-date-picker v-model="qdate" type="date" value-format="YYYY-MM-DD" placeholder="查看某日时段余量" @change="loadSlots" />
      <span v-for="s in slotInfo" :key="s.slot" class="slot-chip" :class="{full: s.remaining<=0}">{{ s.slot }}：余 {{ s.remaining }}/{{ s.capacity }}</span>
      <span class="spacer"></span><el-button type="primary" @click="open()">＋ 新建预约</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="familyName" label="预约人" /><el-table-column prop="deceasedName" label="祭扫对象" /><el-table-column prop="plotNo" label="墓位" />
      <el-table-column label="时间"><template #default="s">{{ s.row.visitDate }} {{ s.row.timeSlot }}</template></el-table-column>
      <el-table-column prop="visitors" label="人数" />
      <el-table-column label="状态" width="96"><template #default="s"><el-tag :type="s.row.status==='CHECKED_IN'?'success':s.row.status==='CANCELLED'?'info':'warning'">{{ VISIT_STATUS[s.row.status] }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="170"><template #default="s">
        <el-button size="small" type="primary" v-if="auth.isStaffSide && s.row.status==='BOOKED'" @click="checkin(s.row)">核销入园</el-button>
        <el-button size="small" v-if="s.row.status==='BOOKED'" @click="cancel(s.row)">取消</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" title="祭扫预约" width="480px">
      <el-form label-width="86px">
        <el-form-item label="祭扫对象"><el-select v-model="form.deceasedName" filterable allow-create placeholder="选择或输入逝者姓名" style="width:100%">
          <el-option v-for="d in decList" :key="d.id" :value="d.name" :label="d.name + '（' + (d.plotNo || '-') + '）'" @click="pickDec(d)" /></el-select></el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD" @change="loadFormSlots" /></el-form-item>
        <el-form-item label="时段"><el-select v-model="form.timeSlot" style="width:100%">
          <el-option v-for="s in formSlots" :key="s.slot" :value="s.slot" :label="s.slot + '（余 ' + s.remaining + ' 人）'" :disabled="s.remaining<=0" /></el-select></el-form-item>
        <el-form-item label="人数"><el-input-number v-model="form.visitors" :min="1" :max="20" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" placeholder="如 需祭扫用品请提前备注" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">提交预约</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { VISIT_STATUS } from '../utils/format'
const auth = useAuthStore()
const rows = ref([]); const dlg = ref(false); const decList = ref([])
const today = new Date().toISOString().slice(0, 10)
const qdate = ref(today); const slotInfo = ref([]); const formSlots = ref([])
const form = reactive({ deceasedName: '', plotId: null, visitDate: today, timeSlot: '08:00-10:00', visitors: 2, remark: '' })
async function load(){ const r = await request.get('/visits'); rows.value = r.data }
async function loadSlots(){ if (!qdate.value) return; const r = await request.get('/visits/slots', { params: { date: qdate.value } }); slotInfo.value = r.data }
async function loadFormSlots(){ if (!form.visitDate) return; const r = await request.get('/visits/slots', { params: { date: form.visitDate } }); formSlots.value = r.data }
function pickDec(d){ form.plotId = d.plotId }
function open(){ dlg.value = true; loadFormSlots() }
async function save(){ try { await request.post('/visits', form); ElMessage.success('预约成功，请按时段入园'); dlg.value = false; load(); loadSlots() } catch (e) {} }
async function checkin(v){ try { await request.post(`/visits/${v.id}/checkin`); ElMessage.success('已核销入园'); load() } catch (e) {} }
async function cancel(v){ try { await request.post(`/visits/${v.id}/cancel`); ElMessage.success('已取消'); load(); loadSlots() } catch (e) {} }
onMounted(async () => { load(); loadSlots(); const d = await request.get('/deceased'); decList.value = d.data })
</script>
<style scoped>
.slot-chip{ background:#eef4f0; color:#1f5747; border-radius:16px; padding:4px 12px; font-size:12px } .slot-chip.full{ background:#f7e8e5; color:#a44a3f }
</style>
