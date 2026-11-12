<template><router-view /></template>
<style>
:root{ --ink:#173f35; --ink2:#1f5747; --gold:#c9a45c; --paper:#f7f4ec; }
*{ box-sizing:border-box } body{ margin:0; font-family:"PingFang SC","Microsoft YaHei",sans-serif; background:var(--paper); color:#2b2b2b }
.el-button--primary{ --el-button-bg-color:#1f5747; --el-button-border-color:#1f5747; --el-button-hover-bg-color:#2a6d58; --el-button-hover-border-color:#2a6d58 }
.page-card{ background:#fff; border-radius:12px; padding:18px; box-shadow:0 2px 12px rgba(23,63,53,.06) }
.page-title{ font-size:19px; font-weight:700; color:var(--ink); margin:0 0 4px }
.page-sub{ color:#8a8f8c; font-size:13px; margin-bottom:14px }
.toolbar{ display:flex; gap:10px; flex-wrap:wrap; align-items:center; margin-bottom:14px }
.toolbar .spacer{ flex:1 }
.money{ color:#b07d2b; font-weight:700 }
</style>
