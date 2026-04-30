<template>
  <div class="app-container project-detail-page" v-loading="loading">
    <div class="page-heading detail-heading" v-if="form.projInfo">
      <div>
        <div class="title-row">
          <h2>{{ form.projInfo.projName }}</h2>
          <el-tag size="small" :type="statusTag(form.projInfo.status)">{{ statusLabel(form.projInfo.status) }}</el-tag>
        </div>
        <p>{{ form.projInfo.projNo }} · {{ form.projInfo.customerName || '未关联客户' }}</p>
      </div>
      <div class="heading-actions">
        <template v-if="form.projInfo.status === '1'">
          <el-button type="success" icon="el-icon-check" :loading="submitting" @click="approveIt">通过</el-button>
          <el-button type="danger" icon="el-icon-close" :loading="submitting" @click="rejectIt">驳回</el-button>
        </template>
        <el-button icon="el-icon-back" @click="$router.push('/project/projInfo')">返回</el-button>
      </div>
    </div>

    <el-row v-if="form.projInfo" :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="8" class="summary-col">
        <div class="summary-item">
          <span>总预算</span>
          <strong>{{ formatMoney(form.projInfo.totalBudget) }}</strong>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8" class="summary-col">
        <div class="summary-item">
          <span>合同金额</span>
          <strong>{{ formatMoney(form.projInfo.contractAmount) }}</strong>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8" class="summary-col">
        <div class="summary-item">
          <span>WBS节点</span>
          <strong>{{ form.wbsNodes.length }}</strong>
        </div>
      </el-col>
    </el-row>

    <div v-if="form.projInfo" class="detail-card">
      <div class="detail-card-title">基本信息</div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">项目编号</span><span class="detail-value">{{ form.projInfo.projNo }}</span></div>
        <div class="detail-item"><span class="detail-label">项目名称</span><span class="detail-value">{{ form.projInfo.projName }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">客户名称</span><span class="detail-value">{{ form.projInfo.customerName || '-' }}</span></div>
        <div class="detail-item"><span class="detail-label">合同金额</span><span class="detail-value">{{ formatMoney(form.projInfo.contractAmount) }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">预计开工</span><span class="detail-value">{{ form.projInfo.planStartDate || '-' }}</span></div>
        <div class="detail-item"><span class="detail-label">预计竣工</span><span class="detail-value">{{ form.projInfo.planEndDate || '-' }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item detail-item-full"><span class="detail-label">项目简介</span><span class="detail-value">{{ form.projInfo.projDesc || '-' }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item detail-item-full"><span class="detail-label">驳回原因</span><span class="detail-value">{{ form.projInfo.rejectReason || '-' }}</span></div>
      </div>
    </div>

    <div v-if="form.projInfo" class="detail-card">
      <div class="table-card-head">
        <div class="detail-card-title">WBS节点-成本分配</div>
        <span>节点预算合计 {{ formatMoney(nodeTotal) }}</span>
      </div>
      <el-table :data="matrixRows" border stripe>
        <el-table-column type="index" label="序号" width="64" align="center" />
        <el-table-column prop="nodeNo" label="节点编号" width="112" align="center" />
        <el-table-column prop="nodeName" label="节点名称" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column prop="planFinishDate" label="预计完成" width="120" align="center" />
        <el-table-column v-for="category in costColumns" :key="category.categoryId" :label="category.categoryName" width="130" align="center">
          <template slot-scope="scope">{{ formatMoney(scope.row.allocations[category.categoryId]) }}</template>
        </el-table-column>
        <el-table-column label="节点预算" width="140" align="center">
          <template slot-scope="scope"><strong>{{ formatMoney(scope.row.nodeBudget) }}</strong></template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!matrixRows.length" description="暂无WBS节点" />
    </div>
  </div>
</template>

<script>
import { listCostCategories } from '@/api/project/costCategory'
import { getProjForm, approve, reject } from '@/api/project/projInfo'
import { formatMoney, projectStatusLabel, projectStatusTagType } from '@/utils/project'

export default {
  name: 'ProjInfoDetail',
  data() {
    return {
      loading: false,
      submitting: false,
      costCategories: [],
      form: { projInfo: null, wbsNodes: [], allocations: [] }
    }
  },
  computed: {
    costColumns() {
      return this.costCategories.filter(item => Number(item.parentId || 0) === 0)
    },
    matrixRows() {
      return this.form.wbsNodes.map(node => {
        const allocations = {}
        this.costColumns.forEach(category => {
          const found = this.form.allocations.find(item => String(item.nodeId) === String(node.nodeId) && String(item.categoryId) === String(category.categoryId))
          allocations[category.categoryId] = found ? Number(found.allocationAmount || 0) : 0
        })
        return Object.assign({}, node, { allocations })
      })
    },
    nodeTotal() {
      return this.matrixRows.reduce((sum, item) => sum + Number(item.nodeBudget || 0), 0)
    }
  },
  created() {
    this.loadPage()
  },
  methods: {
    loadPage() {
      this.loading = true
      Promise.all([this.loadCostCategories()])
        .then(() => this.load())
        .finally(() => {
          this.loading = false
        })
    },
    load() {
      const projId = this.$route.params.id
      if (!projId || projId === 'undefined') {
        this.$message.warning('请先从项目列表选择项目详情')
        this.$router.replace('/project/projInfo')
        return Promise.resolve()
      }
      return getProjForm(projId)
        .then(r => {
          this.form = r.data || this.form
        })
        .catch(() => this.$message.error('项目详情加载失败'))
    },
    loadCostCategories() {
      return listCostCategories({ status: '0' })
        .then(res => {
          this.costCategories = res.data || []
        })
        .catch(() => this.$message.error('成本科目加载失败'))
    },
    approveIt() {
      this.submitting = true
      approve(this.form.projInfo.projId).then(() => {
        this.$message.success('已通过')
        this.load()
      }).catch(() => this.$message.error('审批失败')).finally(() => {
        this.submitting = false
      })
    },
    rejectIt() {
      this.$prompt('请输入驳回原因', '驳回项目', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputValidator: value => !!value,
        inputErrorMessage: '驳回原因不能为空'
      }).then(({ value }) => {
        this.submitting = true
        return reject(this.form.projInfo.projId, { rejectReason: value })
      }).then(() => {
        this.$message.success('已驳回')
        this.load()
      }).catch(error => {
        if (error !== 'cancel' && error !== 'close') {
          this.$message.error('驳回失败')
        }
      }).finally(() => {
        this.submitting = false
      })
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
.project-detail-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.detail-card,
.summary-item {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
}

.detail-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
  padding: 18px 20px;
  margin-bottom: 16px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-row h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
}

.detail-heading p {
  margin: 8px 0 0;
  color: #8c98a8;
}

.heading-actions {
  display: flex;
  gap: 8px;
}

.summary-row {
  margin-bottom: 16px;
}

.summary-col {
  margin-bottom: 0;
}

.summary-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 86px;
  padding: 16px 18px;
}

.summary-item span {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  color: #8c98a8;
}

.summary-item strong {
  color: #1f2d3d;
  font-size: 24px;
  font-weight: 600;
}

.detail-card {
  padding: 18px 20px;
  margin-bottom: 16px;
}

.detail-card-title {
  position: relative;
  margin-bottom: 16px;
  padding-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.detail-card-title::after {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 36px;
  height: 3px;
  content: '';
  background: #1890ff;
  border-radius: 2px;
}

.detail-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-top: 1px solid #eef2f6;
}

.detail-row:first-of-type {
  border-top: 0;
}

.detail-item {
  display: grid;
  grid-template-columns: 124px minmax(0, 1fr);
  min-height: 48px;
}

.detail-item-full {
  grid-column: 1 / -1;
}

.detail-label {
  display: flex;
  align-items: center;
  padding: 12px;
  line-height: 1.6;
  color: #606266;
  background: #f8f8f9;
  white-space: nowrap;
}

.detail-value {
  display: flex;
  align-items: center;
  padding: 12px;
  line-height: 1.6;
  color: #1f2d3d;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.detail-item-full .detail-label,
.detail-item-full .detail-value {
  align-items: flex-start;
}

.detail-item-full .detail-value {
  white-space: pre-wrap;
}

.table-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  color: #8c98a8;
  font-size: 13px;
}

@media (max-width: 768px) {
  .detail-heading,
  .heading-actions,
  .table-card-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .detail-row {
    grid-template-columns: 1fr;
  }

  .detail-item {
    grid-template-columns: 112px minmax(0, 1fr);
    width: 100%;
  }

  .summary-col {
    margin-bottom: 12px;
  }
}
</style>
