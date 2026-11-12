<template>
  <div v-if="hall" class="wrap2">
    <div class="hero">
      <div class="big">{{ hall.photoEmoji || '🖼️' }}</div>
      <h2>{{ hall.deceasedName }} 纪念馆</h2>
      <div class="epitaph">「{{ hall.epitaph }}」</div>
      <div class="meta">{{ hall.plotNo }} · 家属 {{ hall.familyName }} 敬立 · 已被瞻仰 {{ hall.visitCount }} 次</div>
    </div>
    <div class="page-card" style="margin-bottom:14px">
      <div class="ct">生平</div><p class="bio">{{ hall.biography || '暂无生平简介' }}</p>
      <div class="actions">
        <el-button size="large" @click="act('FLOWER')">💐 献花（{{ hall.flowerCount }}）</el-button>
        <el-button size="large" @click="act('CANDLE')">🕯️ 点烛（{{ hall.candleCount }}）</el-button>
        <el-button size="large" @click="act('INCENSE')">🪔 上香</el-button>
        <el-button size="large" @click="act('BOW')">🙏 鞠躬</el-button>
      </div>
      <el-input v-model="msg" type="textarea" :rows="2" placeholder="写下您的思念留言……" style="margin-top:12px" />
      <el-button type="primary" style="margin-top:10px" @click="act('MESSAGE')">发表留言（{{ hall.messageCount }}）</el-button>
      <el-button v-if="canEdit" style="margin-top:10px;margin-left:8px" @click="editDlg=true">编辑纪念馆</el-button>
    </div>
    <div class="page-card">
      <div class="ct">祭扫足迹</div>
      <el-timeline>
        <el-timeline-item v-for="a in acts" :key="a.id" :timestamp="a.createTime">
          <b>{{ a.userName }}</b> {{ label(a) }} <span v-if="a.content" class="content">：{{ a.content }}</span>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!acts.length" description="暂无祭扫记录，来献上第一束花吧" />
    </div>
    <el-dialog v-model="editDlg" title="编辑纪念馆" width="480px">
      <el-form label-width="70px">
        <el-form-item label="墓志铭"><el-input v-model="editForm.epitaph" /></el-form-item>
        <el-form-item label="生平"><el-input v-model="editForm.biography" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="editDlg=false">取消</el-button><el-button type="primary" @click="saveEdit">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
const route = useRoute(); const auth = useAuthStore()
const hall = ref(null); const acts = ref([]); const msg = ref('')
const editDlg = ref(false); const editForm = reactive({ epitaph: '', biography: '' })
const canEdit = computed(() => hall.value && (auth.isStaffSide || (auth.user && auth.user.id === hall.value.familyId)))
const NAMES = { FLOWER: '献上鲜花 💐', CANDLE: '点亮烛光 🕯️', INCENSE: '上香一炷 🪔', BOW: '鞠躬致哀 🙏', MESSAGE: '留言' }
const label = a => NAMES[a.actionType] || a.actionType
async function load(){ const r = await request.get('/memorials/' + route.params.id); hall.value = r.data.hall; acts.value = r.data.actions
  editForm.epitaph = hall.value.epitaph; editForm.biography = hall.value.biography }
async function act(t){ if (!auth.user) return ElMessage.warning('请先登录后进行云祭扫')
  try { const r = await request.post(`/memorials/${route.params.id}/action`, { actionType: t, content: t === 'MESSAGE' ? msg.value : null })
    hall.value = r.data; if (t === 'MESSAGE') msg.value = ''; ElMessage.success('已记录您的哀思'); const a = await request.get(`/memorials/${route.params.id}/actions`); acts.value = a.data } catch (e) {} }
async function saveEdit(){ try { const r = await request.put('/memorials/' + route.params.id, editForm); hall.value = r.data; ElMessage.success('已保存'); editDlg.value = false } catch (e) {} }
onMounted(load)
</script>
<style scoped>
.wrap2{ max-width:860px; margin:0 auto }
.hero{ text-align:center; padding:30px 16px 22px; background:linear-gradient(180deg,#173f35,#1f5747); border-radius:16px; color:#f3ead3; margin-bottom:14px }
.big{ font-size:58px } .hero h2{ margin:8px 0 4px } .epitaph{ color:#c9a45c } .meta{ font-size:12px; color:#b9c4bd; margin-top:6px }
.ct{ font-weight:700; color:#173f35; margin-bottom:8px } .bio{ color:#555; line-height:1.8; margin:0 }
.actions{ display:flex; gap:10px; flex-wrap:wrap; margin-top:14px } .content{ color:#666 }
</style>
