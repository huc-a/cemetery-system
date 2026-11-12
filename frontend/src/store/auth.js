import { defineStore } from 'pinia'
import request from '../utils/request'
export const useAuthStore = defineStore('auth', {
  state: () => ({ token: localStorage.getItem('cem_token') || '', user: JSON.parse(localStorage.getItem('cem_user') || 'null') }),
  getters: {
    role: s => (s.user ? s.user.role : ''),
    isAdmin: s => s.user && s.user.role === 'ADMIN',
    isStaffSide: s => s.user && s.user.role !== 'FAMILY',
    isFamily: s => s.user && s.user.role === 'FAMILY'
  },
  actions: {
    async login(username, password) {
      const res = await request.post('/auth/login', { username, password })
      this.token = res.data.token; this.user = res.data.user
      localStorage.setItem('cem_token', this.token)
      localStorage.setItem('cem_user', JSON.stringify(this.user))
      return this.user
    },
    logout() { this.token = ''; this.user = null; localStorage.removeItem('cem_token'); localStorage.removeItem('cem_user') }
  }
})
