<template>
  <div class="page-card">
    <div class="page-title">🪦 墓位管理</div><div class="page-sub">按墓区 / 状态筛选墓位；可单个新增或按排×列批量生成墓位网格。</div>
    <div class="toolbar">
      <el-select v-model="q.areaId" placeholder="全部墓区" clearable style="width:150px" @change="load"><el-option v-for="a in areas" :key="a.id" :value="a.id" :label="a.name" /></el-select>
      <el-select v-model="q.status" placeholder="全部状态" clearable style="width:130px" @change="load"><el-option v-for="(v,k) in PLOT_STATUS" :key="k" :value="k" :label="v" /></el-select>
      <el-input v-model="q.kw" placeholder="墓位编号搜索" clearable style="width:180px" @keyup.enter="load" @clear="load" />
      <el-button @click="load">查询</el-button><span class="spacer"></span>
      <el-button @click="batchDlg=true">批量生成</el-button><el-button type="primary" @click="open()">＋ 新增墓位</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="plotNo" label="编号" min-width="120" /><el-table-column prop="areaName" label="墓区" />
      <el-table-column label="位置"><template #default="s">{{ s.row.rowNo }}排 {{ s.row.colNo }}号</template></el-table-column>
      <el-table-column prop="plotType" label="类型" min-width="110" />
      <el-table-column label="价格"><template #default="s"><span class="money">{{ money(s.row.price) }}</span></template></el-table-column>
      <el-table-column label="管理费/年"><template #default="s">{{ money(s.row.manageFeeYearly) }}</template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="s"><el-tag :color="PLOT_COLOR[s.row.status]" style="color:#fff;border:none">{{ PLOT_STATUS[s.row.status] }}</el-tag></template></el-table-column>
      <el-table-column prop="buyerName" label="购买人" />
      <el-table-column label="操作" width="220"><template #default="s">
        <el-button size="small" @click="open(s.row)">编辑</el-button>
        <el-button size="small" v-if="s.row.status==='RESERVED'" @click="cancelReserve(s.row)">取消预订</el-button>
        <el-button size="small" type="danger" plain v-if="s.row.status==='FREE'" @click="del(s.row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dlg" :title="form.id ? '编辑墓位' : '新增墓位'" width="500px">
      <el-form label-width="92px">
        <el-form-item label="墓区" v-if="!form.id"><el-select v-model="form.areaId" style="width:100%"><el-option v-for="a in areas" :key="a.id" :value="a.id" :label="a.name" /></el-select></el-form-item>
        <el-form-item label="编号" v-if="!form.id"><el-input v-model="form.plotNo" placeholder="如 A-6排-1号" /></el-form-item>
        <el-form-item label="排 / 号" v-if="!form.id"><el-input-number v-model="form.rowNo" :min="1" /> <el-input-number v-model="form.colNo" :min="1" style="margin-left:8px" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="form.plotType" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" /></el-form-item>
        <el-form-item label="管理费/年"><el-input-number v-model="form.manageFeeYearly" :min="0" /></el-form-item>
        <el-form-item label="朝向"><el-input v-model="form.orientation" /></el-form-item>
        <el-form-item label="状态" v-if="form.id"><el-select v-model="form.status"><el-option v-for="(v,k) in PLOT_STATUS" :key="k" :value="k" :label="v" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="batchDlg" title="批量生成墓位" width="440px">
      <el-form label-width="92px">
        <el-form-item label="墓区"><el-select v-model="batch.areaId" style="width:100%"><el-option v-for="a in areas" :key="a.id" :value="a.id" :label="a.name" /></el-select></el-form-item>
        <el-form-item label="排数 × 列数"><el-input-number v-model="batch.rows" :min="1" :max="20" /> × <el-input-number v-model="batch.cols" :min="1" :max="20" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="batch.plotType" /></el-form-item>
        <el-form-item label="统一价格"><el-input-number v-model="batch.price" :min="0" /></el-form-item>
        <el-form-item label="管理费/年"><el-input-number v-model="batch.manageFeeYearly" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="batchDlg=false">取消</el-button><el-button type="primary" @click="doBatch">生成</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { money, PLOT_STATUS, PLOT_COLOR } from '../utils/format'
const rows = ref([]); const areas = ref([]); const dlg = ref(false); const batchDlg = ref(false)
const q = reactive({ areaId: null, status: '', kw: '' }); const form = reactive({})
const batch = reactive({ areaId: null, rows: 3, cols: 6, plotType: '双穴立碑墓', price: 18800, manageFeeYearly: 260 })
async function load(){ const r = await request.get('/plots', { params: q }); rows.value = r.data }
function open(p){ Object.assign(form, p ? { ...p } : { areaId: areas.value[0]?.id, plotNo: '', rowNo: 1, colNo: 1, plotType: '双穴立碑墓', price: 18800, manageFeeYearly: 260, orientation: '坐北朝南' }); dlg.value = true }
async function save(){ try { if (form.id) await request.put('/plots/' + form.id, form); else await request.post('/plots', form); ElMessage.success('已保存'); dlg.value = false; load() } catch (e) {} }
async function doBatch(){ try { const r = await request.post('/plots/batch-generate', batch); ElMessage.success('新生成墓位 ' + r.data.created + ' 个'); batchDlg.value = false; load() } catch (e) {} }
async function cancelReserve(p){ try { await request.post(`/plots/${p.id}/cancel-reserve`); ElMessage.success('已取消预订'); load() } catch (e) {} }
async function del(p){ await ElMessageBox.confirm('确认删除墓位 ' + p.plotNo + '？', '提示'); try { await request.delete('/plots/' + p.id); ElMessage.success('已删除'); load() } catch (e) {} }
onMounted(async () => { const r = await request.get('/areas'); areas.value = r.data.map(x => x.area); load() })
</script>
