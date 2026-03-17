<script setup>
defineProps({
  title: String,
  description: String,
  metrics: {
    type: Array,
    default: () => [],
  },
  focus: {
    type: Array,
    default: () => [],
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

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="content-grid">
      <article class="app-card panel-block">
        <h3>页面说明</h3>
        <div class="soft-list">
          <div class="soft-item">
            <strong>已接入菜单与布局</strong>
            <span>当前页面已具备可访问的业务入口，可继续补充查询、分页、搜索和表单操作。</span>
          </div>
          <div class="soft-item">
            <strong>建议先联调核心接口</strong>
            <span>优先对接列表、详情、保存和状态操作，再做体验增强。</span>
          </div>
        </div>
      </article>

      <article class="app-card panel-block">
        <h3>当前关注点</h3>
        <div class="soft-list">
          <div v-for="item in focus" :key="item.title" class="soft-item">
            <strong>{{ item.title }}</strong>
            <span>{{ item.text }}</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>
