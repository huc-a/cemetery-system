import { createRouter, createWebHashHistory } from 'vue-router'
const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true, guestEntry: true } },
  { path: '/register', component: () => import('../views/Register.vue'), meta: { public: true, guestEntry: true } },
  { path: '/admin/login', component: () => import('../views/AdminLogin.vue'), meta: { public: true, staffEntry: true } },
  { path: '/', component: () => import('../layout/RootShell.vue'), children: [
    { path: '', redirect: () => defaultHome() },
    { path: 'home', component: () => import('../views/front/Home.vue'), meta: { title: '首页', front: true, guest: true } },
    { path: 'memorial-wall', component: () => import('../views/front/MemorialWall.vue'), meta: { title: '云祭扫纪念馆', front: true, guest: true } },
    { path: 'front-services', component: () => import('../views/front/FrontServices.vue'), meta: { title: '服务中心', front: true, guest: true } },
    { path: 'memorials/:id', component: () => import('../views/MemorialDetail.vue'), meta: { title: '纪念馆' } },
    { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据仪表盘', staffOnly: true } },
    { path: 'areas', component: () => import('../views/Areas.vue'), meta: { title: '墓区管理', staffOnly: true } },
    { path: 'plot-map', component: () => import('../views/PlotMap.vue'), meta: { title: '可视化选墓', guest: true } },
    { path: 'plots', component: () => import('../views/Plots.vue'), meta: { title: '墓位管理', staffOnly: true } },
    { path: 'deceased', component: () => import('../views/Deceased.vue'), meta: { title: '逝者档案' } },
    { path: 'contracts', component: () => import('../views/Contracts.vue'), meta: { title: '购墓合同' } },
    { path: 'fees', component: () => import('../views/Fees.vue'), meta: { title: '管理费与续费' } },
    { path: 'burials', component: () => import('../views/Burials.vue'), meta: { title: '安葬管理' } },
    { path: 'visits', component: () => import('../views/Visits.vue'), meta: { title: '祭扫预约' } },
    { path: 'memorials', component: () => import('../views/Memorials.vue'), meta: { title: '在线纪念馆' } },
    { path: 'services', component: () => import('../views/Services.vue'), meta: { title: '服务项目', staffOnly: true } },
    { path: 'service-orders', component: () => import('../views/ServiceOrders.vue'), meta: { title: '服务订单' } },
    { path: 'work-orders', component: () => import('../views/WorkOrders.vue'), meta: { title: '巡查养护工单' } },
    { path: 'payments', component: () => import('../views/Payments.vue'), meta: { title: '财务流水' } },
    { path: 'notices', component: () => import('../views/Notices.vue'), meta: { title: '通知公告', guest: true } },
    { path: 'users', component: () => import('../views/Users.vue'), meta: { title: '用户管理', staffOnly: true } },
    { path: 'my', component: () => import('../views/MyCenter.vue'), meta: { title: '家属中心', front: true } },
    { path: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心' } },
  ] },
  { path: '/:pathMatch(.*)*', redirect: () => defaultHome() }
]
const router = createRouter({ history: createWebHashHistory(), routes })
const currentRole = () => { try { return (JSON.parse(localStorage.getItem('cem_user') || 'null') || {}).role || '' } catch (e) { return '' } }
const hasToken = () => !!localStorage.getItem('cem_token')
function defaultHome(){ if (!hasToken()) return '/home'; return currentRole() === 'FAMILY' ? '/home' : '/dashboard' }
const STAFF_PATHS = ['/dashboard', '/areas', '/plots', '/users', '/services']
router.beforeEach((to, from, next) => {
  const token = hasToken(); const role = currentRole()
  if (to.meta.public) {
    if (!token) return next()
    if (to.meta.guestEntry && role !== 'FAMILY') return next('/dashboard')
    if (to.meta.staffEntry && role === 'FAMILY') return next(to.query.redirect || '/home')
    if (to.meta.guestEntry && role === 'FAMILY') return next(to.query.redirect || '/home')
    return next()
  }
  if (!token) {
    if (to.meta.guest) return next()
    if (to.path === '/' || to.path === '') return next('/home')
    if (STAFF_PATHS.includes(to.path)) return next({ path: '/admin/login' })
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  if (to.path === '/') return next(defaultHome())
  if (role === 'FAMILY' && to.meta.staffOnly) return next('/home')
  next()
})
export default router
