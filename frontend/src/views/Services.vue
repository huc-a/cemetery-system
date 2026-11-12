<template>
  <div class="page-card">
    <div class="page-title">🧺 服务项目管理</div><div class="page-sub">立碑、代祭扫、绿化养护、礼仪、寄存等增值服务定价与上下架。</div>
    <div class="toolbar"><el-select v-model="cat" placeholder="全部分类" clearable style="width:140px" @change="load">
      <el-option v-for="c in cats" :key="c" :value="c" :label="c" /></el-select>
      <span class="spacer"></span><el-button type="primary" @click="open()">＋ 新增服务</el-button></div>
    <el-table :data="rows" stripe>
      <el-table-column label="服务" min-width="200"><template #default="s">{{ s.row.coverEmoji }} <b>{{ s.row.name }}</b></template></el-table-column>
      <el-table-column prop="category" label="分类" /><el-table-column prop="description" label="说明" min-width="260" show-overflow-tooltip />
      <el-table-column label="价格"><template #default="s"><span class="money">{{ money(s.row.price) }}</span> / {{ s.row.unit }}</template></el-table-column>
      <el-table-column prop="sales" label="销量" />
      <el-table-column label="状态"><template #default="s"><el-tag :type="s.row.status===1?'success':'info'">{{ s.row.status===1?'上架':'下架' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150"><template #default="s"><el-button size="small" @click="open(s.row)">编辑</el-button>
        <el-button size="small" type="danger" plain v-if="auth.isAdmin" @click="del(s.row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" :title="form.id?'编辑服务':'新增服务'" width="500px">
      <el-form label-width="76px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.category" allow-create filterable style="width:100%"><el-option v-for="c in cats" :key="c" :value="c" :label="c" /></el-select></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" placeholder="次 / 座 / 年" style="width:130px" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.coverEmoji" style="width:130px" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { useAuthStore } from '../store/auth'
import { money } from '../utils/format'
const auth = useAuthStore()
const cats = ['碑刻服务', '代祭扫', '绿化养护', '养护维修', '礼仪服务', '祭扫用品', '寄存服务', '咨询服务']
const rows = ref([]); const cat = ref(''); const dlg = ref(false); const form = reactive({})
async function load(){ const r = await request.get('/services', { params: { category: cat.value } }); rows.value = r.data }
function open(s){ Object.assign(form, s ? { ...s } : { name: '', category: '代祭扫', price: 198, unit: '次', coverEmoji: '💐', status: 1, description: '' }); dlg.value = true }
async function save(){ try { if (form.id) await request.put('/services/' + form.id, form); else await request.post('/services', form); ElMessage.success('已保存'); dlg.value = false; load() } catch (e) {} }
async function del(s){ await ElMessageBox.confirm('确认删除服务「' + s.name + '」？', '提示'); try { await request.delete('/services/' + s.id); ElMessage.success('已删除'); load() } catch (e) {} }
onMounted(load)
</script>
