<template>
  <div class="app-container cost-approval-page">
    <div class="page-heading">
      <div>
        <h2>成本审批</h2>
        <p>统一处理工时单与报销单，审批通过后自动入账并扣减预算余额</p>
      </div>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :span="6"><div class="summary-item"><span>待审批</span><strong>{{ summary.pendingCount || 0 }}</strong></div></el-col>
      <el-col :span="6"><div class="summary-item"><span>待入账</span><strong>{{ summary.approvedCount || 0 }}</strong></div></el-col>
      <el-col :span="6"><div class="summary-item"><span>已入账</span><strong>{{ summary.postedCount || 0 }}</strong></div></el-col>
      <el-col :span="6"><div class="summary-item"><span>待审金额</span><strong>{{ formatMoney(summary.pendingAmount) }}</strong></div></el-col>
    </el-row>

    <div class="filter-panel">
      <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" label-width="76px">
        <el-form-item label="单据编号" prop="billNo">
          <el-input v-model="queryParams.billNo" clearable placeholder="请输入编号" @keyup.enter.native="handleQuery" @clear="handleQuery" />
        </el-form-item>
        <el-form-item label="单据类型" prop="billType">
          <el-select v-model="queryParams.billType" clearable placeholder="全部类型" class="filter-status" @change="handleQuery">
            <el-option label="工时单" value="WORK_HOUR" />
            <el-option label="报销单" value="REIMBURSEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属项目" prop="projId">
          <el-select v-model="queryParams.projId" clearable filterable placeholder="全部项目" class="filter-project" @change="handleQuery">
            <el-option v-for="item in projects" :key="item.projId" :label="item.projName" :value="item.projId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" class="filter-status" @change="handleQuery">
            <el-option label="审批中" value="1" />
            <el-option label="已通过" value="2" />
            <el-option label="已入账" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker v-model="submitRange" type="daterange" value-format="yyyy-MM-dd" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="handleQuery" />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <div class="table-toolbar">
        <div><span class="toolbar-title">成本单据</span><span class="toolbar-count">共 {{ total }} 条</span></div>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="单据类型" width="100" align="center">
          <template slot-scope="scope"><el-tag size="small" :type="billTypeTag(scope.row.billType)">{{ billTypeLabel(scope.row.billType) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="billNo" label="单据编号" width="170" align="center" />
        <el-table-column prop="projName" label="项目名称" min-width="160" align="center" show-overflow-tooltip />
        <el-table-column prop="nodeName" label="WBS节点" min-width="140" align="center" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.nodeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="categoryName" label="成本科目" min-width="120" align="center">
          <template slot-scope="scope">{{ scope.row.categoryName || '-' }}</template>
        </el-table-column>
        <el-table-column label="金额/工时" width="130" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.billType === 'WORK_HOUR'">{{ formatHours(scope.row.workHours) }}</span>
            <span v-else>{{ formatMoney(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="入账金额" width="130" align="center"><template slot-scope="scope">{{ formatMoney(scope.row.amount) }}</template></el-table-column>
        <el-table-column prop="applicant" label="申请人" width="110" align="center" />
        <el-table-column prop="submitTime" label="提交时间" width="160" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope"><el-tag size="small" :type="statusTag(scope.row)">{{ statusLabel(scope.row) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
            <template v-if="scope.row.status === '1'">
              <el-button type="text" icon="el-icon-check" :loading="submitting" :disabled="submitting" @click="handleApprove(scope.row)" v-hasPermi="['cost:approval:approve']">通过入账</el-button>
              <el-button type="text" icon="el-icon-close" class="danger-action" :loading="submitting" :disabled="submitting" @click="handleReject(scope.row)" v-hasPermi="['cost:approval:approve']">驳回</el-button>
            </template>
            <el-button v-if="scope.row.status === '2'" type="text" icon="el-icon-finished" :loading="submitting" :disabled="submitting" @click="handlePost(scope.row)" v-hasPermi="['cost:approval:post']">入账</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="暂无待处理成本单据" />
      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="load" />
    </div>
  </div>
</template>

<script>
import { approveAndPostBill, getCostApprovalSummary, listCostApprovalBills, postApprovedBill, rejectBill } from '@/api/project/costApproval'
import { listProjInfos } from '@/api/project/projInfo'
import { formatHours, formatMoney, workHourStatusLabel, workHourStatusTagType } from '@/utils/project'

export default {
  name: 'CostApproval',
  data() {
    return {
      loading: false,
      submitting: false,
      list: [],
      total: 0,
      projects: [],
      submitRange: [],
      summary: {},
      queryParams: { pageNum: 1, pageSize: 10, billNo: undefined, billType: undefined, projId: undefined, status: '1', beginSubmitTime: undefined, endSubmitTime: undefined }
    }
  },
  created() {
    this.loadProjects()
    this.loadSummary()
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      const params = Object.assign({}, this.queryParams)
      if (this.submitRange && this.submitRange.length === 2) {
        params.beginSubmitTime = this.submitRange[0]
        params.endSubmitTime = this.submitRange[1]
      }
      listCostApprovalBills(params).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
      }).catch(() => this.$message.error('成本单据加载失败')).finally(() => { this.loading = false })
    },
    loadSummary() {
      getCostApprovalSummary().then(res => {
        this.summary = res.data || {}
      }).catch(() => this.$message.error('审批统计加载失败'))
    },
    loadProjects() {
      listProjInfos({ pageNum: 1, pageSize: 500 }).then(res => {
        this.projects = res.rows || []
      }).catch(() => this.$message.error('项目列表加载失败'))
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.load()
      this.loadSummary()
    },
    resetQuery() {
      this.submitRange = []
      this.resetForm('queryForm')
      this.queryParams.status = '1'
      this.handleQuery()
    },
    handleDetail(row) {
      if (row.billType === 'WORK_HOUR') {
        this.$router.push('/cost/workHour/detail/' + row.billId)
      } else {
        this.$router.push('/cost/reimbursement/detail/' + row.billId)
      }
    },
    handleApprove(row) {
      if (this.submitting) return
      this.$modal.confirm('确认审批通过并入账单据 ' + row.billNo + ' 吗？').then(() => {
        this.submitting = true
        approveAndPostBill(row.billType, row.billId).then(() => {
          this.$message.success('审批入账成功')
          this.handleQuery()
        }).catch(() => this.$message.error('审批入账失败')).finally(() => { this.submitting = false })
      }).catch(() => {})
    },
    handleReject(row) {
      if (this.submitting) return
      this.submitting = true
      this.$prompt('请输入驳回原因', '驳回成本单据', {
        inputType: 'textarea',
        inputValidator: value => !!value || '请填写驳回原因'
      }).then(({ value }) => {
        rejectBill(row.billType, row.billId, { remark: value }).then(() => {
          this.$message.success('已驳回')
          this.handleQuery()
        }).catch(() => this.$message.error('驳回失败'))
      }).catch(error => {
        if (error !== 'cancel') {
          this.$message.error('驳回失败')
        }
      }).finally(() => {
        this.submitting = false
      })
    },
    handlePost(row) {
      if (this.submitting) return
      this.$modal.confirm('确认入账单据 ' + row.billNo + ' 吗？').then(() => {
        this.submitting = true
        postApprovedBill(row.billType, row.billId).then(() => {
          this.$message.success('入账成功')
          this.handleQuery()
        }).catch(() => this.$message.error('入账失败')).finally(() => { this.submitting = false })
      }).catch(() => {})
    },
    billTypeLabel(type) {
      const map = { WORK_HOUR: '工时单', REIMBURSEMENT: '报销单' }
      return map[type] || type
    },
    billTypeTag(type) {
      const map = { WORK_HOUR: 'primary', REIMBURSEMENT: 'success' }
      return map[type] || 'info'
    },
    statusLabel(row) { return workHourStatusLabel(row.status) },
    statusTag(row) { return workHourStatusTagType(row.status) },
    formatMoney,
    formatHours
  }
}
</script>

<style scoped>
.cost-approval-page {
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

.filter-project {
  width: 190px;
}

.filter-status {
  width: 130px;
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
