<template>
  <div>
    <div class="head"><h2>🧺 服务中心</h2><p>代祭扫、立碑刻字、绿化养护、礼仪服务——明码标价，在线下单，服务完成回传照片说明。</p></div>
    <div class="toolbar" style="margin:14px 0">
      <el-radio-group v-model="cat" @change="flt"><el-radio-button value="">全部</el-radio-button>
      <el-radio-button v-for="c in cats" :key="c" :value="c">{{ c }}</el-radio-button></el-radio-group>
    </div>
    <el-row :gutter="14">
      <el-col :span="8" v-for="s in rows" :key="s.id" style="margin-bottom:14px">
        <div class="svc">
          <div class="aemoji">{{ s.coverEmoji }}</div>
          <div class="aname">{{ s.name }} <el-tag size="small" type="success">{{ s.category }}</el-tag></div>
          <p>{{ s.description }}</p>
          <div class="row2"><span class="money big">{{ money(s.price) }} / {{ s.unit }}</span><span class="sold">已售 {{ s.sales }}</span></div>
          <el-button type="primary" style="width:100%;margin-top:10px" @click="order(s)">立即下单</el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { useAuthStore } from '../../store/auth'
import { money } from '../../utils/format'
const all = ref([]); const rows = ref([]); const cat = ref(''); const cats = ref([])
const router = useRouter(); const auth = useAuthStore()
function flt(){ rows.value = cat.value ? all.value.filter(s => s.category === cat.value) : all.value }
async function order(s){ if (!auth.user) return router.push({ path: '/login', query: { redirect: '/front-services' } })
  try { const r = await request.post('/service-orders', { productId: s.id, quantity: 1 }); ElMessage.success('下单成功：' + r.data.orderNo + '，请到服务订单中支付'); router.push('/service-orders') } catch (e) {} }
onMounted(async () => { const r = await request.get('/public/services'); all.value = r.data; cats.value = [...new Set(r.data.map(s => s.category))]; flt() })
</script>
<style scoped>
.head{ text-align:center; padding-top:10px } .head h2{ color:#173f35; margin:0 0 6px } .head p{ color:#8a8f8c }
.svc{ background:#fff; border:1px solid #e8e2d2; border-radius:14px; padding:18px; height:100% }
.aemoji{ font-size:34px } .aname{ font-weight:700; color:#173f35; margin:6px 0 } .svc p{ color:#777; font-size:13px; line-height:1.7; min-height:44px }
.row2{ display:flex; justify-content:space-between; align-items:center } .big{ font-size:16px } .sold{ color:#9aa39d; font-size:12px }
</style>
