<script setup>
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  AriaComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

use([
  AriaComponent,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  BarChart,
  LineChart,
  PieChart,
  CanvasRenderer,
])

const props = defineProps({
  title: {
    type: String,
    default: '图表',
  },
  subtitle: {
    type: String,
    default: '',
  },
  option: {
    type: Object,
    required: true,
  },
  height: {
    type: Number,
    default: 320,
  },
})

const chartRef = ref(null)
let chartInstance = null

const chartStyle = computed(() => ({
  height: `${props.height}px`,
}))

async function renderChart() {
  await nextTick()
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = init(chartRef.value)
  }
  chartInstance.setOption(props.option, true)
}

function handleResize() {
  chartInstance?.resize()
}

watch(() => props.option, renderChart, { deep: true })

onMounted(() => {
  renderChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<template>
  <article class="app-card chart-panel">
    <div class="chart-head">
      <div>
        <h3>{{ title }}</h3>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
    </div>
    <div ref="chartRef" class="chart-canvas" :style="chartStyle" />
  </article>
</template>

<style scoped lang="scss">
.chart-panel {
  padding: 22px;
}

.chart-head {
  margin-bottom: 14px;

  h3 {
    margin: 0;
    font-size: 18px;
  }

  p {
    margin: 8px 0 0;
    color: var(--text-secondary);
    line-height: 1.6;
    font-size: 13px;
  }
}

.chart-canvas {
  width: 100%;
}
</style>
