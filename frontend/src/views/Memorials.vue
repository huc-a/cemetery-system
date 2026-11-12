<template>
  <div>
    <div class="page-title" style="margin-bottom:4px">🏮 在线纪念馆</div>
    <div class="page-sub">为每位逝者设立的线上纪念空间：献花、点烛、上香、留言，寄托哀思。</div>
    <div class="toolbar"><el-input v-model="kw" placeholder="按逝者姓名搜索" clearable style="width:220px" @keyup.enter="load" @clear="load" /><el-button @click="load">查询</el-button></div>
    <el-row :gutter="14">
      <el-col :span="6" v-for="h in rows" :key="h.id" style="margin-bottom:14px">
        <div class="hall" @click="$router.push('/memorials/' + h.id)">
          <div class="emoji">{{ h.photoEmoji || '🖼️' }}</div>
          <div class="name">{{ h.deceasedName }} 纪念馆</div>
          <div class="epitaph">「{{ h.epitaph }}」</div>
          <div class="stats">💐 {{ h.flowerCount }} · 🕯️ {{ h.candleCount }} · 💬 {{ h.messageCount }} · 👁 {{ h.visitCount }}</div>
          <div class="plot">{{ h.plotNo }} · {{ h.familyName }} 敬立</div>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!rows.length" description="暂无纪念馆" />
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import request from '../utils/request'
const rows = ref([]); const kw = ref('')
async function load(){ const r = await request.get('/memorials', { params: { kw: kw.value } }); rows.value = r.data }
onMounted(load)
</script>
<style scoped>
.hall{ background:linear-gradient(180deg,#fff,#f7f4ec); border:1px solid #e8e2d2; border-radius:14px; padding:20px 14px; text-align:center; cursor:pointer; transition:.15s }
.hall:hover{ transform:translateY(-3px); box-shadow:0 10px 24px rgba(23,63,53,.14); border-color:#c9a45c }
.emoji{ font-size:42px } .name{ font-weight:700; color:#173f35; margin-top:6px } .epitaph{ color:#8a7a52; font-size:12px; margin:4px 0 }
.stats{ font-size:12px; color:#666 } .plot{ font-size:12px; color:#9aa39d; margin-top:4px }
</style>
