<template>
  <div class="app-container cost-dashboard-page">
    <div class="dashboard-shell">
      <div class="page-heading">
        <div>
          <h2>成本总览</h2>
          <p>实时查看项目资金状态，点击项目行可进入项目详情。</p>
        </div>
        <el-button size="small" icon="el-icon-refresh" @click="refreshData">刷新</el-button>
      </div>

      <div class="metric-grid">
        <section v-for="item in metricCards" :key="item.key" class="metric-item">
          <span class="metric-label">{{ item.label }}</span>
          <strong :class="['metric-value', item.className]">{{ item.value }}</strong>
          <small>{{ item.sub }}</small>
        </section>
      </div>

      <div class="tab-bar">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          type="button"
          :class="['tab-item', { active: query.status === tab.value }]"
          @click="handleStatusChange(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          class="search-input"
          size="small"
          clearable
          prefix-icon="el-icon-search"
          placeholder="搜索项目名称 / 项目编号"
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button size="small" type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <span class="total-text">共 {{ total }} 个项目</span>
      </div>

      <el-table
        v-loading="loading"
        :data="rows"
        border
        stripe
        size="small"
        row-key="projId"
        class="project-table"
        @row-click="openProjectDetail"
      >
        <el-table-column prop="projNo" label="项目编号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="projName" label="项目名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="96" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="合同金额" width="130" align="right">
          <template slot-scope="scope">{{ formatMoney(scope.row.contractAmount) }}</template>
        </el-table-column>
        <el-table-column label="预算总额" width="130" align="right">
          <template slot-scope="scope">{{ formatMoney(scope.row.totalBudget) }}</template>
        </el-table-column>
        <el-table-column label="已执行成本" width="130" align="right">
          <template slot-scope="scope">{{ formatMoney(scope.row.actualCost) }}</template>
        </el-table-column>
        <el-table-column label="预算余额" width="130" align="right">
          <template slot-scope="scope">
            <span :class="{ danger: Number(scope.row.budgetBalance || 0) < Number(scope.row.totalBudget || 0) * 0.1 }">
              {{ formatMoney(scope.row.budgetBalance) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="利润空间" width="130" align="right">
          <template slot-scope="scope">
            <span :class="Number(scope.row.profitMargin || 0) >= 0 ? 'profit-positive' : 'profit-negative'">
              {{ formatMoney(scope.row.profitMargin) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="本月入账" width="130" align="right">
          <template slot-scope="scope">{{ formatMoney(scope.row.monthPostingAmount) }}</template>
        </el-table-column>
        <el-table-column label="执行率" width="180" align="center">
          <template slot-scope="scope">
            <div class="rate-cell">
              <el-progress :percentage="safeRate(scope.row.execRate)" :stroke-width="6" :show-text="false" :color="rateColor(scope.row.execRate)" />
              <span>{{ Number(scope.row.execRate || 0).toFixed(2) }}%</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="query.pageNum"
        :limit.sync="query.pageSize"
        @pagination="loadList"
      />
    </div>
  </div>
</template>

<script>
import { getCostDashboardSummary, listCostDashboardProjects } from '@/api/project/costDashboard'
import { formatMoney, projectStatusLabel, projectStatusTagType } from '@/utils/project'

export default {
  name: 'CostDashboard',
  data() {
    return {
      loading: false,
      summary: {},
      rows: [],
      total: 0,
      query: {
        status: '',
        keyword: '',
        pageNum: 1,
        pageSize: 10
      },
      tabs: [
        { value: '', label: '全部' },
        { value: '2,4', label: '进行中' },
        { value: '5', label: '已完工' }
      ]
    }
  },
  computed: {
    metricCards() {
      const mom = Number(this.summary.momRate || 0)
      return [
        { key: 'budget', label: '预算总额', value: this.money(this.summary.totalBudget), sub: '当前范围项目预算' },
        { key: 'actual', label: '已执行成本', value: this.money(this.summary.actualCost), sub: '已审批入账成本' },
        { key: 'balance', label: '预算余额', value: this.money(this.summary.budgetBalance), sub: '预算扣减后余额', className: Number(this.summary.budgetBalance || 0) < 0 ? 'danger' : '' },
        { key: 'contract', label: '合同金额', value: this.money(this.summary.contractAmount), sub: '项目合同金额合计' },
        { key: 'profit', label: '利润空间', value: this.money(this.summary.profitMargin), sub: '合同金额 - 预算总额', className: Number(this.summary.profitMargin || 0) >= 0 ? 'profit-positive' : 'profit-negative' },
        { key: 'month', label: '本月入账', value: this.money(this.summary.monthPostingAmount), sub: '环比 ' + (mom > 0 ? '+' : '') + mom.toFixed(2) + '%', className: mom < 0 ? 'danger' : 'profit-positive' }
      ]
    }
  },
  created() {
    this.refreshData()
  },
  methods: {
    formatMoney,
    money(value) {
      return '¥' + formatMoney(value)
    },
    refreshData() {
      this.loadSummary()
      this.loadList()
    },
    loadSummary() {
      getCostDashboardSummary({ status: this.query.status }).then(res => {
        this.summary = res.data || {}
      }).catch(() => {
        this.$message.error('成本总览指标加载失败')
      })
    },
    loadList() {
      this.loading = true
      listCostDashboardProjects(this.query).then(res => {
        this.rows = res.rows || []
        this.total = res.total || 0
      }).catch(() => {
        this.$message.error('项目资金列表加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    handleStatusChange(status) {
      if (this.query.status === status) return
      this.query.status = status
      this.query.pageNum = 1
      this.refreshData()
    },
    handleSearch() {
      this.query.pageNum = 1
      this.loadList()
    },
    openProjectDetail(row) {
      if (row && row.projId) {
        this.$router.push('/project/projInfo/detail/' + row.projId)
      }
    },
    safeRate(value) {
      return Math.max(0, Math.min(100, Number(value || 0)))
    },
    rateColor(value) {
      const rate = Number(value || 0)
      if (rate >= 100) return '#dc2626'
      if (rate >= 90) return '#f97316'
      if (rate >= 80) return '#ca8a04'
      return '#16a34a'
    },
    statusLabel(status) {
      return projectStatusLabel(status)
    },
    statusTag(status) {
      return projectStatusTagType(status)
    }
  }
}
</script>

<style scoped>
.cost-dashboard-page {
  min-height: calc(100vh - 84px);
  background: #f5f7fa;
  color: #1f2d3d;
}
.dashboard-shell {
  max-width: 1440px;
  margin: 0 auto;
  padding: 28px 40px;
}
.page-heading {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}
.page-heading h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0;
}
.page-heading p {
  margin: 0;
  color: #8c98a8;
  font-size: 13px;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 22px;
}
.metric-item {
  min-width: 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #e6ebf2;
}
.metric-label,
.metric-item small {
  display: block;
  color: #8c98a8;
  font-size: 12px;
}
.metric-value {
  display: block;
  margin: 7px 0 4px;
  color: #1f2d3d;
  font-size: 21px;
  line-height: 1.2;
  white-space: nowrap;
}
.profit-positive { color: #16a34a; }
.profit-negative,
.danger { color: #dc2626; }
.tab-bar {
  display: flex;
  gap: 22px;
  border-bottom: 1px solid #e6ebf2;
  margin-bottom: 14px;
}
.tab-item {
  height: 38px;
  padding: 0;
  color: #8c98a8;
  background: transparent;
  border: 0;
  border-bottom: 2px solid transparent;
  cursor: pointer;
}
.tab-item.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
  font-weight: 600;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.search-input {
  width: 280px;
}
.total-text {
  margin-left: auto;
  color: #8c98a8;
  font-size: 13px;
}
.project-table ::v-deep .el-table__body tr {
  cursor: pointer;
}
.rate-cell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 56px;
  gap: 8px;
  align-items: center;
}
.rate-cell span {
  color: #8c98a8;
  text-align: right;
}
@media (max-width: 1180px) {
  .metric-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 768px) {
  .dashboard-shell { padding: 16px; }
  .page-heading { flex-direction: column; }
  .metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .toolbar { align-items: stretch; flex-direction: column; }
  .search-input { width: 100%; }
  .total-text { margin-left: 0; }
}
</style>
