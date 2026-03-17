<script setup>
defineProps({
  title: {
    type: String,
    default: '功能页面',
  },
  description: {
    type: String,
    default: '该页面骨架已预留，可以继续接入真实接口和交互。',
  },
})
</script>

<template>
  <div class="page-wrap">
    <div class="page-heading">
      <div>
        <h1>{{ title }}</h1>
        <p>{{ description }}</p>
      </div>
    </div>
    <section class="content-grid">
      <article class="app-card panel-block">
        <h3>当前状态</h3>
        <div class="soft-list">
          <div class="soft-item">
            <strong>页面入口已接入动态菜单</strong>
            <span>路由、布局、权限链路已经可联调。</span>
          </div>
          <div class="soft-item">
            <strong>可以继续接后端接口</strong>
            <span>下一步适合接列表、详情、表单与操作反馈。</span>
          </div>
        </div>
      </article>
      <article class="app-card panel-block">
        <h3>建议动作</h3>
        <div class="soft-list">
          <div class="soft-item">
            <strong>先补接口联调</strong>
            <span>把当前模块的查询、保存、审核等操作逐步接入。</span>
          </div>
          <div class="soft-item">
            <strong>再做表单与图表细节</strong>
            <span>保证先有完整流程，再强化体验和可视化。</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>
