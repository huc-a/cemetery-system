<template>
  <FrontLayout v-if="isFront" />
  <MainLayout v-else />
</template>
<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from './MainLayout.vue'
import FrontLayout from './FrontLayout.vue'
const route = useRoute()
const isFront = computed(() => !!route.meta.front || !localStorage.getItem('cem_token') || route.path === '/plot-map' && JSON.parse(localStorage.getItem('cem_user')||'null')?.role === 'FAMILY')
</script>
