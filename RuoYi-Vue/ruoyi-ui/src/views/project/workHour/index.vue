<template>
  <div class="app-container work-hour-page">
    <div class="page-heading">
      <div>
        <h2>工时填报</h2>
        <p>维护项目成员工时单，提交后进入审批与入账流程</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['cost:workHour:add']">新建工时</el-button>
    </div>

    <div class="filter-panel">
      <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" label-width="76px">
        <el-form-item label="工时编号" prop="whNo">
          <el-input v-model="queryParams.whNo" clearable placeholder="请输入编号" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="所属项目" prop="projId">
          <el-select v-model="queryParams.projId" clearable filterable placeholder="全部项目" class="filter-project" @change="handleQueryProjectChange">
            <el-option v-for="item in projects" :key="item.projId" :label="item.projName" :value="item.projId" />
          </el-select>
        </el-form-item>
        <el-form-item label="WBS节点" prop="nodeId">
          <el-select v-model="queryParams.nodeId" clearable filterable placeholder="全部节点" class="filter-node">
            <el-option v-for="item in nodes" :key="item.nodeId" :label="item.nodeName" :value="item.nodeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作类型" prop="workType">
          <el-select v-model="queryParams.workType" clearable placeholder="全部类型" class="filter-status">
            <el-option v-for="item in workTypeOptions" :key="item.categoryId" :label="item.categoryName" :value="String(item.categoryId)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" class="filter-status">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="填报日期">
          <el-date-picker v-model="dateRange" type="daterange" value-format="yyyy-MM-dd" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <div class="table-toolbar">
        <div>
          <span class="toolbar-title">工时列表</span>
          <span class="toolbar-count">共 {{ total }} 条</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="whNo" label="工时编号" width="160" align="center" />
        <el-table-column prop="projName" label="项目名称" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column prop="nodeName" label="WBS节点" min-width="150" align="center" show-overflow-tooltip />
        <el-table-column label="工作类型" width="100" align="center">
          <template slot-scope="scope">{{ scope.row.categoryName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="workDate" label="填报日期" width="120" align="center" />
        <el-table-column label="工时数" width="100" align="center">
          <template slot-scope="scope">{{ formatHours(scope.row.workHours) }}</template>
        </el-table-column>
        <el-table-column label="工时成本" width="130" align="center">
          <template slot-scope="scope">{{ formatMoney(scope.row.workCost) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="statusTag(scope.row.status)" :class="statusClass(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
            <el-button v-if="canEdit(scope.row.status)" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['cost:workHour:edit']">编辑</el-button>
            <el-button v-if="canEdit(scope.row.status)" type="text" icon="el-icon-upload2" @click="handleSubmit(scope.row)" v-hasPermi="['cost:workHour:edit']">提交</el-button>
            <el-button v-if="canEdit(scope.row.status)" type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(scope.row)" v-hasPermi="['cost:workHour:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="暂无工时单" />
      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="load" />
    </div>
  </div>
</template>

<script>
import { listCostCategories } from '@/api/project/costCategory'
import { listProjInfos } from '@/api/project/projInfo'
import { delWorkHour, listWorkHours, listWorkHourWbsNodes, submitWorkHour } from '@/api/project/workHour'
import { canEditWorkHour, formatHours, formatMoney, WORK_HOUR_STATUS_OPTIONS, workHourStatusLabel } from '@/utils/project'

export default {
  name: 'WorkHourIndex',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      projects: [],
      nodes: [],
      workTypeOptions: [],
      dateRange: [],
      statusOptions: WORK_HOUR_STATUS_OPTIONS,
      queryParams: { pageNum: 1, pageSize: 10, whNo: undefined, projId: undefined, nodeId: undefined, workType: undefined, status: undefined, beginWorkDate: undefined, endWorkDate: undefined }
    }
  },
  created() {
    this.loadProjects()
    this.loadWorkTypes()
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      const params = Object.assign({}, this.queryParams)
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginWorkDate = this.dateRange[0]
        params.endWorkDate = this.dateRange[1]
      } else {
        params.beginWorkDate = undefined
        params.endWorkDate = undefined
      }
      listWorkHours(params).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
      }).catch(() => this.$message.error('工时列表加载失败')).finally(() => {
        this.loading = false
      })
    },
    loadProjects() {
      Promise.all([
        listProjInfos({ pageNum: 1, pageSize: 100, status: '2' }),
        listProjInfos({ pageNum: 1, pageSize: 100, status: '4' }),
        listProjInfos({ pageNum: 1, pageSize: 100, status: '5' })
      ]).then(([approved, running, completed]) => {
        this.projects = [].concat(approved.rows || [], running.rows || [], completed.rows || [])
      }).catch(() => this.$message.error('项目列表加载失败'))
    },
    loadWorkTypes() {
      listCostCategories({ status: '0' }).then(res => {
        const rows = res.data || []
        const labor = rows.find(item => item.categoryLevel === 1 && Number(item.parentId || 0) === 0 && item.categoryName === '人工费')
        this.workTypeOptions = labor ? rows.filter(item => Number(item.parentId || 0) === Number(labor.categoryId)) : []
      }).catch(() => this.$message.error('工时类型加载失败'))
    },
    handleQueryProjectChange(projId) {
      this.queryParams.nodeId = undefined
      this.nodes = []
      if (projId) {
        listWorkHourWbsNodes(projId).then(res => {
          this.nodes = res.data || []
        }).catch(() => this.$message.error('WBS节点加载失败'))
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.load()
    },
    resetQuery() {
      this.dateRange = []
      this.nodes = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.$router.push('/cost/workHour/form')
    },
    handleUpdate(row) {
      this.$router.push({ path: '/cost/workHour/form', query: { id: row.whId } })
    },
    handleDetail(row) {
      this.$router.push('/cost/workHour/detail/' + row.whId)
    },
    handleSubmit(row) {
      submitWorkHour(row.whId).then(() => {
        this.$message.success('已提交审批')
        this.load()
      }).catch(() => this.$message.error('提交审批失败'))
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除工时单 "' + row.whNo + '" 吗？').then(() => delWorkHour(row.whId)).then(() => {
        this.$message.success('删除成功')
        this.load()
      }).catch(() => {})
    },
    canEdit(status) { return canEditWorkHour(status) },
    statusLabel(status) { return workHourStatusLabel(status) },
    statusTag(status) {
      return ({ '0': 'info', '1': 'warning', '2': 'success', '3': 'danger', '4': 'info' })[status] || 'info'
    },
    statusClass(status) {
      return status === '4' ? 'work-hour-status-posted' : ''
    },
    formatMoney(value) { return formatMoney(value) },
    formatHours(value) { return formatHours(value) }
  }
}
</script>

<style scoped>
.work-hour-page {
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

.filter-project,
.filter-node {
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

.work-hour-status-posted {
  color: #1890ff;
  background: #e6f7ff;
  border-color: #91d5ff;
}
</style>
