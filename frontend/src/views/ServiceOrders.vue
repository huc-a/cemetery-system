<template>
  <div class="page-card">
    <div class="page-title">🧾 服务订单</div><div class="page-sub">{{ auth.isFamily ? '您订购的增值服务：可下单、支付；代祭扫完成后工作人员会回传结果说明。' : '流转：待派单 → 已派单 → 服务中 → 已完成（完成时填写回传说明）。' }}</div>
    <div class="toolbar">
      <el-select v-model="status" placeholder="全部状态" clearable style="width:130px" @change="load"><el-option v-for="(v,k) in ORDER_STATUS" :key="k" :value="k" :label="v" /></el-select>
      <el-button @click="load">刷新</el-button>
      <span class="spacer"></span><el-button type="primary" @click="openOrder">＋ 订购服务</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="150" /><el-table-column prop="productName" label="服务" min-width="150" />
      <el-table-column prop="familyName" label="家属" /><el-table-column prop="plotNo" label="墓位" />
      <el-table-column label="金额"><template #default="s"><span class="money">{{ money(s.row.totalAmount) }}</span></template></el-table-column>
      <el-table-column label="服务时间"><template #default="s">{{ s.row.serviceDate || '-' }} {{ s.row.timeSlot || '' }}</template></el-table-column>
      <el-table-column prop="staffName" label="服务人员" />
      <el-table-column label="状态" width="96"><template #default="s"><el-tag :type="s.row.status==='COMPLETED'?'success':s.row.status==='CANCELLED'?'info':'warning'">{{ ORDER_STATUS[s.row.status] }}</el-tag></template></el-table-column>
      <el-table-column label="支付" width="86"><template #default="s"><el-tag :type="s.row.payStatus==='PAID'?'success':'danger'">{{ s.row.payStatus==='PAID'?'已支付':'未支付' }}</el-tag></template></el-table-column>
      <el-table-column prop="resultNote" label="回传说明" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="270"><template #default="s">
        <el-button size="small" type="success" v-if="s.row.payStatus!=='PAID' && s.row.status!=='CANCELLED'" @click="pay(s.row)">支付</el-button>
        <el-button size="small" type="primary" v-if="auth.isStaffSide && s.row.status==='PENDING'" @click="assign(s.row)">派单</el-button>
        <el-button size="small" v-if="auth.isStaffSide && s.row.status==='ASSIGNED'" @click="start(s.row)">开始</el-button>
        <el-button size="small" type="primary" v-if="auth.isStaffSide && (s.row.status==='PROCESSING'||s.row.status==='ASSIGNED')" @click="complete(s.row)">完成</el-button>
        <el-button size="small" v-if="s.row.status!=='COMPLETED' && s.row.status!=='CANCELLED'" @click="cancel(s.row)">取消</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" title="订购服务" width="500px">
      <el-form label-width="86px">
        <el-form-item label="服务项目"><el-select v-model="form.productId" style="width:100%"><el-option v-for="s in svcList" :key="s.id" :value="s.id" :label="s.coverEmoji + ' ' + s.name + '（' + money(s.price) + '/' + s.unit + '）'" /></el-select></el-form-item>
        <el-form-item label="关联墓位"><el-select v-model="form.plotId" filterable clearable style="width:100%"><el-option v-for="p in plots" :key="p.id" :value="p.id" :label="p.plotNo" /></el-select></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="form.quantity" :min="1" /></el-form-item>
        <el-form-item label="服务日期"><el-date-picker v-model="form.serviceDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="时段"><el-select v-model="form.timeSlot"><el-option v-for="s in SLOTS" :key="s" :value="s" :label="s" /></el-select></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" placeholder="代祭扫可备注祭文内容等" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="saveOrder">提交订单</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { money, ORDER_STATUS, SLOTS } from '../utils/format'
const auth = useAuthStore()
const rows = ref([]); const status = ref(''); const dlg = ref(false); const svcList = ref([]); const plots = ref([]); const staffList = ref([])
const form = reactive({ productId: null, plotId: null, quantity: 1, serviceDate: '', timeSlot: '10:00-12:00', remark: '' })
async function load(){ const r = await request.get('/service-orders', { params: { status: status.value } }); rows.value = r.data }
function openOrder(){ dlg.value = true }
async function saveOrder(){ try { await request.post('/service-orders', form); ElMessage.success('下单成功，请及时支付'); dlg.value = false; load() } catch (e) {} }
async function pay(o){ try { await request.post(`/service-orders/${o.id}/pay`); ElMessage.success('支付成功'); load() } catch (e) {} }
async function assign(o){ const { value } = await ElMessageBox.prompt('输入服务人员用户ID（可从用户管理查看）', '派单', { inputValue: staffList.value[0]?.id || '' })
  try { await request.post(`/service-orders/${o.id}/assign`, { staffId: Number(value) }); ElMessage.success('已派单'); load() } catch (e) {} }
async function start(o){ try { await request.post(`/service-orders/${o.id}/start`); ElMessage.success('已开始服务'); load() } catch (e) {} }
async function complete(o){ const { value } = await ElMessageBox.prompt('填写服务完成说明（代祭扫回传给家属）', '完成服务', { inputValue: '服务已完成，现场照片已回传。' })
  try { await request.post(`/service-orders/${o.id}/complete`, { resultNote: value }); ElMessage.success('已完成'); load() } catch (e) {} }
async function cancel(o){ try { await request.post(`/service-orders/${o.id}/cancel`); ElMessage.success('已取消'); load() } catch (e) {} }
onMounted(async () => { load(); const s = await request.get(auth.user ? '/services' : '/public/services'); svcList.value = s.data.filter(x => x.status === 1)
  const p = await request.get('/plots').catch(() => null); if (p) plots.value = p.data
  if (auth.isStaffSide) { const st = await request.get('/users/staff'); staffList.value = st.data } })
</script>
