<template>
  <div class="iu">
    <div class="thumbs">
      <div v-for="(u, i) in urls" :key="i" class="thumb" :style="{ width: size + 'px', height: size + 'px' }">
        <img :src="u" alt="图片" />
        <div class="ops">
          <span @click="remove(i)">删除</span>
        </div>
      </div>
      <el-upload v-if="urls.length < limit" :show-file-list="false" :http-request="doUpload"
        :before-upload="before" accept="image/jpeg,image/png,image/webp" class="up">
        <div class="add" :style="{ width: size + 'px', height: size + 'px' }">
          <div class="plus">＋</div>
          <div class="txt">{{ urls.length ? '更换/添加' : '上传图片' }}</div>
        </div>
      </el-upload>
    </div>
    <el-input v-if="!multiple" :model-value="modelValue" size="small" clearable
      placeholder="或手动填写图片 URL（如 /img/xxx.jpg）" style="margin-top:8px;max-width:340px"
      @update:model-value="v => emit('update:modelValue', v)" />
    <div class="hint">支持 jpg / png / webp，单张 ≤ 5MB{{ multiple ? '，可传多张' : '' }}</div>
  </div>
</template>
<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  multiple: { type: Boolean, default: false },
  max: { type: Number, default: 6 },
  size: { type: Number, default: 96 }
})
const emit = defineEmits(['update:modelValue'])
const limit = computed(() => props.multiple ? props.max : 1)
const urls = computed(() => {
  if (props.multiple) return Array.isArray(props.modelValue) ? props.modelValue : (props.modelValue ? String(props.modelValue).split(',').filter(Boolean) : [])
  return props.modelValue ? [props.modelValue] : []
})
const before = file => {
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) { ElMessage.error('只支持 jpg/png/webp 图片'); return false }
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片大小不能超过 5MB'); return false }
  return true
}
const doUpload = async opt => {
  const fd = new FormData()
  fd.append('file', opt.file)
  try {
    const res = await request.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    const url = res.data.url
    if (props.multiple) emit('update:modelValue', [...urls.value, url])
    else emit('update:modelValue', url)
    ElMessage.success('上传成功')
  } catch (e) { /* request 拦截器已提示 */ }
}
const remove = i => {
  if (props.multiple) { const arr = [...urls.value]; arr.splice(i, 1); emit('update:modelValue', arr) }
  else emit('update:modelValue', '')
}
</script>
<style scoped>
.thumbs { display: flex; gap: 10px; flex-wrap: wrap; }
.thumb { border-radius: 12px; overflow: hidden; position: relative; border: 1px solid #d8dfd5; }
.thumb img { width: 100%; height: 100%; object-fit: cover; display: block; }
.ops { position: absolute; left: 0; right: 0; bottom: 0; background: rgba(23,63,53,.72); color: #fff; font-size: 12px; text-align: center; padding: 3px 0; opacity: 0; transition: .2s; cursor: pointer; }
.thumb:hover .ops { opacity: 1; }
.add { border: 1.5px dashed #7ba05b; border-radius: 12px; background: #f4f7f1; color: #1f5747; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; }
.add:hover { background: #e9f0e2; border-color: #c9a45c; }
.plus { font-size: 26px; line-height: 1; }
.txt { font-size: 12px; margin-top: 4px; }
.hint { color: #9aa39d; font-size: 12px; margin-top: 6px; }
</style>
