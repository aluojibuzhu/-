<template>
  <div class="app-container cost-report-page">
    <div class="report-shell">
      <div class="page-heading">
        <div>
          <h2>报表导出</h2>
          <p>按项目、科目、WBS节点和月份组合生成成本入账报表。</p>
        </div>
      </div>

      <section class="filter-panel">
        <el-form ref="form" :model="form" label-width="86px" size="small">
          <div class="filter-grid">
            <el-form-item label="时间范围" required class="date-item">
              <el-radio-group v-model="form.dateType" class="date-tabs" @change="applyDateType">
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="quarter">本季</el-radio-button>
                <el-radio-button label="year">本年</el-radio-button>
                <el-radio-button label="custom">自定义</el-radio-button>
              </el-radio-group>
              <el-date-picker
                v-model="form.dateRange"
                class="date-range"
                type="daterange"
                value-format="yyyy-MM-dd"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :disabled="form.dateType !== 'custom'"
              />
            </el-form-item>

            <el-form-item label="项目">
              <el-select v-model="form.projIds" multiple clearable filterable collapse-tags placeholder="全部项目">
                <el-option v-for="item in options.projects" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="成本科目">
              <el-select v-model="form.categoryIds" multiple clearable filterable collapse-tags placeholder="全部一级科目">
                <el-option v-for="item in options.categories" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="WBS节点">
              <el-select v-model="form.nodeIds" multiple clearable filterable collapse-tags placeholder="全部节点">
                <el-option v-for="item in filteredNodeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="审批人">
              <el-select v-model="form.approvers" multiple clearable filterable collapse-tags placeholder="全部审批人">
                <el-option v-for="item in options.approvers" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="分组方式" required class="group-item">
              <el-checkbox-group v-model="form.groupBy" class="group-box">
                <el-checkbox label="project">项目</el-checkbox>
                <el-checkbox label="category">科目</el-checkbox>
                <el-checkbox label="node">节点</el-checkbox>
                <el-checkbox label="month">月份</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <div class="action-row">
              <el-button type="primary" icon="el-icon-view" :loading="previewLoading" @click="handlePreview">预览</el-button>
              <el-button icon="el-icon-download" :loading="exportLoading" @click="handleExport">导出Excel</el-button>
            </div>
          </div>
        </el-form>
      </section>

      <section class="preview-panel">
        <div class="preview-title">
          <strong>报表预览</strong>
          <span>最多展示前 100 行，导出包含完整结果。</span>
        </div>
        <el-table
          v-loading="previewLoading"
          :data="previewRows"
          border
          stripe
          size="small"
          max-height="560"
          empty-text="请设置条件后点击预览"
        >
          <el-table-column
            v-for="col in tableColumns"
            :key="col.prop"
            :prop="col.prop"
            :label="col.label"
            :min-width="col.width"
            :align="col.align || 'left'"
            show-overflow-tooltip
          >
            <template slot-scope="scope">
              <span v-if="col.money">{{ formatMoney(scope.row[col.prop]) }}</span>
              <span v-else>{{ scope.row[col.prop] || '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<script>
import { saveAs } from 'file-saver'
import { exportCostReport, getCostReportOptions, previewCostReport } from '@/api/project/costReport'
import { formatMoney } from '@/utils/project'

export default {
  name: 'CostReport',
  data() {
    return {
      previewLoading: false,
      exportLoading: false,
      previewRows: [],
      options: {
        projects: [],
        categories: [],
        nodes: [],
        approvers: []
      },
      form: {
        dateType: 'month',
        dateRange: [],
        projIds: [],
        categoryIds: [],
        nodeIds: [],
        approvers: [],
        groupBy: ['project', 'category']
      }
    }
  },
  computed: {
    filteredNodeOptions() {
      if (!this.form.projIds.length) {
        return this.options.nodes
      }
      const selected = this.form.projIds.map(item => String(item))
      return this.options.nodes.filter(item => selected.includes(String(item.projId)))
    },
    tableColumns() {
      const columns = []
      if (this.form.groupBy.includes('project')) {
        columns.push({ prop: 'projNo', label: '项目编号', width: 130 })
        columns.push({ prop: 'projName', label: '项目名称', width: 160 })
      }
      if (this.form.groupBy.includes('category')) {
        columns.push({ prop: 'categoryName', label: '成本科目', width: 120 })
      }
      if (this.form.groupBy.includes('node')) {
        columns.push({ prop: 'nodeName', label: 'WBS节点', width: 140 })
      }
      if (this.form.groupBy.includes('month')) {
        columns.push({ prop: 'postingMonth', label: '入账月份', width: 100, align: 'center' })
      }
      columns.push({ prop: 'recordCount', label: '单据数', width: 90, align: 'center' })
      columns.push({ prop: 'amount', label: '入账金额', width: 120, align: 'right', money: true })
      return columns
    }
  },
  created() {
    this.applyDateType()
    this.loadOptions()
  },
  methods: {
    formatMoney,
    loadOptions() {
      getCostReportOptions().then(res => {
        this.options = res.data || this.options
      }).catch(() => {
        this.$message.error('报表筛选项加载失败')
      })
    },
    applyDateType() {
      const now = new Date()
      let start = new Date(now.getFullYear(), now.getMonth(), 1)
      let end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      if (this.form.dateType === 'quarter') {
        const quarterStartMonth = Math.floor(now.getMonth() / 3) * 3
        start = new Date(now.getFullYear(), quarterStartMonth, 1)
        end = new Date(now.getFullYear(), quarterStartMonth + 3, 0)
      } else if (this.form.dateType === 'year') {
        start = new Date(now.getFullYear(), 0, 1)
        end = new Date(now.getFullYear(), 11, 31)
      } else if (this.form.dateType === 'custom') {
        return
      }
      this.form.dateRange = [this.formatDate(start), this.formatDate(end)]
    },
    buildPayload() {
      if (!this.form.dateRange || this.form.dateRange.length !== 2) {
        this.$message.warning('请选择报表时间范围')
        return null
      }
      if (!this.form.groupBy.length) {
        this.$message.warning('请至少选择一种分组方式')
        return null
      }
      return {
        beginDate: this.form.dateRange[0],
        endDate: this.form.dateRange[1],
        projIds: this.form.projIds,
        categoryIds: this.form.categoryIds,
        nodeIds: this.form.nodeIds,
        approvers: this.form.approvers,
        groupBy: this.form.groupBy
      }
    },
    handlePreview() {
      const payload = this.buildPayload()
      if (!payload) return
      this.previewLoading = true
      previewCostReport(payload).then(res => {
        this.previewRows = res.data || []
      }).catch(() => {
        this.$message.error('报表预览失败')
      }).finally(() => {
        this.previewLoading = false
      })
    },
    handleExport() {
      const payload = this.buildPayload()
      if (!payload) return
      this.exportLoading = true
      exportCostReport(payload).then(res => {
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        saveAs(blob, '成本报表.xlsx')
      }).catch(() => {
        this.$message.error('报表导出失败')
      }).finally(() => {
        this.exportLoading = false
      })
    },
    formatDate(date) {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return y + '-' + m + '-' + d
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
  padding: 28px 40px;
}
.page-heading {
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
.filter-panel,
.preview-panel {
  padding: 18px;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
}
.filter-panel {
  margin-bottom: 16px;
}
.filter-grid {
  display: grid;
  grid-template-columns: minmax(360px, 1.25fr) repeat(3, minmax(220px, 1fr));
  column-gap: 18px;
  row-gap: 4px;
  align-items: start;
}
.filter-panel ::v-deep .el-select,
.filter-panel ::v-deep .el-date-editor {
  width: 100%;
}
.date-tabs {
  margin-bottom: 10px;
}
.date-range {
  display: block;
}
.date-item {
  grid-row: span 2;
}
.group-box {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  min-height: 32px;
  align-items: center;
}
.action-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  justify-content: flex-end;
}
.preview-title {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}
.preview-title strong {
  font-size: 16px;
  color: #1f2d3d;
}
.preview-title span {
  color: #8c98a8;
  font-size: 12px;
}
@media (max-width: 992px) {
  .report-shell { padding: 16px; }
  .filter-grid { grid-template-columns: 1fr; }
  .date-item { grid-row: auto; }
  .action-row { justify-content: flex-start; }
}
</style>
