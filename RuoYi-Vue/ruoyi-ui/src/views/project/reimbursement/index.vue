<template>
  <div class="app-container reimbursement-page">
    <div class="page-heading">
      <div>
        <h2>报销申请</h2>
        <p>登记项目费用，提交审批后进入成本入账流程</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['cost:reimbursement:add']">新建报销</el-button>
    </div>

    <div class="filter-panel">
      <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="76px">
        <el-form-item label="报销编号" prop="reimburseNo"><el-input v-model="queryParams.reimburseNo" clearable placeholder="请输入编号" @keyup.enter.native="handleQuery" /></el-form-item>
        <el-form-item label="所属项目" prop="projId">
          <el-select v-model="queryParams.projId" clearable filterable placeholder="全部项目" @change="handleQueryProjectChange">
            <el-option v-for="item in projects" :key="item.projId" :label="item.projName" :value="item.projId" />
          </el-select>
        </el-form-item>
        <el-form-item label="WBS节点" prop="nodeId">
          <el-select v-model="queryParams.nodeId" clearable filterable placeholder="全部节点">
            <el-option v-for="item in nodes" :key="item.nodeId" :label="item.nodeName" :value="item.nodeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型" prop="expenseType">
          <el-select v-model="queryParams.expenseType" clearable placeholder="全部类型">
            <el-option v-for="dict in dict.type.exp_expense_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="发生日期">
          <el-date-picker v-model="dateRange" value-format="yyyy-MM-dd" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <div class="table-toolbar"><span class="toolbar-title">报销列表</span><span class="toolbar-count">共 {{ total }} 条</span></div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="reimburseNo" label="报销编号" width="170" align="center" />
        <el-table-column prop="projName" label="项目名称" min-width="160" align="center" show-overflow-tooltip />
        <el-table-column prop="nodeName" label="WBS节点" min-width="150" align="center" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="成本科目" min-width="130" align="center" />
        <el-table-column label="费用类型" width="110" align="center"><template slot-scope="scope"><dict-tag :options="dict.type.exp_expense_type" :value="scope.row.expenseType" /></template></el-table-column>
        <el-table-column prop="expenseDate" label="发生日期" width="120" align="center" />
        <el-table-column label="报销金额" width="130" align="center"><template slot-scope="scope">{{ formatMoney(scope.row.amount) }}</template></el-table-column>
        <el-table-column label="状态" width="100" align="center"><template slot-scope="scope"><el-tag size="small" :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
            <el-button v-if="canEdit(scope.row.status)" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['cost:reimbursement:edit']">编辑</el-button>
            <el-button v-if="canEdit(scope.row.status)" type="text" icon="el-icon-upload2" @click="handleSubmit(scope.row)" v-hasPermi="['cost:reimbursement:edit']">提交</el-button>
            <el-button v-if="canEdit(scope.row.status)" type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(scope.row)" v-hasPermi="['cost:reimbursement:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && list.length === 0" description="暂无报销申请" />
      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script>
import { delReimbursement, listReimbursementWbsNodes, listReimbursements, submitReimbursement } from '@/api/project/reimbursement'
import { listProjInfos } from '@/api/project/projInfo'
import { canEditReimbursement, formatMoney, REIMBURSEMENT_STATUS_OPTIONS, reimbursementStatusLabel, reimbursementStatusTagType } from '@/utils/project'

export default {
  name: 'Reimbursement',
  dicts: ['exp_expense_type'],
  data() {
    return { loading: false, list: [], total: 0, projects: [], nodes: [], dateRange: [], statusOptions: REIMBURSEMENT_STATUS_OPTIONS, queryParams: { pageNum: 1, pageSize: 10 } }
  },
  created() { this.loadProjects(); this.getList() },
  methods: {
    getList() {
      this.loading = true
      const query = Object.assign({}, this.queryParams, { beginExpenseDate: this.dateRange && this.dateRange[0], endExpenseDate: this.dateRange && this.dateRange[1] })
      listReimbursements(query).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
      }).catch(() => this.$message.error('报销列表加载失败')).finally(() => {
        this.loading = false
      })
    },
    loadProjects() {
      return Promise.all([listProjInfos({ pageNum: 1, pageSize: 100, status: '2' }), listProjInfos({ pageNum: 1, pageSize: 100, status: '4' })])
        .then(([approved, running]) => { this.projects = [].concat(approved.rows || [], running.rows || []) })
    },
    handleQueryProjectChange(value) { this.queryParams.nodeId = undefined; this.nodes = []; if (value) listReimbursementWbsNodes(value).then(res => { this.nodes = res.data || [] }) },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.dateRange = []; this.nodes = []; this.resetForm('queryForm'); this.handleQuery() },
    handleAdd() { this.$router.push('/cost/reimbursement/form') },
    handleUpdate(row) { this.$router.push({ path: '/cost/reimbursement/form', query: { id: row.reimburseId } }) },
    handleDetail(row) { this.$router.push('/cost/reimbursement/detail/' + row.reimburseId) },
    handleSubmit(row) {
      submitReimbursement(row.reimburseId).then(() => {
        this.$message.success('提交成功')
        this.getList()
      }).catch(() => this.$message.error('提交失败'))
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除报销单 ' + row.reimburseNo + ' 吗？').then(() => {
        delReimbursement(row.reimburseId).then(() => {
          this.$message.success('删除成功')
          this.getList()
        }).catch(() => this.$message.error('删除失败'))
      }).catch(() => {})
    },
    canEdit(status) { return canEditReimbursement(status) },
    statusLabel(status) { return reimbursementStatusLabel(status) },
    statusTag(status) { return reimbursementStatusTagType(status) },
    formatMoney
  }
}
</script>

<style scoped>
.reimbursement-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-heading,
.filter-panel,
.table-panel {
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
