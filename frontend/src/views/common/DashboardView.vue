<script setup>
import { computed } from 'vue'
import { usePortalStore } from '../../stores/portal'
import EChartPanel from './EChartPanel.vue'

const portalStore = usePortalStore()
const dashboard = computed(() => portalStore.dashboard || { roleCode: '', cards: [], extra: {} })

const palette = ['#a1642f', '#6f9560', '#d59d55', '#5186a4', '#c56d52', '#7d6ab0']

const primaryChartOption = computed(() => {
  const cards = dashboard.value.cards || []
  return {
    color: palette,
    tooltip: { trigger: 'axis' },
    grid: { left: 20, right: 20, top: 40, bottom: 20, containLabel: true },
    xAxis: {
      type: 'category',
      axisLine: { lineStyle: { color: 'rgba(77,60,38,.2)' } },
      axisLabel: { color: '#6a5b48' },
      data: cards.map((item) => item.title),
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(77,60,38,.08)' } },
      axisLabel: { color: '#6a5b48' },
    },
    series: [
      {
        type: 'bar',
        barWidth: 28,
        itemStyle: {
          borderRadius: [10, 10, 0, 0],
        },
        data: cards.map((item, index) => ({ value: item.value, itemStyle: { color: palette[index % palette.length] } })),
      },
    ],
  }
})

const ratioChartOption = computed(() => {
  const cards = dashboard.value.cards || []
  return {
    color: palette,
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#6a5b48' } },
    series: [
      {
        type: 'pie',
        radius: ['42%', '72%'],
        center: ['50%', '45%'],
        itemStyle: { borderRadius: 10, borderColor: '#fffaf4', borderWidth: 3 },
        label: { color: '#2e2418' },
        data: cards.slice(0, 5).map((item) => ({ name: item.title, value: item.value })),
      },
    ],
  }
})

const trendChartOption = computed(() => {
  const cards = dashboard.value.cards || []
  const values = cards.map((item) => item.value)
  return {
    color: ['#7f4b21'],
    tooltip: { trigger: 'axis' },
    grid: { left: 20, right: 20, top: 36, bottom: 20, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLine: { lineStyle: { color: 'rgba(77,60,38,.2)' } },
      axisLabel: { color: '#6a5b48' },
      data: cards.map((item) => item.key),
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(77,60,38,.08)' } },
      axisLabel: { color: '#6a5b48' },
    },
    series: [
      {
        data: values,
        type: 'line',
        smooth: true,
        symbolSize: 8,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(161,100,47,.35)' },
              { offset: 1, color: 'rgba(161,100,47,.03)' },
            ],
          },
        },
      },
    ],
  }
})

const insightList = computed(() => {
  const roleCode = dashboard.value.roleCode
  const extra = dashboard.value.extra || {}
  if (roleCode === 'ADMIN') {
    return [
      { title: '待审核课程', text: `当前有 ${extra.pendingCourseAudit ?? 0} 门课程等待管理员审核。` },
      { title: '上架率', text: `当前课程上架率约为 ${extra.publishedCourseRate ?? 0}% ，适合继续补齐课程内容与审核节奏。` },
      { title: '平台观察', text: '优先关注课程审核、证书审核与新增教师后的课程生产速度。' },
    ]
  }
  if (roleCode === 'TEACHER') {
    return [
      { title: '课程准备度', text: `当前草稿课程 ${extra.draftCourses ?? 0} 门，可继续完善章节与课时后提交审核。` },
      { title: '教学覆盖', text: `当前累计选课人次 ${extra.teachingStudents ?? 0}，适合继续追踪学生学习与 AI 提问情况。` },
      { title: '教学建议', text: '课程、考试、作业建议保持联动，让教学链路更完整。' },
    ]
  }
  return [
    { title: '课程完成率', text: `当前总体完成率约为 ${extra.completionRate ?? 0}% ，建议优先完成正在学习中的课程。` },
    { title: '学习中课程', text: `当前仍有 ${extra.activeLearningCourses ?? 0} 门课程处于学习中状态。` },
    { title: '学习建议', text: '建议结合 AI 辅导、考试和作业节点安排复习节奏。' },
  ]
})

const spotlightCards = computed(() => {
  const cards = dashboard.value.cards || []
  return cards.slice(0, 3)
})
</script>

<template>
  <div class="page-wrap dashboard-page">
    <div class="page-heading">
      <div>
        <h1>{{ dashboard.title || '工作台' }}</h1>
        <p>通过实时指标、结构图表与运营提示，快速把握当前角色下最重要的工作重点。</p>
      </div>
    </div>

    <section class="stat-grid">
      <article v-for="card in dashboard.cards || []" :key="card.key" class="app-card stat-card featured-stat">
        <div class="label">{{ card.title }}</div>
        <div class="value">{{ card.value }}<small>{{ card.unit || '' }}</small></div>
      </article>
    </section>

    <section class="hero-band app-card">
      <div class="hero-copy">
        <span class="eyebrow">Role Insight</span>
        <h2>{{ dashboard.title || '工作台总览' }}</h2>
        <p>核心数据已经转成可视化面板，方便在演示和日常使用时更直观地观察业务结构。</p>
      </div>
      <div class="spotlight-grid">
        <div v-for="card in spotlightCards" :key="card.key" class="spotlight-item">
          <strong>{{ card.value }}</strong>
          <span>{{ card.title }}</span>
        </div>
      </div>
    </section>

    <section class="chart-grid">
      <EChartPanel
        title="核心指标柱状图"
        subtitle="按当前角色展示最关键的数量型指标"
        :option="primaryChartOption"
        :height="340"
      />
      <EChartPanel
        title="结构占比"
        subtitle="从整体结构看当前角色下最主要的业务构成"
        :option="ratioChartOption"
        :height="340"
      />
    </section>

    <section class="content-grid dashboard-grid">
      <EChartPanel
        title="指标趋势视图"
        subtitle="以折线形式观察不同核心指标之间的相对变化"
        :option="trendChartOption"
        :height="320"
      />

      <article class="app-card panel-block insight-panel">
        <h3>运营提示</h3>
        <div class="soft-list">
          <div v-for="item in insightList" :key="item.title" class="soft-item">
            <strong>{{ item.title }}</strong>
            <span>{{ item.text }}</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped lang="scss">
small {
  margin-left: 4px;
  font-size: 14px;
  color: var(--text-secondary);
}

.dashboard-page {
  gap: 18px;
}

.featured-stat {
  position: relative;
  overflow: hidden;
}

.featured-stat::after {
  content: '';
  position: absolute;
  inset: auto -24px -44px auto;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(161, 100, 47, 0.18), transparent 72%);
}

.hero-band {
  padding: 24px 28px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 18px;
  align-items: center;
}

.hero-copy {
  h2 {
    margin: 10px 0 10px;
    font-family: var(--font-display);
    font-size: 34px;
  }

  p {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.8;
  }
}

.eyebrow {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--brand-soft);
  color: var(--brand-deep);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.spotlight-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.spotlight-item {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid var(--line-soft);

  strong,
  span {
    display: block;
  }

  strong {
    font-size: 28px;
    font-family: var(--font-display);
  }

  span {
    margin-top: 8px;
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.5;
  }
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.dashboard-grid {
  grid-template-columns: 1.2fr 0.8fr;
}

.insight-panel {
  min-height: 100%;
}

@media (max-width: 1080px) {
  .hero-band,
  .chart-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .spotlight-grid {
    grid-template-columns: 1fr;
  }

  .hero-copy h2 {
    font-size: 28px;
  }
}
</style>
