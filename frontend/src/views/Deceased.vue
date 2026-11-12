<template>
  <div class="page-card">
    <div class="page-title">📖 逝者档案（一人一档）</div><div class="page-sub">{{ auth.isFamily ? '您亲人的档案；新建档案将自动生成在线纪念馆。' : '逝者生平档案，与墓位、合同、纪念馆一一关联。' }}</div>
    <div class="toolbar"><el-input v-model="kw" placeholder="按姓名搜索" clearable style="width:200px" @keyup.enter="load" @clear="load" /><el-button @click="load">查询</el-button>
      <span class="spacer"></span><el-button type="primary" @click="open()">＋ 新建档案</el-button></div>
    <el-table :data="rows" stripe>
      <el-table-column label="逝者" min-width="120"><template #default="s">{{ s.row.photoEmoji || '🖼️' }} <b>{{ s.row.name }}</b>（{{ s.row.gender }}）</template></el-table-column>
      <el-table-column label="生卒"><template #default="s">{{ s.row.birthDate }} ～ {{ s.row.deathDate }}（享年 {{ s.row.age }} 岁）</template></el-table-column>
      <el-table-column prop="plotNo" label="安葬墓位" /><el-table-column prop="familyName" label="家属" /><el-table-column prop="relation" label="关系" />
      <el-table-column prop="burialDate" label="安葬日期" /><el-table-column prop="biography" label="生平简介" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="200"><template #default="s">
        <el-button size="small" @click="goHall(s.row)">纪念馆</el-button>
        <el-button size="small" @click="open(s.row)">编辑</el-button>
        <el-button size="small" type="danger" plain v-if="auth.isAdmin" @click="del(s.row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" :title="form.id ? '编辑档案' : '新建档案'" width="540px">
      <el-form label-width="86px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="性别"><el-radio-group v-model="form.gender"><el-radio value="男">男</el-radio><el-radio value="女">女</el-radio></el-radio-group></el-form-item>
        <el-form-item label="出生日期"><el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="逝世日期"><el-date-picker v-model="form.deathDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="安葬墓位"><el-select v-model="form.plotId" filterable clearable placeholder="选择已售/已安葬墓位" style="width:100%">
          <el-option v-for="p in myPlots" :key="p.id" :value="p.id" :label="p.plotNo + '（' + p.areaName + '）'" /></el-select></el-form-item>
        <el-form-item label="与家属关系"><el-input v-model="form.relation" placeholder="如 父亲 / 母亲 / 祖父" /></el-form-item>
        <el-form-item label="生平简介"><el-input v-model="form.biography" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
const auth = useAuthStore(); const router = useRouter()
const rows = ref([]); const kw = ref(''); const dlg = ref(false); const form = reactive({}); const myPlots = ref([])
async function load(){ const r = await request.get('/deceased', { params: { kw: kw.value } }); rows.value = r.data }
function open(d){ Object.assign(form, d ? { ...d } : { name: '', gender: '男', birthDate: '', deathDate: '', plotId: null, relation: '', biography: '', photoEmoji: '🖼️' }); dlg.value = true }
async function save(){ try { if (form.id) await request.put('/deceased/' + form.id, form); else await request.post('/deceased', form); ElMessage.success('已保存'); dlg.value = false; load() } catch (e) {} }
async function del(d){ await ElMessageBox.confirm('确认删除档案「' + d.name + '」？', '提示'); try { await request.delete('/deceased/' + d.id); ElMessage.success('已删除'); load() } catch (e) {} }
async function goHall(d){ const r = await request.get('/memorials'); const h = r.data.find(x => x.deceasedId === d.id); if (h) router.push('/memorials/' + h.id); else ElMessage.info('该逝者暂无纪念馆') }
onMounted(async () => { load(); const r = await request.get('/plots'); myPlots.value = r.data.filter(p => ['SOLD', 'BURIED'].includes(p.status)) })
</script>
