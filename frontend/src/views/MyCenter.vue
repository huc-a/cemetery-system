<template>
  <div>
    <div class="page-title" style="margin-bottom:4px">🏠 家属中心</div>
    <div class="page-sub">{{ auth.user?.realName }} 您好，这里汇总您的合同、费用、预约与订单，可快速办理续费与祭扫。</div>
    <el-row :gutter="14" class="cards">
      <el-col :span="6"><div class="stat" @click="$router.push('/contracts')"><div class="num">{{ contracts.length }}</div><div>我的购墓合同</div></div></el-col>
      <el-col :span="6"><div class="stat warn" @click="$router.push('/fees')"><div class="num">{{ alertCount }}</div><div>管理费逾期/临期提醒</div></div></el-col>
      <el-col :span="6"><div class="stat" @click="$router.push('/visits')"><div class="num">{{ visits.length }}</div><div>我的祭扫预约</div></div></el-col>
      <el-col :span="6"><div class="stat" @click="$router.push('/service-orders')"><div class="num">{{ orders.length }}</div><div>我的服务订单</div></div></el-col>
    </el-row>
    <el-row :gutter="14">
      <el-col :span="12"><div class="page-card"><div class="ct">我的亲人纪念馆</div>
        <div v-for="h in halls" :key="h.id" class="mrow" @click="$router.push('/memorials/' + h.id)"><span>{{ h.photoEmoji || '🖼️' }} {{ h.deceasedName }} 纪念馆</span><span>💐 {{ h.flowerCount }} · 🕯️ {{ h.candleCount }} · 💬 {{ h.messageCount }}</span></div>
        <el-empty v-if="!halls.length" description="暂无纪念馆，可在逝者档案中新建" /></div></el-col>
      <el-col :span="12"><div class="page-card"><div class="ct">快捷办理</div>
        <div class="quick">
          <el-button @click="$router.push('/plot-map')">可视化选墓</el-button>
          <el-button @click="$router.push('/visits')">祭扫预约</el-button>
          <el-button @click="$router.push('/fees')">管理费续缴</el-button>
          <el-button @click="$router.push('/front-services')">订购代祭扫</el-button>
          <el-button @click="$router.push('/burials')">安葬预约</el-button>
          <el-button @click="$router.push('/deceased')">逝者档案</el-button>
          <el-button @click="$router.push('/payments')">我的缴费记录</el-button>
          <el-button @click="$router.push('/profile')">个人资料</el-button>
        </div></div></el-col>
    </el-row>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
const auth = useAuthStore()
const contracts = ref([]); const visits = ref([]); const orders = ref([]); const halls = ref([]); const alertCount = ref(0)
onMounted(async () => {
  contracts.value = (await request.get('/contracts')).data
  visits.value = (await request.get('/visits')).data.filter(v => v.status === 'BOOKED')
  orders.value = (await request.get('/service-orders')).data
  halls.value = (await request.get('/memorials')).data
  const a = await request.get('/fees/alerts'); alertCount.value = (a.data.overdueCount || 0) + (a.data.dueSoonCount || 0)
})
</script>
<style scoped>
.cards{ margin-bottom:14px } .stat{ background:#fff; border-radius:12px; padding:18px; text-align:center; cursor:pointer; border-top:4px solid #1f5747; box-shadow:0 2px 12px rgba(23,63,53,.06) }
.stat.warn{ border-top-color:#a44a3f } .num{ font-size:26px; font-weight:800; color:#173f35 }
.ct{ font-weight:700; color:#173f35; margin-bottom:10px } .mrow{ display:flex; justify-content:space-between; padding:9px 2px; border-bottom:1px dashed #eee7d5; cursor:pointer }
.quick{ display:flex; flex-wrap:wrap; gap:10px }
</style>
