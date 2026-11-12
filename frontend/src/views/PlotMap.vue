<template>
  <div class="page-card">
    <div class="page-title">🗺️ 可视化选墓</div>
    <div class="page-sub">按墓区查看平面网格图：颜色代表墓位状态，点击墓位查看详情{{ auth.isFamily || !auth.user ? '，空闲墓位可直接预订 / 购买' : '' }}。</div>
    <div class="toolbar">
      <el-radio-group v-model="areaId" @change="load">
        <el-radio-button v-for="a in areas" :key="a.id" :value="a.id">{{ a.coverEmoji }} {{ a.name }}</el-radio-button>
      </el-radio-group>
      <span class="spacer"></span>
      <span v-for="(label,s) in PLOT_STATUS" :key="s" class="legend"><i :style="{background:PLOT_COLOR[s]}"></i>{{ label }}</span>
    </div>
    <div v-if="curArea" class="area-info">{{ curArea.coverEmoji }} <b>{{ curArea.name }}</b> · {{ curArea.type }} · {{ curArea.description }} · 起价 <span class="money">{{ money(curArea.priceFrom) }}</span></div>
    <div class="grid-wrap">
      <div v-for="row in rows" :key="row" class="grow">
        <div class="rlabel">{{ row }}排</div>
        <div v-for="p in plotsInRow(row)" :key="p.id" class="cell" :style="{background:PLOT_COLOR[p.status]}" @click="open(p)">
          <div class="cno">{{ p.colNo }}号</div><div class="ctype">{{ shortType(p.plotType) }}</div>
        </div>
      </div>
    </div>
    <el-dialog v-model="dlg" title="墓位详情" width="520px">
      <template v-if="cur">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="墓位编号">{{ cur.plotNo }}</el-descriptions-item>
          <el-descriptions-item label="所属墓区">{{ cur.areaName }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :color="PLOT_COLOR[cur.status]" style="color:#fff;border:none">{{ PLOT_STATUS[cur.status] }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="类型">{{ cur.plotType }}</el-descriptions-item>
          <el-descriptions-item label="价格"><span class="money">{{ money(cur.price) }}</span></el-descriptions-item>
          <el-descriptions-item label="管理费">{{ money(cur.manageFeeYearly) }} / 年</el-descriptions-item>
          <el-descriptions-item label="占地面积">{{ cur.areaSize }} ㎡</el-descriptions-item>
          <el-descriptions-item label="朝向">{{ cur.orientation }}</el-descriptions-item>
          <el-descriptions-item v-if="cur.buyerName && auth.isStaffSide" label="购买人">{{ cur.buyerName }}</el-descriptions-item>
        </el-descriptions>
        <el-form v-if="canBuy" label-width="86px" style="margin-top:16px">
          <el-form-item label="逝者姓名"><el-input v-model="buyForm.deceasedName" placeholder="拟安葬逝者姓名（可后补档案）" /></el-form-item>
          <el-form-item label="管理年限"><el-select v-model="buyForm.manageYears"><el-option :value="10" label="10年" /><el-option :value="20" label="20年" /><el-option :value="30" label="30年" /></el-select>
            <span class="hint">总价 = 墓位价 + 2% 服务费 + 管理费×年限 = <b class="money">{{ money(previewTotal) }}</b></span></el-form-item>
          <el-form-item v-if="auth.isStaffSide" label="购买家属"><el-select v-model="buyForm.buyerId" placeholder="选择家属账号" filterable style="width:100%">
            <el-option v-for="f in families" :key="f.id" :value="f.id" :label="f.realName + '（' + f.username + '）'" /></el-select></el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="dlg=false">关闭</el-button>
        <el-button v-if="cur && cur.status==='FREE' && auth.user" @click="reserve">预订此墓位</el-button>
        <el-button v-if="canBuy" type="primary" @click="buy">选此墓位 · 生成合同</el-button>
        <el-button v-if="!auth.user" type="primary" @click="$router.push({path:'/login',query:{redirect:'/plot-map'}})">登录后预订 / 购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { money, PLOT_STATUS, PLOT_COLOR } from '../utils/format'
const auth = useAuthStore(); const router = useRouter()
const areas = ref([]); const areaId = ref(null); const plots = ref([]); const families = ref([])
const dlg = ref(false); const cur = ref(null)
const buyForm = reactive({ deceasedName: '', manageYears: 20, buyerId: null })
const curArea = computed(() => areas.value.find(a => a.id === areaId.value))
const rows = computed(() => [...new Set(plots.value.map(p => p.rowNo))].sort((a, b) => a - b))
const plotsInRow = r => plots.value.filter(p => p.rowNo === r).sort((a, b) => a.colNo - b.colNo)
const shortType = t => (t || '').replace('立碑', '').replace('格位', '').slice(0, 4)
const canBuy = computed(() => cur.value && auth.user && (cur.value.status === 'FREE' || (cur.value.status === 'RESERVED' && auth.isStaffSide)))
const previewTotal = computed(() => { if (!cur.value) return 0; const p = Number(cur.value.price); return p + p * 0.02 + Number(cur.value.manageFeeYearly) * buyForm.manageYears })
async function loadAreas(){ const r = await request.get('/public/overview').catch(() => null)
  const list = r ? r.data.areas : (await request.get('/areas')).data.map(x => x.area)
  areas.value = list; if (!areaId.value && list.length) areaId.value = list[0].id; await load() }
async function load(){ if (!areaId.value) return
  const url = auth.user ? '/plots' : '/public/plots'
  const r = await request.get(url, { params: { areaId: areaId.value } }); plots.value = r.data }
function open(p){ cur.value = p; buyForm.deceasedName = ''; buyForm.buyerId = null; dlg.value = true }
async function reserve(){ try { await request.post(`/plots/${cur.value.id}/reserve`); ElMessage.success('预订成功，请尽快到园区或在线签约'); dlg.value = false; load() } catch (e) {} }
async function buy(){ try { const r = await request.post('/plots/buy', { plotId: cur.value.id, deceasedName: buyForm.deceasedName, manageYears: buyForm.manageYears, buyerId: buyForm.buyerId })
    ElMessage.success('合同已生成：' + r.data.contractNo + '，请前往购墓合同完成签约缴费'); dlg.value = false; router.push('/contracts') } catch (e) {} }
onMounted(async () => { await loadAreas(); if (auth.isStaffSide) { const r = await request.get('/users/families'); families.value = r.data } })
</script>
<style scoped>
.legend{ font-size:12px; color:#666; display:inline-flex; align-items:center; gap:4px } .legend i{ width:12px; height:12px; border-radius:3px; display:inline-block }
.area-info{ background:#f7f4ec; border-radius:8px; padding:10px 14px; margin-bottom:14px; font-size:13px; color:#5c5648 }
.grid-wrap{ overflow-x:auto; padding:6px 2px } .grow{ display:flex; align-items:center; gap:8px; margin-bottom:8px }
.rlabel{ width:36px; color:#8a8f8c; font-size:12px; text-align:right; flex:none }
.cell{ width:86px; flex:none; border-radius:9px; color:#fff; padding:8px 6px; cursor:pointer; text-align:center; transition:.15s; box-shadow:inset 0 -3px 0 rgba(0,0,0,.15) }
.cell:hover{ transform:translateY(-2px); box-shadow:0 6px 14px rgba(0,0,0,.22) }
.cno{ font-weight:700; font-size:13px } .ctype{ font-size:11px; opacity:.85 } .hint{ margin-left:10px; font-size:12px; color:#8a8f8c }
</style>
