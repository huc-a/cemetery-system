<template>
  <div class="page-card">
    <div class="page-title">📜 购墓合同 / 订单</div><div class="page-sub">业务流转：选墓生成合同（待签约）→ 签约（墓位变已售）→ 缴费（可分次，缴清生成管理费周期）。已售墓位不可重复购买。</div>
    <div class="toolbar">
      <el-select v-model="status" placeholder="全部状态" clearable style="width:130px" @change="load"><el-option v-for="(v,k) in CONTRACT_STATUS" :key="k" :value="k" :label="v" /></el-select>
      <el-input v-model="kw" placeholder="合同号 / 购买人 / 墓位" clearable style="width:210px" @keyup.enter="load" @clear="load" /><el-button @click="load">查询</el-button>
      <span class="spacer"></span><el-button type="primary" @click="$router.push('/plot-map')">＋ 去选墓下单</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="contractNo" label="合同号" min-width="150" /><el-table-column prop="plotNo" label="墓位" />
      <el-table-column prop="buyerName" label="购买人" /><el-table-column prop="deceasedName" label="拟安葬逝者" />
      <el-table-column label="总金额"><template #default="s"><span class="money">{{ money(s.row.totalAmount) }}</span></template></el-table-column>
      <el-table-column label="已缴"><template #default="s">{{ money(s.row.paidAmount) }}</template></el-table-column>
      <el-table-column label="合同状态"><template #default="s"><el-tag :type="s.row.status==='SIGNED'?'success':s.row.status==='CANCELLED'?'info':'warning'">{{ CONTRACT_STATUS[s.row.status] }}</el-tag></template></el-table-column>
      <el-table-column label="缴费状态"><template #default="s"><el-tag :type="s.row.payStatus==='PAID'?'success':s.row.payStatus==='PARTIAL'?'warning':'danger'">{{ PAY_STATUS[s.row.payStatus] }}</el-tag></template></el-table-column>
      <el-table-column prop="manageExpireDate" label="管理费到期" />
      <el-table-column label="操作" width="240"><template #default="s">
        <el-button size="small" type="primary" v-if="s.row.status==='PENDING_SIGN'" @click="sign(s.row)">签约</el-button>
        <el-button size="small" type="success" v-if="s.row.status==='SIGNED' && s.row.payStatus!=='PAID'" @click="openPay(s.row)">缴费</el-button>
        <el-button size="small" v-if="auth.isStaffSide && s.row.status!=='CANCELLED'" @click="cancel(s.row)">取消</el-button>
      </template></el-table-column>
    </el-table>
    <el-dialog v-model="payDlg" title="合同缴费" width="420px">
      <div v-if="payTarget" style="margin-bottom:12px">合同 {{ payTarget.contractNo }} · 应付 <b class="money">{{ money(payTarget.totalAmount) }}</b> · 已缴 {{ money(payTarget.paidAmount) }} · 待缴 <b class="money">{{ money(remain) }}</b></div>
      <el-form label-width="76px">
        <el-form-item label="本次金额"><el-input-number v-model="payForm.amount" :min="1" :max="remain" style="width:100%" /></el-form-item>
        <el-form-item label="方式"><el-select v-model="payForm.method"><el-option value="ONLINE" label="在线支付" /><el-option value="CASH" label="现金" /><el-option value="POS" label="刷卡" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="payDlg=false">取消</el-button><el-button type="primary" @click="doPay">确认缴费</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { money, CONTRACT_STATUS, PAY_STATUS } from '../utils/format'
const auth = useAuthStore()
const rows = ref([]); const status = ref(''); const kw = ref('')
const payDlg = ref(false); const payTarget = ref(null); const payForm = reactive({ amount: 0, method: 'ONLINE' })
const remain = computed(() => payTarget.value ? Number(payTarget.value.totalAmount) - Number(payTarget.value.paidAmount) : 0)
async function load(){ const r = await request.get('/contracts', { params: { status: status.value, kw: kw.value } }); rows.value = r.data }
async function sign(c){ await ElMessageBox.confirm('确认签约合同 ' + c.contractNo + '？签约后墓位状态变为已售。', '签约确认'); try { await request.post(`/contracts/${c.id}/sign`); ElMessage.success('签约完成'); load() } catch (e) {} }
function openPay(c){ payTarget.value = c; payForm.amount = remain.value; payDlg.value = true }
async function doPay(){ try { await request.post(`/contracts/${payTarget.value.id}/pay`, payForm); ElMessage.success('缴费成功'); payDlg.value = false; load() } catch (e) {} }
async function cancel(c){ await ElMessageBox.confirm('确认取消合同 ' + c.contractNo + '？未缴费合同取消后墓位将释放。', '提示'); try { await request.post(`/contracts/${c.id}/cancel`); ElMessage.success('已取消'); load() } catch (e) {} }
onMounted(load)
</script>
