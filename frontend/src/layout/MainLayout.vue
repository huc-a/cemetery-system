<template>
  <el-container class="shell">
    <el-aside width="218px" class="aside">
      <div class="logo">🌲 青松陵园</div>
      <div class="logo-sub">智慧墓园管理平台</div>
      <el-menu :default-active="$route.path" router class="menu" background-color="transparent" text-color="#cfd8d2" active-text-color="#e9c979">
        <el-menu-item index="/dashboard"><el-icon><DataBoard /></el-icon>数据仪表盘</el-menu-item>
        <el-menu-item index="/areas"><el-icon><MapLocation /></el-icon>墓区管理</el-menu-item>
        <el-menu-item index="/plot-map"><el-icon><Grid /></el-icon>可视化选墓</el-menu-item>
        <el-menu-item index="/plots"><el-icon><Tickets /></el-icon>墓位管理</el-menu-item>
        <el-menu-item index="/deceased"><el-icon><Notebook /></el-icon>逝者档案</el-menu-item>
        <el-menu-item index="/contracts"><el-icon><Document /></el-icon>购墓合同</el-menu-item>
        <el-menu-item index="/fees"><el-icon><Wallet /></el-icon>管理费与续费</el-menu-item>
        <el-menu-item index="/burials"><el-icon><Flag /></el-icon>安葬管理</el-menu-item>
        <el-menu-item index="/visits"><el-icon><Calendar /></el-icon>祭扫预约</el-menu-item>
        <el-menu-item index="/memorials"><el-icon><Sunny /></el-icon>在线纪念馆</el-menu-item>
        <el-menu-item index="/services"><el-icon><Goods /></el-icon>服务项目</el-menu-item>
        <el-menu-item index="/service-orders"><el-icon><List /></el-icon>服务订单</el-menu-item>
        <el-menu-item index="/work-orders"><el-icon><Tools /></el-icon>巡查养护工单</el-menu-item>
        <el-menu-item index="/payments"><el-icon><Money /></el-icon>财务流水</el-menu-item>
        <el-menu-item index="/notices"><el-icon><Bell /></el-icon>通知公告</el-menu-item>
        <el-menu-item v-if="auth.isAdmin" index="/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
        <el-menu-item index="/my"><el-icon><House /></el-icon>家属中心</el-menu-item>
        <el-menu-item index="/profile"><el-icon><Setting /></el-icon>个人中心</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="htitle">{{ $route.meta.title || '智慧墓园' }}</div>
        <div class="huser">
          <el-tag v-if="feeAlert>0" type="danger" effect="dark" class="alert-tag" @click="$router.push('/fees')">⚠ 管理费逾期/临期 {{ feeAlert }} 笔</el-tag>
          <span>{{ auth.user?.avatarEmoji }} {{ auth.user?.realName }}（{{ roleName }}）</span>
          <el-button size="small" @click="goFront">家属前台</el-button>
          <el-button size="small" type="danger" plain @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main"><router-view :key="$route.fullPath" /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import request from '../utils/request'
const auth = useAuthStore(); const router = useRouter(); const feeAlert = ref(0)
const roleName = computed(() => ({ ADMIN: '管理员', STAFF: '园区工作人员', FAMILY: '家属' }[auth.role] || auth.role))
function logout(){ auth.logout(); router.push('/admin/login') }
function goFront(){ router.push('/home') }
onMounted(async () => { try { const r = await request.get('/fees/alerts'); feeAlert.value = (r.data.overdueCount || 0) + (r.data.dueSoonCount || 0) } catch (e) {} })
</script>
<style scoped>
.shell{ height:100vh }
.aside{ background:linear-gradient(180deg,#123128,#1a4636 60%,#123128); padding:16px 10px; overflow-y:auto }
.logo{ color:#f3ead3; font-size:19px; font-weight:700; padding:4px 10px } .logo-sub{ color:#c9a45c; font-size:12px; padding:0 10px 14px }
.menu{ border:none } .menu :deep(.el-menu-item){ border-radius:8px; margin:2px 0 } .menu :deep(.el-menu-item.is-active){ background:rgba(201,164,92,.18) }
.header{ background:#fff; display:flex; align-items:center; justify-content:space-between; border-bottom:1px solid #e8e2d2 }
.htitle{ font-size:17px; font-weight:700; color:#173f35 } .huser{ display:flex; align-items:center; gap:10px; font-size:13px }
.alert-tag{ cursor:pointer } .main{ padding:18px }
</style>
