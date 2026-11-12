<template>
  <div>
    <div class="hero">
      <div class="hero-in">
        <h1>青松陵园 · 生命纪念园</h1>
        <p>松柏长青之地，安放思念之所。在线选墓、祭扫预约、云祭扫、代祭扫与管理费续缴，一站式办理。</p>
        <div class="hero-btns">
          <el-button size="large" type="primary" @click="$router.push('/plot-map')">🗺️ 可视化选墓</el-button>
          <el-button size="large" @click="$router.push('/memorial-wall')">🕯️ 云祭扫纪念馆</el-button>
          <el-button size="large" @click="goBook">🌼 预约祭扫</el-button>
        </div>
        <div class="hero-stats">
          <span>墓位总数 <b>{{ ov.totalPlots ?? '-' }}</b></span><span>在售空闲 <b>{{ ov.freePlots ?? '-' }}</b></span>
          <span>已安葬 <b>{{ ov.buriedCount ?? '-' }}</b></span><span>在线纪念馆 <b>{{ ov.memorialCount ?? '-' }}</b></span>
        </div>
      </div>
    </div>
    <h3 class="sec">🏞️ 园区墓区</h3>
    <el-row :gutter="14">
      <el-col :span="8" v-for="(a,i) in (ov.areas||[])" :key="a.id" style="margin-bottom:14px">
        <div class="area-card" @click="$router.push('/plot-map')">
          <div class="aemoji">{{ a.coverEmoji }}</div>
          <div class="aname">{{ a.name }} <el-tag size="small">{{ a.type }}</el-tag></div>
          <p>{{ a.description }}</p>
          <div class="aprice">起价 <span class="money">{{ money(a.priceFrom) }}</span> · 点击查看平面图选墓 →</div>
        </div>
      </el-col>
    </el-row>
    <h3 class="sec">🧺 热门服务</h3>
    <el-row :gutter="14">
      <el-col :span="8" v-for="s in (ov.hotServices||[])" :key="s.id" style="margin-bottom:14px">
        <div class="svc" @click="$router.push('/front-services')">
          <div class="aemoji">{{ s.coverEmoji }}</div>
          <div class="aname">{{ s.name }}</div><p>{{ s.description }}</p>
          <div class="aprice"><span class="money">{{ money(s.price) }}</span> / {{ s.unit }} · 已售 {{ s.sales }}</div>
        </div>
      </el-col>
    </el-row>
    <h3 class="sec">📢 园区公告</h3>
    <div class="page-card">
      <div v-for="n in (ov.notices||[])" :key="n.id" class="nrow"><el-tag v-if="n.pinned" type="danger" size="small">置顶</el-tag><b>{{ n.title }}</b><span>{{ n.createTime }}</span></div>
      <el-empty v-if="!(ov.notices||[]).length" description="暂无公告" />
    </div>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useAuthStore } from '../../store/auth'
import { money } from '../../utils/format'
const ov = ref({}); const router = useRouter(); const auth = useAuthStore()
function goBook(){ if (!auth.user) return router.push({ path: '/login', query: { redirect: '/visits' } }); router.push('/visits') }
onMounted(async () => { const r = await request.get('/public/overview'); ov.value = r.data })
</script>
<style scoped>
.hero{ border-radius:18px; padding:46px 34px; color:#f7f2e2; background:linear-gradient(120deg,rgba(18,49,40,.92),rgba(31,87,71,.82)), url('/login-wallpaper.jpg') center/cover; box-shadow:0 14px 34px rgba(23,63,53,.25) }
.hero h1{ margin:0 0 10px; font-size:32px } .hero p{ color:#d8dfd5; max-width:640px; line-height:1.8 }
.hero-btns{ display:flex; gap:10px; margin:18px 0; flex-wrap:wrap } .hero-stats{ display:flex; gap:26px; font-size:13px; color:#c9d4cc; flex-wrap:wrap } .hero-stats b{ color:#e9c979; font-size:16px }
.sec{ color:#173f35; margin:24px 0 12px }
.area-card,.svc{ background:#fff; border:1px solid #e8e2d2; border-radius:14px; padding:18px; cursor:pointer; height:100%; transition:.15s }
.area-card:hover,.svc:hover{ transform:translateY(-3px); box-shadow:0 10px 24px rgba(23,63,53,.13); border-color:#c9a45c }
.aemoji{ font-size:34px } .aname{ font-weight:700; color:#173f35; margin:6px 0 } .area-card p,.svc p{ color:#777; font-size:13px; line-height:1.7; min-height:44px }
.aprice{ font-size:13px; color:#8a7a52 } .nrow{ display:flex; gap:10px; align-items:center; padding:9px 2px; border-bottom:1px dashed #eee7d5 } .nrow span{ color:#9aa39d; font-size:12px }
</style>
