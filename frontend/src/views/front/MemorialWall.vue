<template>
  <div>
    <div class="head">
      <h2>🕯️ 云祭扫 · 在线纪念馆</h2>
      <p>天涯海角，思念无碍。为亲人建立的线上纪念馆，随时献花、点烛、留言寄托哀思。</p>
      <el-input v-model="kw" placeholder="搜索亲人姓名" clearable style="width:280px" @keyup.enter="load" @clear="load" />
    </div>
    <el-row :gutter="14" style="margin-top:16px">
      <el-col :span="6" v-for="h in rows" :key="h.id" style="margin-bottom:14px">
        <div class="hall" @click="open(h)">
          <div class="emoji">{{ h.photoEmoji || '🖼️' }}</div>
          <div class="name">{{ h.deceasedName }} 纪念馆</div>
          <div class="epitaph">「{{ h.epitaph }}」</div>
          <div class="stats">💐 {{ h.flowerCount }} · 🕯️ {{ h.candleCount }} · 💬 {{ h.messageCount }}</div>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!rows.length" description="暂无纪念馆" />
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useAuthStore } from '../../store/auth'
const rows = ref([]); const kw = ref(''); const router = useRouter(); const auth = useAuthStore()
async function load(){ const r = await request.get('/public/memorials', { params: { kw: kw.value } }); rows.value = r.data }
function open(h){ if (!auth.user) return router.push({ path: '/login', query: { redirect: '/memorials/' + h.id } }); router.push('/memorials/' + h.id) }
onMounted(load)
</script>
<style scoped>
.head{ text-align:center; padding:14px 0 4px } .head h2{ color:#173f35; margin:0 0 6px } .head p{ color:#8a8f8c }
.hall{ background:linear-gradient(180deg,#fff,#f7f4ec); border:1px solid #e8e2d2; border-radius:14px; padding:20px 14px; text-align:center; cursor:pointer; transition:.15s }
.hall:hover{ transform:translateY(-3px); box-shadow:0 10px 24px rgba(23,63,53,.14); border-color:#c9a45c }
.emoji{ font-size:42px } .name{ font-weight:700; color:#173f35; margin-top:6px } .epitaph{ color:#8a7a52; font-size:12px; margin:4px 0 } .stats{ font-size:12px; color:#666 }
</style>
