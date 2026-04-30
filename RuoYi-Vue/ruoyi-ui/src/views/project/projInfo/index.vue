<template>
  <div class="app-container project-list-page">
    <div class="page-heading">
      <div>
        <h2>项目立项</h2>
        <p>维护项目立项方案、WBS节点和审批状态</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="$router.push('/project/projInfo/form')">新建项目</el-button>
    </div>

    <div class="filter-panel">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="72px">
        <el-form-item label="项目名称" prop="projName">
          <el-input class="filter-name" v-model="queryParams.projName" clearable placeholder="请输入项目名称" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" class="filter-status">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
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
          <span class="toolbar-title">项目列表</span>
          <span class="toolbar-count">共 {{ total }} 条</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="projNo" label="项目编号" width="170" align="center" />
        <el-table-column label="项目名称" min-width="220" align="center" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="project-name">
              <span>{{ scope.row.projName }}</span>
              <small>{{ scope.row.customerName || '未关联客户' }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="planStartDate" label="预计开工" width="120" align="center" />
        <el-table-column prop="planEndDate" label="预计竣工" width="120" align="center" />
        <el-table-column label="总预算" width="140" align="center">
          <template slot-scope="scope">{{ formatMoney(scope.row.totalBudget) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" @click="$router.push('/project/projInfo/detail/' + scope.row.projId)">详情</el-button>
            <el-button v-if="canEdit(scope.row)" type="text" icon="el-icon-edit" @click="$router.push('/project/projInfo/form?id=' + scope.row.projId)">编辑</el-button>
            <el-button v-if="canEdit(scope.row)" type="text" icon="el-icon-upload2" @click="submit(scope.row)">提交</el-button>
            <el-button v-if="canEdit(scope.row)" type="text" icon="el-icon-delete" class="danger-action" @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="load" />
    </div>
  </div>
</template>

<script>
import { listProjInfos, submitForApproval, delProjInfo } from '@/api/project/projInfo'
import { PROJECT_STATUS_OPTIONS, canEditProject, formatMoney, projectStatusLabel, projectStatusTagType } from '@/utils/project'

export default {
  name: 'ProjInfoIndex',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      statusOptions: PROJECT_STATUS_OPTIONS,
      queryParams: { pageNum: 1, pageSize: 10, projName: undefined, status: undefined }
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
      }).catch(() => {
        this.$message.error('项目列表加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.load()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    canEdit(row) {
      return canEditProject(row.status)
    },
    submit(row) {
      submitForApproval(row.projId).then(() => {
        this.$message.success('已提交审批')
        this.load()
      }).catch(() => this.$message.error('提交审批失败'))
    },
    remove(row) {
      this.$modal.confirm('确认删除项目 "' + row.projName + '" 吗？').then(() => delProjInfo(row.projId)).then(() => {
        this.$message.success('删除成功')
        this.load()
      }).catch(() => {})
    },
    statusLabel(status) {
      return projectStatusLabel(status)
    },
    statusTag(status) {
      return projectStatusTagType(status)
    },
    formatMoney(value) {
      return formatMoney(value)
    }
  }
}
</script>

<style scoped>
.project-list-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.page-heading h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
}

.page-heading p {
  margin: 0;
  font-size: 13px;
  color: #7a8797;
}

.filter-panel,
.table-panel {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
  padding: 16px 18px;
}

.filter-panel {
  margin-bottom: 12px;
}

.filter-panel .el-form-item {
  margin-bottom: 0;
}

.filter-name {
  width: 280px;
}

.filter-status {
  width: 160px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.toolbar-count {
  margin-left: 8px;
  font-size: 12px;
  color: #8c98a8;
}

.project-name {
  display: flex;
  flex-direction: column;
  line-height: 20px;
}

.project-name span {
  font-weight: 500;
  color: #1f2d3d;
}

.project-name small {
  color: #8c98a8;
}

.danger-action {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .page-heading {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .filter-name,
  .filter-status {
    width: 100%;
  }
}
</style>
