<template>
  <div class="app-container cost-report-page">
    <div class="report-shell">
      <div class="page-heading">
        <div>
          <h2>{{ title }}</h2>
          <p>{{ desc }}</p>
        </div>
        <el-button size="small" icon="el-icon-download" :loading="exportLoading" @click="handleExport">导出Excel</el-button>
      </div>

      <div class="filter-panel">
        <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="76px">
          <el-form-item v-if="filters.includes('project')" label="项目范围" prop="projIds">
            <el-select v-model="queryParams.projIds" multiple clearable filterable collapse-tags placeholder="全部项目" class="filter-control filter-control-wide">
              <el-option v-for="item in options.projects" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item v-if="filters.includes('category')" label="成本科目" prop="categoryIds">
            <el-select v-model="queryParams.categoryIds" multiple clearable filterable collapse-tags placeholder="全部一级科目" class="filter-control">
              <el-option v-for="item in options.categories" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item v-if="filters.includes('node')" label="WBS节点" prop="nodeIds">
            <el-select v-model="queryParams.nodeIds" multiple clearable filterable collapse-tags placeholder="全部节点" class="filter-control">
              <el-option v-for="item in filteredNodes" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item v-if="filters.includes('billType')" label="单据类型" prop="billType">
            <el-select v-model="queryParams.billType" clearable placeholder="全部类型" class="filter-control-small">
              <el-option label="工时" value="WORK_HOUR" />
              <el-option label="报销" value="REIMBURSEMENT" />
            </el-select>
          </el-form-item>

          <el-form-item v-if="filters.includes('approver')" label="审批人" prop="approvers">
            <el-select v-model="queryParams.approvers" multiple clearable filterable collapse-tags placeholder="全部审批人" class="filter-control-small">
              <el-option v-for="item in options.approvers" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="入账时间">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              value-format="yyyy-MM-dd"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              class="filter-date"
            />
          </el-form-item>

          <el-form-item class="filter-actions">
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-panel">
        <div class="table-toolbar">
          <span class="toolbar-title">{{ title }}</span>
          <span class="toolbar-count">共 {{ total }} 条</span>
        </div>
        <el-table v-loading="loading" :data="rows" border stripe size="small">
          <el-table-column
            v-for="col in columns"
            :key="col.prop"
            :prop="col.prop"
            :label="col.label"
            :min-width="col.width"
            :align="col.align || 'left'"
            show-overflow-tooltip
          >
            <template slot-scope="scope">
              <span v-if="col.money" :class="moneyClass(col, scope.row[col.prop])">{{ formatMoney(scope.row[col.prop]) }}</span>
              <span v-else-if="col.rate">{{ Number(scope.row[col.prop] || 0).toFixed(2) }}%</span>
              <span v-else>{{ scope.row[col.prop] || '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="loadList" />
      </div>
    </div>
  </div>
</template>

<script>
import { saveAs } from 'file-saver'
import { exportTypedCostReport, getCostReportOptions, listCostReport } from '@/api/project/costReport'
import { formatMoney } from '@/utils/project'

export default {
  name: 'CostReportPage',
  props: {
    type: { type: String, required: true },
    title: { type: String, required: true },
    desc: { type: String, default: '' },
    filters: { type: Array, default: () => [] },
    columns: { type: Array, required: true },
    filterHint: { type: String, default: '' },
    filterColumns: { type: Number, default: 4 }
  },
  data() {
    return {
      loading: false,
      exportLoading: false,
      rows: [],
      total: 0,
      dateRange: [],
      options: {
        projects: [],
        categories: [],
        nodes: [],
        approvers: []
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projIds: [],
        categoryIds: [],
        nodeIds: [],
        approvers: [],
        billType: undefined
      }
    }
  },
  computed: {
    filteredNodes() {
      if (!this.queryParams.projIds || !this.queryParams.projIds.length) {
        return this.options.nodes
      }
      const selected = this.queryParams.projIds.map(item => String(item))
      return this.options.nodes.filter(item => selected.includes(String(item.projId)))
    }
  },
  watch: {
    'queryParams.projIds'() {
      if (!this.queryParams.nodeIds.length) return
      const validNodeIds = this.filteredNodes.map(item => String(item.value))
      this.queryParams.nodeIds = this.queryParams.nodeIds.filter(id => validNodeIds.includes(String(id)))
    }
  },
  created() {
    this.resetDateRange()
    this.loadOptions()
    this.loadList()
  },
  methods: {
    formatMoney,
    loadOptions() {
      getCostReportOptions().then(res => {
        this.options = res.data || this.options
      })
    },
    loadList() {
      this.loading = true
      listCostReport(this.type, this.buildQuery()).then(res => {
        this.rows = res.rows || []
        this.total = res.total || 0
      }).catch(() => {
        this.$message.error(this.title + '加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.loadList()
    },
    resetQuery() {
      const pageSize = this.queryParams.pageSize
      this.queryParams = {
        pageNum: 1,
        pageSize,
        projIds: [],
        categoryIds: [],
        nodeIds: [],
        approvers: [],
        billType: undefined
      }
      this.resetDateRange()
      this.loadList()
    },
    handleExport() {
      this.exportLoading = true
      exportTypedCostReport(this.type, this.buildQuery()).then(res => {
        saveAs(new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }), this.title + '.xlsx')
      }).catch(() => {
        this.$message.error(this.title + '导出失败')
      }).finally(() => {
        this.exportLoading = false
      })
    },
    buildQuery() {
      return {
        ...this.queryParams,
        beginDate: this.dateRange && this.dateRange[0],
        endDate: this.dateRange && this.dateRange[1]
      }
    },
    resetDateRange() {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth(), 1)
      const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      this.dateRange = [this.formatDate(start), this.formatDate(end)]
    },
    formatDate(date) {
      return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0')
    },
    moneyClass(col, value) {
      if (col.sign) {
        return Number(value || 0) >= 0 ? 'positive' : 'negative'
      }
      return ''
    }
  }
}
</script>

<style scoped>
.cost-report-page {
  min-height: calc(100vh - 84px);
  background: #f5f7fa;
  color: #1f2d3d;
}
.report-shell {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px 32px;
}
.page-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 16px;
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
.filter-panel,
.table-panel {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
}
.filter-panel {
  margin-bottom: 12px;
  padding: 14px 16px 4px;
}
.filter-panel ::v-deep .el-form-item {
  margin-right: 16px;
  margin-bottom: 10px;
}
.filter-panel ::v-deep .el-form-item__label {
  color: #526173;
  font-weight: 500;
}
.filter-control {
  width: 220px;
}
.filter-control-wide {
  width: 260px;
}
.filter-control-small {
  width: 180px;
}
.filter-date {
  width: 260px;
}
.filter-actions {
  margin-right: 0;
}
.table-panel {
  padding: 16px 18px;
}
.table-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.toolbar-title {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 600;
}
.toolbar-count {
  margin-left: 8px;
  color: #8c98a8;
  font-size: 12px;
}
.positive {
  color: #16a34a;
}
.negative {
  color: #dc2626;
}
@media (max-width: 768px) {
  .report-shell {
    padding: 16px;
  }
  .page-heading {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-panel ::v-deep .el-form-item {
    display: block;
    margin-right: 0;
  }
  .filter-control,
  .filter-control-wide,
  .filter-control-small,
  .filter-date {
    width: 100%;
  }
}
</style>
