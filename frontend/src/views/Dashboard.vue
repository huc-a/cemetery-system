<template>
  <div>
    <el-row :gutter="14" class="cards">
      <el-col :span="6" v-for="c in cards" :key="c.label"><div class="stat" :style="{borderTopColor:c.color}">
        <div class="num" :style="{color:c.color}">{{ c.value }}</div><div class="lab">{{ c.label }}</div><div class="sub2">{{ c.sub }}</div>
      </div></el-col>
    </el-row>
    <el-row :gutter="14">
      <el-col :span="10"><div class="page-card"><div class="ct">墓位状态分布</div><div ref="pieEl" class="chart"></div></div></el-col>
      <el-col :span="14"><div class="page-card"><div class="ct">近6个月收入（元）</div><div ref="barEl" class="chart"></div></div></el-col>
    </el-row>
    <el-row :gutter="14" style="margin-top:14px">
      <el-col :span="10"><div class="page-card"><div class="ct">收入构成</div><div ref="compEl" class="chart"></div></div></el-col>
      <el-col :span="14"><div class="page-card"><div class="ct">近7天祭扫预约趋势（人次）</div><div ref="lineEl" class="chart"></div></div></el-col>
    </el-row>
    <el-row :gutter="14" style="margin-top:14px">
      <el-col :span="12"><div class="page-card"><div class="ct">各墓区售出情况</div>
        <el-table :data="d.areaStats || []" size="small"><el-table-column prop="area" label="墓区" /><el-table-column prop="total" label="墓位总数" /><el-table-column prop="sold" label="已售+已安葬" />
        <el-table-column label="售出率"><template #default="s">{{ s.row.total ? Math.round(s.row.sold / s.row.total * 100) : 0 }}%</template></el-table-column></el-table></div></el-col>
      <el-col :span="12"><div class="page-card"><div class="ct">最新合同</div>
        <el-table :data="d.recentContracts || []" size="small"><el-table-column prop="contractNo" label="合同号" /><el-table-column prop="plotNo" label="墓位" /><el-table-column prop="buyerName" label="购买人" />
        <el-table-column label="金额"><template #default="s"><span class="money">{{ money(s.row.totalAmount) }}</span></template></el-table-column></el-table></div></el-col>
    </el-row>
  </div>
</template>
<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'
import { money } from '../utils/format'
const d = ref({}); const pieEl = ref(); const barEl = ref(); const compEl = ref(); const lineEl = ref()
const cards = computed(() => [
  { label: '墓位总数', value: d.value.totalPlots ?? '-', sub: `空闲 ${d.value.freePlots ?? 0} · 已安葬 ${d.value.buriedPlots ?? 0}`, color: '#1f5747' },
  { label: '累计收入', value: money(d.value.totalRevenue), sub: `缴费流水 ${d.value.paymentCount ?? 0} 笔`, color: '#b07d2b' },
  { label: '逝者档案 / 纪念馆', value: (d.value.totalDeceased ?? '-') + ' / ' + (d.value.memorialCount ?? '-'), sub: `累计献花 ${d.value.totalFlowers ?? 0} 束`, color: '#457b9d' },
  { label: '待处理工单', value: d.value.pendingWorkOrders ?? '-', sub: `今日祭扫 ${d.value.todayVisits ?? 0} 人次 · 家属 ${d.value.totalFamilies ?? 0} 户`, color: '#a44a3f' },
])
onMounted(async () => {
  const r = await request.get('/stats/dashboard'); d.value = r.data
  await nextTick()
  echarts.init(pieEl.value).setOption({ tooltip: {}, legend: { bottom: 0 }, series: [{ type: 'pie', radius: ['42%', '68%'], label: { formatter: '{b} {c}' }, data: d.value.plotStatusDist || [] }] })
  echarts.init(barEl.value).setOption({ tooltip: {}, xAxis: { type: 'category', data: (d.value.monthlyRevenue || []).map(x => x.month) }, yAxis: { type: 'value' }, series: [{ type: 'bar', itemStyle: { color: '#1f5747', borderRadius: [6, 6, 0, 0] }, data: (d.value.monthlyRevenue || []).map(x => x.amount) }] })
  echarts.init(compEl.value).setOption({ tooltip: {}, legend: { bottom: 0 }, series: [{ type: 'pie', radius: '62%', label: { formatter: '{b}' }, data: d.value.revenueComposition || [] }] })
  echarts.init(lineEl.value).setOption({ tooltip: {}, xAxis: { type: 'category', data: (d.value.visitTrend || []).map(x => x.date.slice(5)) }, yAxis: { type: 'value' }, series: [{ type: 'line', smooth: true, areaStyle: { opacity: .18 }, itemStyle: { color: '#c9a45c' }, data: (d.value.visitTrend || []).map(x => x.visitors) }] })
})
</script>
<style scoped>
.cards{ margin-bottom:14px } .stat{ background:#fff; border-radius:12px; padding:16px; border-top:4px solid #1f5747; box-shadow:0 2px 12px rgba(23,63,53,.06) }
.num{ font-size:24px; font-weight:800 } .lab{ color:#555; margin-top:2px } .sub2{ color:#9aa39d; font-size:12px; margin-top:2px }
.ct{ font-weight:700; color:#173f35; margin-bottom:8px } .chart{ height:280px }
</style>
