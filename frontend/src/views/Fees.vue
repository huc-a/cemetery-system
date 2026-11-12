<template>
  <div>
    <el-row :gutter="14" style="margin-bottom:14px">
      <el-col :span="8"><div class="alert-card red">⛔ 已逾期 <b>{{ alerts.overdueCount || 0 }}</b> 笔 · 金额 <b>{{ money(alerts.overdueAmount) }}</b></div></el-col>
      <el-col :span="8"><div class="alert-card orange">⏰ 30天内临期 <b>{{ alerts.dueSoonCount || 0 }}</b> 笔，请提前续缴</div></el-col>
      <el-col :span="8"><div class="alert-card green">💡 续缴后系统自动顺延下一管理周期，并生成财务流水</div></el-col>
    </el-row>
    <div class="page-card">
      <div class="page-title">💰 管理费与续费</div><div class="page-sub">管理费按年收取；到期状态由系统按到期日动态计算（逾期 / 临期 / 待缴 / 已缴）。</div>
      <div class="toolbar">
        <el-select v-model="status" placeholder="全部状态" clearable style="width:130px" @change="load">
          <el-option value="OVERDUE" label="已逾期" /><el-option value="DUE_SOON" label="临期" /><el-option value="UNPAID" label="待缴" /><el-option value="PAID" label="已缴" /></el-select>
        <el-button @click="load">刷新</el-button>
        <span class="spacer"></span><el-button v-if="auth.isStaffSide" type="primary" @click="openCreate">＋ 新增费用单</el-button>
      </div>
      <el-table :data="rows" stripe>
        <el-table-column prop="plotNo" label="墓位" /><el-table-column prop="familyName" label="家属" />
        <el-table-column label="年费 × 年限"><template #default="s">{{ money(s.row.yearlyAmount) }} × {{ s.row.years }} 年</template></el-table-column>
        <el-table-column label="应缴总额"><template #default="s"><span class="money">{{ money(s.row.totalAmount) }}</span></template></el-table-column>
        <el-table-column label="计费周期"><template #default="s">{{ s.row.periodStart }} ～ {{ s.row.periodEnd }}</template></el-table-column>
        <el-table-column prop="dueDate" label="到期日" />
        <el-table-column label="状态" width="96"><template #default="s"><el-tag :type="s.row.status==='PAID'?'success':s.row.status==='OVERDUE'?'danger':s.row.status==='DUE_SOON'?'warning':'info'">{{ FEE_STATUS[s.row.status] }}</el-tag></template></el-table-column>
        <el-table-column prop="remark" label="备注" min-width="170" show-overflow-tooltip />
        <el-table-column label="操作" width="130"><template #default="s">
          <el-button size="small" type="primary" v-if="s.row.status!=='PAID'" @click="renew(s.row)">续费缴纳</el-button>
        </template></el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="dlg" title="新增管理费单" width="460px">
      <el-form label-width="86px">
        <el-form-item label="墓位"><el-select v-model="form.plotId" filterable style="width:100%"><el-option v-for="p in plots" :key="p.id" :value="p.id" :label="p.plotNo + '（' + (p.buyerName || '未售') + '）'" /></el-select></el-form-item>
        <el-form-item label="家属"><el-select v-model="form.familyId" filterable style="width:100%"><el-option v-for="f in families" :key="f.id" :value="f.id" :label="f.realName" /></el-select></el-form-item>
        <el-form-item label="年限"><el-input-number v-model="form.years" :min="1" :max="30" /></el-form-item>
        <el-form-item label="周期开始"><el-date-picker v-model="form.periodStart" type="date" value-format="YYYY-MM-DD" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="saveCreate">创建</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { money, FEE_STATUS } from '../utils/format'
const auth = useAuthStore()
const rows = ref([]); const alerts = ref({}); const status = ref(''); const dlg = ref(false)
const plots = ref([]); const families = ref([]); const form = reactive({ plotId: null, familyId: null, years: 1, periodStart: '' })
async function load(){ const r = await request.get('/fees', { params: { status: status.value } }); rows.value = r.data
  const a = await request.get('/fees/alerts'); alerts.value = a.data }
async function renew(f){ await ElMessageBox.confirm(`确认为 ${f.plotNo} 缴纳管理费 ${money(f.totalAmount)}？缴清后自动顺延 ${f.years} 年周期。`, '续费确认')
  try { const r = await request.post(`/fees/${f.id}/renew`, { years: f.years, method: 'ONLINE' }); ElMessage.success('续费成功，下一周期到期日：' + r.data.dueDate); load() } catch (e) {} }
function openCreate(){ dlg.value = true }
async function saveCreate(){ try { await request.post('/fees', form); ElMessage.success('已创建'); dlg.value = false; load() } catch (e) {} }
onMounted(async () => { load(); if (auth.isStaffSide) { const p = await request.get('/plots'); plots.value = p.data; const f = await request.get('/users/families'); families.value = f.data } })
</script>
<style scoped>
.alert-card{ border-radius:12px; padding:16px; font-size:14px; color:#fff }
.alert-card.red{ background:linear-gradient(135deg,#a44a3f,#c05b4d) } .alert-card.orange{ background:linear-gradient(135deg,#b07d2b,#c9a45c) } .alert-card.green{ background:linear-gradient(135deg,#1f5747,#2a6d58) }
</style>
