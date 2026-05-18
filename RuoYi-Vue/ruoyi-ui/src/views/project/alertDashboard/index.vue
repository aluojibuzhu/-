<template>
  <div class="app-container alert-dashboard-page">
    <div class="dashboard-shell">
      <div class="page-heading">
        <div>
          <h2>预警看板</h2>
          <p>聚合项目预算执行、预警等级和成本变化趋势，辅助快速识别风险项目。</p>
        </div>
        <el-button icon="el-icon-refresh" size="small" @click="loadData">刷新数据</el-button>
      </div>

      <div class="summary-row">
        <div v-for="item in summaryCards" :key="item.key" class="summary-card">
          <span class="summary-label">{{ item.label }}</span>
          <strong class="summary-value" :class="item.key">{{ item.value }}</strong>
          <span class="summary-sub">{{ item.sub }}</span>
        </div>
      </div>

      <div class="health-row">
        <div v-for="item in healthCards" :key="item.key" class="health-card">
          <span class="health-dot" :class="item.key"></span>
          <div>
            <strong>{{ item.count }}</strong>
            <span>{{ item.label }}</span>
          </div>
          <small>{{ item.desc }}</small>
        </div>
      </div>

      <div v-loading="chartLoading" class="chart-grid">
        <section class="chart-card">
          <div class="chart-header">
            <div>
              <h3>成本入账趋势</h3>
            </div>
            <el-radio-group v-model="trendPeriodType" size="mini" @change="handleTrendPeriodChange">
              <el-radio-button label="year">年</el-radio-button>
              <el-radio-button label="month">月</el-radio-button>
              <el-radio-button label="day">日</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-body">
            <div ref="trendChart" class="chart"></div>
          </div>
          <div class="chart-caption">{{ selectedProjectName }}</div>
          <div class="trend-category-switch">
            <el-tooltip v-for="item in trendCategoryOptions" :key="item.key" :content="item.name" placement="top">
              <button
                type="button"
                class="trend-category-dot"
                :class="{ active: String(item.categoryId || '') === String(selectedTrendCategoryId || '') }"
                :style="{ background: item.color }"
                @click="handleTrendCategorySelect(item)"
              ></button>
            </el-tooltip>
          </div>
        </section>

        <section class="chart-card">
          <div class="chart-header">
            <div>
              <h3>成本科目占比</h3>
            </div>
          </div>
          <div class="chart-body">
            <div ref="categoryChart" class="chart"></div>
          </div>
          <div class="chart-caption">{{ selectedProjectName }}</div>
          <div class="chart-legend">
            <span v-for="item in categoryLegend" :key="item.name" class="legend-item">
              <i class="legend-dot" :style="{ background: item.color }"></i>{{ item.name }}
            </span>
          </div>
        </section>
      </div>

      <section class="table-panel">
        <div class="table-title">
          <span>项目健康度与预算执行</span>
          <small>按预算执行率由高到低排序，点击项目行切换图表数据源</small>
        </div>
        <div class="table-toolbar">
          <el-input
            v-model="tableQuery.keyword"
            class="table-search"
            size="small"
            clearable
            prefix-icon="el-icon-search"
            placeholder="搜索项目名称 / 项目编号"
          />
          <el-select v-model="tableQuery.alertLevel" class="table-filter" size="small" placeholder="预警筛选">
            <el-option label="全部预警" value="" />
            <el-option label="红色预警" value="red" />
            <el-option label="橙色预警" value="orange" />
            <el-option label="黄色预警" value="yellow" />
            <el-option label="无活跃预警" value="normal" />
          </el-select>
        </div>
        <div class="table-scroll">
          <el-table
            v-loading="loading"
            :data="filteredProjectRows"
            border
            size="small"
            height="360"
            row-key="projId"
            :row-class-name="projectRowClassName"
            @row-click="handleProjectSelect"
          >
            <el-table-column label="排名" width="64" align="center">
              <template slot-scope="scope">{{ projectRank(scope.row) }}</template>
            </el-table-column>
            <el-table-column prop="projNo" label="项目编号" width="150" show-overflow-tooltip />
            <el-table-column prop="projName" label="项目名称" min-width="220" show-overflow-tooltip />
            <el-table-column label="执行率" width="180" align="center">
              <template slot-scope="scope">
                <div class="rate-cell">
                  <el-progress :percentage="safePercentage(scope.row.execRate)" :stroke-width="6" :show-text="false" :color="progressColor(scope.row.execRate)" />
                  <span>{{ scope.row.execRate || 0 }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="活跃预警" width="92" align="center">
              <template slot-scope="scope"><strong>{{ scope.row.activeAlertCount || alertCount(scope.row) }}</strong></template>
            </el-table-column>
            <el-table-column label="红" width="64" align="center">
              <template slot-scope="scope"><span class="count red">{{ scope.row.redCount || 0 }}</span></template>
            </el-table-column>
            <el-table-column label="橙" width="64" align="center">
              <template slot-scope="scope"><span class="count orange">{{ scope.row.orangeCount || 0 }}</span></template>
            </el-table-column>
            <el-table-column label="黄" width="64" align="center">
              <template slot-scope="scope"><span class="count yellow">{{ scope.row.yellowCount || 0 }}</span></template>
            </el-table-column>
            <el-table-column label="预计开工" width="120" align="center">
              <template slot-scope="scope">{{ formatDate(scope.row.planStartDate) }}</template>
            </el-table-column>
            <el-table-column label="预计竣工" width="120" align="center">
              <template slot-scope="scope">{{ formatDate(scope.row.planEndDate) }}</template>
            </el-table-column>
            <el-table-column label="预算总额" width="140" align="right">
              <template slot-scope="scope">{{ formatMoney(scope.row.totalBudget) }}</template>
            </el-table-column>
            <el-table-column label="实际成本" width="140" align="right">
              <template slot-scope="scope">{{ formatMoney(scope.row.actualCost) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="96" align="center">
              <template slot-scope="scope">
                <el-button type="text" icon="el-icon-view" @click.stop="openProjectDetail(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getAlertSummary, getAlertTop10, getBudgetTrend, getCategoryCompare, getProjectHealth } from '@/api/project/alertDashboard'
import { listCostCategories } from '@/api/project/costCategory'
import { formatMoney } from '@/utils/project'

const CATEGORY_COLORS = ['rgba(24,144,255,.78)', '#16a34a', '#f97316', '#dc2626', '#8b5cf6', '#14b8a6', '#ca8a04', '#64748b']

export default {
  name: 'AlertDashboard',
  data() {
    return {
      loading: false,
      chartLoading: false,
      summary: {},
      healthList: [],
      trendList: [],
      categoryList: [],
      rootCostCategories: [],
      topList: [],
      selectedProjId: null,
      selectedProjectName: '全部项目',
      selectedTrendCategoryId: null,
      selectedTrendCategoryName: '总计',
      selectedTrendCategoryColor: CATEGORY_COLORS[0],
      trendPeriodType: 'month',
      tableQuery: {
        keyword: '',
        alertLevel: ''
      },
      trendChart: null,
      categoryChart: null
    }
  },
  computed: {
    summaryCards() {
      return [
        { key: 'total', label: '累计预警', value: this.summary.totalCount || 0, sub: '全部触发记录' },
        { key: 'active', label: '活跃预警', value: this.summary.activeCount || 0, sub: '待处理与跟进中' },
        { key: 'red', label: '红色预警', value: this.summary.redCount || 0, sub: '严重风险' },
        { key: 'orange', label: '橙色预警', value: this.summary.orangeCount || 0, sub: '重点关注' },
        { key: 'yellow', label: '黄色预警', value: this.summary.yellowCount || 0, sub: '趋势提醒' },
        { key: 'today', label: '今日新增', value: this.summary.todayCount || 0, sub: '当日触发' }
      ]
    },
    healthCards() {
      const normal = this.healthList.filter(item => this.alertCount(item) === 0).length
      const yellow = this.healthList.filter(item => (item.yellowCount || 0) > 0 && (item.orangeCount || 0) === 0 && (item.redCount || 0) === 0).length
      const orange = this.healthList.filter(item => (item.orangeCount || 0) > 0 && (item.redCount || 0) === 0).length
      const red = this.healthList.filter(item => (item.redCount || 0) > 0).length
      return [
        { key: 'normal', label: '正常项目', count: normal, desc: '暂无活跃预警' },
        { key: 'yellow', label: '黄色项目', count: yellow, desc: '预算趋势提醒' },
        { key: 'orange', label: '橙色项目', count: orange, desc: '需要关注处理' },
        { key: 'red', label: '红色项目', count: red, desc: '优先处置风险' }
      ]
    },
    trendPeriodLabel() {
      return { year: '年', month: '月', day: '日' }[this.trendPeriodType] || '月'
    },
    categoryLegend() {
      return this.categoryList.slice(0, 4).map((item, index) => ({
        name: item.categoryName || '未分类',
        color: CATEGORY_COLORS[index]
      }))
    },
    trendCategoryOptions() {
      const options = [{
        key: 'total',
        categoryId: null,
        name: '总计',
        color: CATEGORY_COLORS[0]
      }]
      this.rootCostCategories.forEach((item, index) => {
        options.push({
          key: 'category-' + item.categoryId,
          categoryId: item.categoryId,
          name: item.categoryName || '未分类',
          color: CATEGORY_COLORS[(index + 1) % CATEGORY_COLORS.length]
        })
      })
      return options
    },
    mergedProjectRows() {
      const rowMap = new Map()
      this.healthList.forEach(item => {
        rowMap.set(String(item.projId), { ...item })
      })
      this.topList.forEach(item => {
        const key = String(item.projId)
        rowMap.set(key, this.mergeProjectRow(rowMap.get(key) || {}, item))
      })
      return Array.from(rowMap.values()).sort((a, b) => Number(b.execRate || 0) - Number(a.execRate || 0))
    },
    filteredProjectRows() {
      const keyword = String(this.tableQuery.keyword || '').trim().toLowerCase()
      return this.mergedProjectRows.filter(row => {
        const matchKeyword = !keyword ||
          String(row.projName || '').toLowerCase().includes(keyword) ||
          String(row.projNo || '').toLowerCase().includes(keyword)
        const matchAlert = this.matchAlertFilter(row)
        return matchKeyword && matchAlert
      })
    },
    selectedProject() {
      return this.mergedProjectRows.find(item => String(item.projId) === String(this.selectedProjId)) || null
    },
    displayTrendList() {
      const amountMap = this.buildTrendAmountMap()
      const periods = this.buildTrendPeriods()
      return periods.map(period => ({
        period,
        postingAmount: amountMap.get(period) || 0
      }))
    }
  },
  watch: {
    'tableQuery.keyword': 'ensureVisibleSelection',
    'tableQuery.alertLevel': 'ensureVisibleSelection'
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    if (this.trendChart) this.trendChart.dispose()
    if (this.categoryChart) this.categoryChart.dispose()
  },
  methods: {
    formatMoney,
    loadData() {
      this.loading = true
      Promise.all([getAlertSummary(), getProjectHealth(), getAlertTop10(), this.loadRootCostCategories()]).then(res => {
        this.summary = res[0].data || {}
        this.healthList = res[1].data || []
        this.topList = res[2].data || []
        this.ensureSelectedProject()
        return this.loadChartData(this.selectedProjId)
      }).catch(() => {
        this.$message.error('预警看板数据加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    loadRootCostCategories() {
      return listCostCategories({ status: '0' }).then(res => {
        const rows = res.data || []
        this.rootCostCategories = rows.filter(item => item.categoryLevel === 1 && Number(item.parentId || 0) === 0)
      })
    },
    ensureSelectedProject() {
      const rows = this.filteredProjectRows.length ? this.filteredProjectRows : this.mergedProjectRows
      const selected = rows.find(item => String(item.projId) === String(this.selectedProjId))
      const next = selected || rows[0]
      this.selectedProjId = next ? next.projId : null
      this.selectedProjectName = next ? next.projName : '全部项目'
    },
    ensureVisibleSelection() {
      const selectedVisible = this.filteredProjectRows.some(item => String(item.projId) === String(this.selectedProjId))
      if (selectedVisible || !this.filteredProjectRows.length) {
        this.$nextTick(this.renderCharts)
        return
      }
      this.handleProjectSelect(this.filteredProjectRows[0])
    },
    loadChartData(projId) {
      this.chartLoading = true
      const query = projId ? { projId, periodType: 'day' } : { periodType: 'day' }
      if (this.selectedTrendCategoryId) {
        query.categoryId = this.selectedTrendCategoryId
      }
      return Promise.all([getBudgetTrend(query), getCategoryCompare(query)]).then(res => {
        this.trendList = res[0].data || []
        this.categoryList = res[1].data || []
        this.ensureTrendCategorySelection()
        this.$nextTick(this.renderCharts)
      }).catch(() => {
        this.trendList = []
        this.categoryList = []
        this.$message.error('图表数据加载失败')
        this.$nextTick(this.renderCharts)
      }).finally(() => {
        this.chartLoading = false
      })
    },
    handleTrendPeriodChange() {
      this.$nextTick(this.renderCharts)
    },
    handleProjectSelect(row) {
      if (!row || !row.projId || String(row.projId) === String(this.selectedProjId)) {
        return
      }
      this.selectedProjId = row.projId
      this.selectedProjectName = row.projName || '当前项目'
      this.setTrendCategory(null)
      this.loadChartData(row.projId)
    },
    handleTrendCategorySelect(item) {
      const nextId = item ? item.categoryId : null
      if (String(nextId || '') === String(this.selectedTrendCategoryId || '')) {
        return
      }
      this.setTrendCategory(item)
      this.loadChartData(this.selectedProjId)
    },
    setTrendCategory(item) {
      this.selectedTrendCategoryId = item && item.categoryId ? item.categoryId : null
      this.selectedTrendCategoryName = item && item.name ? item.name : '总计'
      this.selectedTrendCategoryColor = item && item.color ? item.color : CATEGORY_COLORS[0]
    },
    ensureTrendCategorySelection() {
      const currentKey = String(this.selectedTrendCategoryId || '')
      const selected = this.trendCategoryOptions.find(item => String(item.categoryId || '') === currentKey)
      this.setTrendCategory(selected || this.trendCategoryOptions[0])
    },
    projectRowClassName({ row }) {
      return String(row.projId) === String(this.selectedProjId) ? 'project-row-selected' : ''
    },
    projectRank(row) {
      const index = this.mergedProjectRows.findIndex(item => String(item.projId) === String(row.projId))
      return index >= 0 ? index + 1 : '-'
    },
    openProjectDetail(row) {
      if (!row || !row.projId) {
        this.$message.warning('项目ID不存在，无法打开详情')
        return
      }
      this.$router.push('/project/projInfo/detail/' + row.projId)
    },
    mergeProjectRow(base, next) {
      return {
        ...base,
        ...next,
        planStartDate: next.planStartDate || base.planStartDate,
        planEndDate: next.planEndDate || base.planEndDate
      }
    },
    matchAlertFilter(row) {
      const level = this.tableQuery.alertLevel
      if (!level) return true
      if (level === 'red') return Number(row.redCount || 0) > 0
      if (level === 'orange') return Number(row.orangeCount || 0) > 0
      if (level === 'yellow') return Number(row.yellowCount || 0) > 0
      if (level === 'normal') return this.alertCount(row) === 0
      return true
    },
    buildTrendAmountMap() {
      const amountMap = new Map()
      this.trendList.forEach(item => {
        const date = this.parseDate(item.period)
        if (!date) {
          return
        }
        const bucket = this.formatPeriod(this.normalizeTrendBoundary(date))
        amountMap.set(bucket, (amountMap.get(bucket) || 0) + Number(item.postingAmount || 0))
      })
      return amountMap
    },
    buildTrendPeriods() {
      const selected = this.selectedProject || {}
      const rawStart = this.parseDate(selected.planStartDate) || this.firstTrendDate()
      const rawEnd = this.parseDate(selected.planEndDate) || this.lastTrendDate() || rawStart
      const start = this.normalizeTrendBoundary(rawStart)
      const end = this.normalizeTrendBoundary(rawEnd)
      if (!start || !end) {
        return this.trendList.map(item => item.period)
      }
      if (end < start) {
        return [this.formatPeriod(start)]
      }
      const periods = []
      const current = new Date(start.getTime())
      const limit = new Date(end.getTime())
      while (current <= limit) {
        periods.push(this.formatPeriod(current))
        if (this.trendPeriodType === 'year') {
          current.setFullYear(current.getFullYear() + 1, 0, 1)
        } else if (this.trendPeriodType === 'day') {
          current.setDate(current.getDate() + 1)
        } else {
          current.setMonth(current.getMonth() + 1, 1)
        }
      }
      return Array.from(new Set(periods))
    },
    normalizeTrendBoundary(date) {
      if (!date) return null
      const normalized = new Date(date.getTime())
      if (this.trendPeriodType === 'year') {
        normalized.setMonth(0, 1)
      } else if (this.trendPeriodType === 'month') {
        normalized.setDate(1)
      }
      normalized.setHours(0, 0, 0, 0)
      return normalized
    },
    firstTrendDate() {
      return this.trendList.length ? this.parseDate(this.trendList[0].period) : null
    },
    lastTrendDate() {
      return this.trendList.length ? this.parseDate(this.trendList[this.trendList.length - 1].period) : null
    },
    parseDate(value) {
      if (!value) return null
      const text = String(value).slice(0, 10)
      const date = new Date(text.replace(/-/g, '/'))
      return Number.isNaN(date.getTime()) ? null : date
    },
    parsePeriod(period) {
      if (!period) return null
      const text = String(period)
      if (this.trendPeriodType === 'year') return this.parseDate(text + '-01-01')
      if (this.trendPeriodType === 'day') return this.parseDate(text)
      return this.parseDate(text + '-01')
    },
    formatDate(value) {
      const date = this.parseDate(value)
      return date ? this.formatDay(date) : '-'
    },
    formatPeriod(date) {
      if (this.trendPeriodType === 'year') return String(date.getFullYear())
      if (this.trendPeriodType === 'day') return this.formatDay(date)
      return date.getFullYear() + '-' + this.pad(date.getMonth() + 1)
    },
    formatAxisLabel(value) {
      const text = String(value || '')
      if (this.trendPeriodType === 'day' && text.length >= 10) {
        return text.slice(5)
      }
      return text
    },
    formatDay(date) {
      return date.getFullYear() + '-' + this.pad(date.getMonth() + 1) + '-' + this.pad(date.getDate())
    },
    pad(value) {
      return String(value).padStart(2, '0')
    },
    renderCharts() {
      this.renderTrend()
      this.renderCategory()
    },
    renderTrend() {
      if (!this.trendChart) this.trendChart = echarts.init(this.$refs.trendChart)
      const trendData = this.displayTrendList
      const showZoom = this.trendPeriodType === 'day' && trendData.length > 45
      const labelInterval = this.trendPeriodType === 'day' && trendData.length > 30 ? Math.ceil(trendData.length / 8) : 0
      const zoomStart = showZoom ? Math.max(0, 100 - (45 / trendData.length) * 100) : 0
      const axisUnit = this.resolveAmountAxisUnit(trendData)
      const lineColor = this.selectedTrendCategoryColor || CATEGORY_COLORS[0]
      this.trendChart.setOption({
        color: [lineColor],
        grid: { left: 76, right: 20, top: 32, bottom: showZoom ? 72 : 44 },
        tooltip: {
          trigger: 'axis',
          formatter: params => {
            const point = params && params[0] ? params[0] : {}
            return point.axisValue + '<br/>' + point.marker + this.selectedTrendCategoryName + '：' + this.formatMoney(point.data || 0)
          }
        },
        xAxis: {
          type: 'category',
          data: trendData.map(item => item.period),
          axisTick: { show: false },
          axisLabel: {
            color: '#4b5563',
            fontSize: 12,
            interval: labelInterval,
            formatter: value => this.formatAxisLabel(value),
            margin: 12
          },
          axisLine: { lineStyle: { color: '#cbd5e1' } }
        },
        yAxis: {
          type: 'value',
          name: axisUnit.name,
          nameLocation: 'end',
          nameGap: 18,
          nameTextStyle: { color: '#697586', align: 'left', padding: [0, 0, 0, 8] },
          axisLabel: {
            color: '#697586',
            formatter: value => this.formatAxisAmount(value, axisUnit)
          },
          splitLine: { lineStyle: { color: '#e5eaf2' } }
        },
        dataZoom: showZoom ? [
          { type: 'inside', start: zoomStart, end: 100 },
          {
            type: 'slider',
            start: zoomStart,
            end: 100,
            bottom: 8,
            height: 18,
            borderColor: '#d9e1ec',
            fillerColor: 'rgba(24,144,255,.12)',
            handleStyle: { color: '#1890ff' },
            textStyle: { color: '#697586' }
          }
        ] : [],
        series: [{
          type: 'line',
          smooth: true,
          symbolSize: 7,
          data: trendData.map(item => Number(item.postingAmount || 0)),
          lineStyle: { width: 3 },
          areaStyle: {
            opacity: 0.18,
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: lineColor },
              { offset: 1, color: 'rgba(255,255,255,0)' }
            ])
          }
        }]
      }, true)
    },
    resolveAmountAxisUnit(rows) {
      const max = Math.max(...rows.map(item => Number(item.postingAmount || 0)), 0)
      if (max >= 100000000) {
        return { name: '单位：亿元', divisor: 100000000, precision: 2 }
      }
      if (max >= 10000) {
        return { name: '单位：万元', divisor: 10000, precision: 1 }
      }
      return { name: '单位：元', divisor: 1, precision: 0 }
    },
    formatAxisAmount(value, unit) {
      const number = Number(value || 0) / unit.divisor
      if (unit.precision === 0) {
        return String(Math.round(number))
      }
      return Number(number.toFixed(unit.precision)).toString()
    },
    renderCategory() {
      if (!this.categoryChart) this.categoryChart = echarts.init(this.$refs.categoryChart)
      this.categoryChart.setOption({
        color: CATEGORY_COLORS,
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie',
          radius: ['48%', '70%'],
          center: ['50%', '48%'],
          data: this.categoryList.map(item => ({
            name: item.categoryName || '未分类',
            value: Number(item.actualCost || 0)
          })),
          label: { formatter: '{b}', color: '#606266' },
          labelLine: { lineStyle: { color: '#d9e1ec' } }
        }]
      }, true)
    },
    resizeCharts() {
      if (this.trendChart) this.trendChart.resize()
      if (this.categoryChart) this.categoryChart.resize()
    },
    progressColor(value) {
      const rate = Number(value || 0)
      if (rate >= 100) return '#dc2626'
      if (rate >= 90) return '#f97316'
      if (rate >= 80) return '#ca8a04'
      return '#16a34a'
    },
    safePercentage(value) {
      return Math.max(0, Math.min(100, Number(value || 0)))
    },
    alertCount(item) {
      return Number(item.redCount || 0) + Number(item.orangeCount || 0) + Number(item.yellowCount || 0)
    }
  }
}
</script>

<style scoped>
.alert-dashboard-page {
  --bg-page: #f5f7fa;
  --bg-card: #ffffff;
  --text-primary: #1f2d3d;
  --text-regular: #606266;
  --text-secondary: #8c98a8;
  --color-primary: #1890ff;
  --color-danger: #dc2626;
  --color-warning: #f97316;
  --color-yellow: #ca8a04;
  --color-success: #16a34a;
  --border-subtle: #e6ebf2;
  min-height: calc(100vh - 84px);
  background: var(--bg-page);
  color: var(--text-primary);
}
.dashboard-shell {
  max-width: 1440px;
  margin: 0 auto;
  padding: 28px 40px;
}
.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 0;
  margin-bottom: 28px;
  background: transparent;
  border: 0;
}
.page-heading h2 {
  margin: 0 0 6px;
  color: var(--text-primary);
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0;
}
.page-heading p {
  margin: 0;
  max-width: 640px;
  color: var(--text-secondary);
  font-size: 13px;
}
.summary-row {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 28px;
}
.summary-card {
  min-width: 0;
  padding: 0;
  background: transparent;
  border: 0;
}
.summary-label,
.summary-sub {
  display: block;
  color: var(--text-secondary);
  font-size: 13px;
}
.summary-sub {
  margin-top: 6px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.summary-value {
  display: block;
  color: var(--text-primary);
  font-size: 26px;
  font-weight: 700;
  line-height: 1.15;
}
.summary-value.red { color: var(--color-danger); }
.summary-value.orange { color: var(--color-warning); }
.summary-value.yellow { color: var(--color-yellow); }
.health-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.health-card {
  display: grid;
  grid-template-columns: 16px 1fr;
  gap: 4px 10px;
  align-items: center;
  min-width: 0;
}
.health-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
}
.health-dot.normal { background: var(--color-success); }
.health-dot.yellow { background: var(--color-yellow); }
.health-dot.orange { background: var(--color-warning); }
.health-dot.red { background: var(--color-danger); }
.health-card strong {
  margin-right: 8px;
  color: var(--text-primary);
  font-size: 22px;
  line-height: 24px;
}
.health-card span:not(.health-dot) {
  color: var(--text-regular);
  font-size: 13px;
}
.health-card small {
  grid-column: 2;
  color: var(--text-secondary);
  font-size: 12px;
}
.chart-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(320px, .95fr);
  gap: 16px;
  margin-bottom: 18px;
}
.chart-card {
  min-height: 414px;
  display: flex;
  flex-direction: column;
  background: transparent;
  border: 0;
  padding: 0;
}
.chart-header {
  min-height: 48px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}
