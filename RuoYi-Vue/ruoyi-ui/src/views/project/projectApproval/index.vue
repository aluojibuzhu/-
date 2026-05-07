<template>
  <div class="app-container project-approval-page">
    <div class="page-heading">
      <div>
        <h2>立项审批</h2>
        <p>集中处理项目立项申请，审批人查看完整方案后通过或驳回</p>
      </div>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="8" class="summary-col">
        <div class="summary-item"><span>待审批</span><strong>{{ summary.pending }}</strong></div>
      </el-col>
      <el-col :xs="24" :sm="8" class="summary-col">
        <div class="summary-item"><span>预算合计</span><strong>{{ formatMoney(summary.totalBudget) }}</strong></div>
      </el-col>
      <el-col :xs="24" :sm="8" class="summary-col">
        <div class="summary-item"><span>合同金额</span><strong>{{ formatMoney(summary.contractAmount) }}</strong></div>
      </el-col>
    </el-row>

    <div class="filter-panel">
      <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" label-width="76px">
        <el-form-item label="项目名称" prop="projName">
          <el-input v-model="queryParams.projName" clearable placeholder="请输入项目名称" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <div class="table-toolbar">
        <div><span class="toolbar-title">待审批立项</span><span class="toolbar-count">共 {{ total }} 条</span></div>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="projNo" label="项目编号" width="170" align="center" />
        <el-table-column prop="projName" label="项目名称" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户名称" min-width="150" align="center" show-overflow-tooltip />
        <el-table-column prop="contractAmount" label="合同金额" width="130" align="center">
          <template slot-scope="scope">{{ formatMoney(scope.row.contractAmount) }}</template>
        </el-table-column>
        <el-table-column prop="totalBudget" label="预算金额" width="130" align="center">
          <template slot-scope="scope">{{ formatMoney(scope.row.totalBudget) }}</template>
        </el-table-column>
        <el-table-column prop="planStartDate" label="预计开工" width="120" align="center" />
        <el-table-column prop="planEndDate" label="预计竣工" width="120" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope"><el-tag size="small" :type="projectStatusTagType(scope.row.status)">{{ projectStatusLabel(scope.row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
            <el-button type="text" icon="el-icon-check" @click="handleApprove(scope.row)" v-hasPermi="['project:projInfo:approve']">通过</el-button>
            <el-button type="text" icon="el-icon-close" class="danger-action" @click="handleReject(scope.row)" v-hasPermi="['project:projInfo:approve']">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="暂无待审批立项" />
      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="load" />
    </div>
  </div>
</template>

<script>
import { approve, listProjInfos, reject } from '@/api/project/projInfo'
import { formatMoney, projectStatusLabel, projectStatusTagType } from '@/utils/project'

export default {
  name: 'ProjectApproval',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      summary: { pending: 0, totalBudget: 0, contractAmount: 0 },
      queryParams: { pageNum: 1, pageSize: 10, projName: undefined, status: '1' }
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      listProjInfos(this.queryParams).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
        this.loadSummary()
      }).catch(() => this.$message.error('立项审批列表加载失败')).finally(() => {
        this.loading = false
      })
    },
    loadSummary() {
      const params = Object.assign({}, this.queryParams, { pageNum: 1, pageSize: 1000 })
      listProjInfos(params).then(res => {
        const rows = res.rows || []
        this.summary = {
          pending: res.total || 0,
          totalBudget: rows.reduce((sum, item) => sum + Number(item.totalBudget || 0), 0),
          contractAmount: rows.reduce((sum, item) => sum + Number(item.contractAmount || 0), 0)
        }
      }).catch(() => {
        this.summary = { pending: this.total || 0, totalBudget: 0, contractAmount: 0 }
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.load()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.queryParams.status = '1'
      this.handleQuery()
    },
    handleDetail(row) {
      this.$router.push('/project/projInfo/detail/' + row.projId)
    },
    handleApprove(row) {
      this.$modal.confirm('确认审批通过项目立项 ' + row.projNo + ' 吗？').then(() => {
        approve(row.projId).then(() => {
          this.$message.success('审批通过成功')
          this.load()
        }).catch(() => this.$message.error('审批失败'))
      }).catch(() => {})
    },
    handleReject(row) {
      this.$prompt('请输入驳回原因', '驳回项目立项', {
        inputType: 'textarea',
        inputValidator: value => !!value || '请填写驳回原因'
      }).then(({ value }) => {
        reject(row.projId, { rejectReason: value }).then(() => {
          this.$message.success('已驳回')
          this.load()
        }).catch(() => this.$message.error('驳回失败'))
      }).catch(error => {
        if (error !== 'cancel') {
          this.$message.error('驳回失败')
        }
      })
    },
    formatMoney,
    projectStatusLabel,
    projectStatusTagType
  }
}
</script>

<style scoped>
.project-approval-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-heading,
.filter-panel,
.table-panel,
.summary-item {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  margin-bottom: 14px;
}

.page-heading h2 {
  margin: 0;
  color: #1f2d3d;
  font-size: 22px;
  font-weight: 600;
}

.page-heading p {
  margin: 8px 0 0;
  color: #8c98a8;
}

.summary-row {
  margin-bottom: 14px;
}

.summary-col {
  margin-bottom: 12px;
}

.summary-item {
  min-height: 86px;
  padding: 16px 18px;
}

.summary-item span {
  display: block;
  margin-bottom: 10px;
  color: #8c98a8;
  font-size: 13px;
}

.summary-item strong {
  color: #1f2d3d;
  font-size: 24px;
  font-weight: 600;
}

.filter-panel {
  padding: 16px 18px 0;
  margin-bottom: 14px;
}

.table-panel {
  padding: 16px 18px 18px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.toolbar-title {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 600;
}

.toolbar-count {
  margin-left: 10px;
  color: #8c98a8;
  font-size: 13px;
}

.danger-action {
  color: #ff4d4f;
}
</style>
