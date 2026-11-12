<template>
  <div class="page-card">
    <div class="page-title">🏞️ 墓区管理</div><div class="page-sub">维护园区分区信息；售出统计与剩余墓位实时汇总。</div>
    <div class="toolbar"><span class="spacer"></span><el-button type="primary" @click="open()">＋ 新增墓区</el-button></div>
    <el-table :data="rows" stripe>
      <el-table-column label="墓区" min-width="170"><template #default="s">{{ s.row.area.coverEmoji }} <b>{{ s.row.area.name }}</b>（{{ s.row.area.code }}区）</template></el-table-column>
      <el-table-column prop="area.type" label="类型" /><el-table-column label="起价"><template #default="s"><span class="money">{{ money(s.row.area.priceFrom) }}</span></template></el-table-column>
      <el-table-column prop="total" label="墓位总数" /><el-table-column prop="free" label="空闲" /><el-table-column prop="sold" label="已售/安葬" />
      <el-table-column prop="area.description" label="简介" min-width="240" show-overflow-tooltip />
      <el-table-column label="操作" width="150"><template #default="s">
        <el-button size="small" @click="open(s.row.area)">编辑</el-button>
        <el-button size="small" type="danger" plain @click="del(s.row.area)">删除</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" :title="form.id ? '编辑墓区' : '新增墓区'" width="480px">
      <el-form label-width="84px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="代码"><el-input v-model="form.code" placeholder="如 F" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="form.type" placeholder="如 传统墓区" /></el-form-item>
        <el-form-item label="起价"><el-input-number v-model="form.priceFrom" :min="0" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.coverEmoji" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { money } from '../utils/format'
const rows = ref([]); const dlg = ref(false); const form = reactive({})
async function load(){ const r = await request.get('/areas'); rows.value = r.data }
function open(a){ Object.assign(form, a ? { ...a } : { name: '', code: '', type: '', priceFrom: 10000, coverEmoji: '🌲', sortOrder: 9, description: '' }); dlg.value = true }
async function save(){ try { if (form.id) await request.put('/areas/' + form.id, form); else await request.post('/areas', form); ElMessage.success('已保存'); dlg.value = false; load() } catch (e) {} }
async function del(a){ await ElMessageBox.confirm('确认删除墓区「' + a.name + '」？', '提示'); try { await request.delete('/areas/' + a.id); ElMessage.success('已删除'); load() } catch (e) {} }
onMounted(load)
</script>
