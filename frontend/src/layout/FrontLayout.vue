<template>
  <div class="front">
    <header class="fnav">
      <div class="brand" @click="$router.push('/home')">🌲 青松陵园 <span>生命纪念园</span></div>
      <nav>
        <a :class="{on:$route.path==='/home'}" @click="$router.push('/home')">首页</a>
        <a :class="{on:$route.path==='/plot-map'}" @click="$router.push('/plot-map')">可视化选墓</a>
        <a :class="{on:$route.path==='/memorial-wall'}" @click="$router.push('/memorial-wall')">云祭扫 · 纪念馆</a>
        <a :class="{on:$route.path==='/front-services'}" @click="$router.push('/front-services')">服务中心</a>
        <a :class="{on:$route.path==='/visits'}" @click="go('/visits')">祭扫预约</a>
        <a :class="{on:$route.path==='/notices'}" @click="$router.push('/notices')">园区公告</a>
      </nav>
      <div class="fuser">
        <template v-if="auth.user">
          <span class="hello">{{ auth.user.avatarEmoji }} {{ auth.user.realName }}</span>
          <el-button size="small" @click="go('/my')">家属中心</el-button>
          <el-button size="small" text @click="logout">退出</el-button>
        </template>
        <template v-else>
          <el-button size="small" type="primary" @click="$router.push('/login')">家属登录</el-button>
          <el-button size="small" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </header>
    <main class="fmain"><router-view :key="$route.fullPath" /></main>
    <footer class="ffoot">
      <div>青松陵园 · 智慧墓园管理系统 —— 思念有归处，祭扫更从容</div>
      <div class="staff-link" @click="$router.push('/admin/login')">员工 / 管理入口</div>
    </footer>
  </div>
</template>
<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
const auth = useAuthStore(); const router = useRouter()
function go(p){ if(!auth.user){ router.push({path:'/login', query:{redirect:p}}); return } router.push(p) }
function logout(){ auth.logout(); router.push('/home') }
</script>
<style scoped>
.front{ min-height:100vh; display:flex; flex-direction:column }
.fnav{ position:sticky; top:0; z-index:20; display:flex; align-items:center; gap:28px; padding:12px 28px; background:linear-gradient(90deg,#123128,#1f5747); color:#f3ead3; box-shadow:0 2px 10px rgba(0,0,0,.18) }
.brand{ font-size:19px; font-weight:700; cursor:pointer; white-space:nowrap } .brand span{ font-size:12px; color:#c9a45c; margin-left:6px }
nav{ display:flex; gap:20px; flex:1; flex-wrap:wrap } nav a{ cursor:pointer; color:#e8e2d2; font-size:14px } nav a.on, nav a:hover{ color:#e9c979 }
.fuser{ display:flex; align-items:center; gap:8px; white-space:nowrap } .hello{ font-size:13px }
.fmain{ flex:1; width:100%; max-width:1240px; margin:0 auto; padding:22px 20px 34px }
.ffoot{ text-align:center; padding:20px; background:#123128; color:#b9c4bd; font-size:13px }
.staff-link{ margin-top:6px; cursor:pointer; color:#7d8f85; font-size:12px } .staff-link:hover{ color:#c9a45c }
</style>
