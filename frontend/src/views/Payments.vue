<template>
  <div class="page-card">
    <div class="page-title">💳 收费 / 财务记录</div><div class="page-sub">全部缴费流水（购墓合同、管理费、增值服务），与统计仪表盘收入口径一致，可对账。</div>
    <el-row :gutter="14" style="margin-bottom:14px">
      <el-col :span="6"><div class="sum">累计收入 <b class="money">{{ money(summary.total) }}</b></div></el-col>
      <el-col :span="6"><div class="sum">流水笔数 <b>{{ summary.count || 0 }}</b></div></el-col>
      <el-col :span="12"><div class="sum">构成：<span v-for="(v,k) in (summary.byType||{})" :key="k" style="margin-right:14px">{{ BIZ_TYPE[k] || k }} <b class="money">{{ money(v) }}</b></span></div></el-col>
    </el-row>
    <div class="toolbar"><el-select v-model="bizType" placeholder="全部类型" clearable style="width:150px" @change="load">
      <el-option v-for="(v,k) in BIZ_TYPE" :key="k" :value="k" :label="v" /></el-select><el-button @click="load">刷新</el-button></div>
    <el-table :data="rows" stripe>
      <el-table-column prop="payNo" label="流水号" min-width="190" />
      <el-table-column label="业务类型"><template #default="s"><el-tag>{{ BIZ_TYPE[s.row.bizType] || s.row.bizType }}</el-tag></template></el-table-column>
      <el-table-column prop="bizNo" label="业务单号" min-width="140" /><el-table-column prop="payerName" label="付款人" />
      <el-table-column label="金额"><template #default="s"><span class="money">{{ money(s.row.amount) }}</span></template></el-table-column>
      <el-table-column prop="method" label="方式" /><el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" min-width="160" />
    </el-table>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import request from '../utils/request'
import { money, BIZ_TYPE } from '../utils/format'
const rows = ref([]); const summary = ref({}); const bizType = ref('')
async function load(){ const r = await request.get('/payments', { params: { bizType: bizType.value } }); rows.value = r.data
  const s = await request.get('/payments/summary'); summary.value = s.data }
onMounted(load)
</script>
<style scoped>.sum{ background:#f7f4ec; border-radius:10px; padding:12px 14px; font-size:14px }</style>