.chart-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0;
}
.chart-header p {
  margin: 5px 0 0;
  max-width: 360px;
  color: var(--text-secondary);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chart-body {
  flex: 1;
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.chart {
  width: 100%;
  height: 320px;
}
.chart-caption {
  min-height: 22px;
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 22px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.trend-category-switch {
  min-height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  margin-top: 2px;
}
.trend-category-dot {
  width: 13px;
  height: 13px;
  padding: 0;
  border: 2px solid #fff;
  border-radius: 50%;
  box-shadow: 0 0 0 1px #d9e1ec;
  cursor: pointer;
  transition: transform .16s ease, box-shadow .16s ease;
}
.trend-category-dot:hover {
  transform: scale(1.16);
}
.trend-category-dot.active {
  transform: scale(1.18);
  box-shadow: 0 0 0 2px var(--color-primary), 0 3px 8px rgba(24, 144, 255, .22);
}
.chart-legend {
  min-height: 34px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px 12px;
  color: var(--text-secondary);
  font-size: 12px;
  flex-wrap: wrap;
  overflow: hidden;
}
.legend-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  vertical-align: middle;
}
.legend-item {
  line-height: 22px;
  white-space: nowrap;
}
.legend-item .legend-dot {
  margin-right: 6px;
}
.table-panel {
  min-width: 0;
  padding: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
}
.table-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.table-title span {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
}
.table-title small {
  color: var(--text-secondary);
  font-size: 12px;
  white-space: nowrap;
}
.table-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.table-search {
  width: 260px;
}
.table-filter {
  width: 142px;
}
.table-scroll {
  width: 100%;
}
.table-scroll ::v-deep .el-table::before {
  display: none;
}
.rate-cell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 42px;
  gap: 8px;
  align-items: center;
}
.rate-cell span {
  color: var(--text-secondary);
  font-size: 12px;
  text-align: right;
}
.count {
  font-weight: 700;
}
.count.red { color: var(--color-danger); }
.count.orange { color: var(--color-warning); }
.count.yellow { color: var(--color-yellow); }
::v-deep .el-table__body tr {
  cursor: pointer;
}
::v-deep .el-table__body tr.project-row-selected > td {
  background: #eef6ff !important;
}
::v-deep .el-table__body tr.project-row-selected > td:first-child {
  box-shadow: inset 3px 0 0 var(--color-primary);
}
::v-deep .el-table__body tr.project-row-selected:hover > td {
  background: #e6f1ff !important;
}
@media (max-width: 1180px) {
  .summary-row { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .health-row { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .chart-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .dashboard-shell { padding: 16px; }
  .page-heading { flex-direction: column; align-items: stretch; }
  .summary-row { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
  .health-row { grid-template-columns: 1fr; }
  .chart-header { flex-direction: column; }
  .chart-header p {
    max-width: 100%;
  }
  .chart-legend {
    height: auto;
    overflow: visible;
  }
  .table-panel { padding: 12px; }
  .table-title {
    display: block;
  }
  .table-title small {
    display: block;
    margin-top: 4px;
    white-space: normal;
  }
  .table-toolbar {
    align-items: stretch;
    flex-direction: column;
  }
  .table-search,
  .table-filter {
    width: 100%;
  }
}
</style>
