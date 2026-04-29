<template>
  <div class="app-container project-detail-page" v-if="form.projInfo">
    <div class="detail-heading">
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

    <el-row :gutter="12" class="summary-row">
      <el-col :xs="24" :sm="8">
        <div class="summary-item">
          <span>总预算</span>
          <strong>{{ formatMoney(form.projInfo.totalBudget) }}</strong>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="summary-item">
          <span>合同金额</span>
          <strong>{{ formatMoney(form.projInfo.contractAmount) }}</strong>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="summary-item">
          <span>WBS节点</span>
          <strong>{{ form.wbsNodes.length }}</strong>
        </div>
      </el-col>
    </el-row>

    <div class="detail-panel">
      <div class="section-head">
        <span class="section-title">基本信息</span>
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目编号">{{ form.projInfo.projNo }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ form.projInfo.projName }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ form.projInfo.customerName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(form.projInfo.status) }}</el-descriptions-item>
        <el-descriptions-item label="预计开工">{{ form.projInfo.planStartDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预计竣工">{{ form.projInfo.planEndDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="项目简介" :span="2">{{ form.projInfo.projDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="驳回原因" :span="2">{{ form.projInfo.rejectReason || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="detail-panel">
      <div class="section-head">
        <span class="section-title">WBS节点</span>
        <span class="section-subtitle">节点预算合计 {{ formatMoney(nodeTotal) }}</span>
      </div>
      <el-table :data="form.wbsNodes" border stripe>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="nodeNo" label="节点编号" width="120" align="center" />
        <el-table-column prop="nodeName" label="节点名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="planFinishDate" label="预计完成" width="130" align="center" />
        <el-table-column label="预算" width="160" align="right">
          <template slot-scope="scope">{{ formatMoney(scope.row.nodeBudget) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { getProjForm, approve, reject } from '@/api/project/projInfo'

const STATUS_OPTIONS = [
  { value: '0', label: '草稿' },
  { value: '1', label: '审批中' },
  { value: '2', label: '已立项' },
  { value: '3', label: '已驳回' },
  { value: '4', label: '进行中' },
  { value: '5', label: '已完工' }
]

export default {
  data() {
    return { submitting: false, form: { projInfo: null, wbsNodes: [] } }
  },
  computed: {
    nodeTotal() {
      return this.form.wbsNodes.reduce((sum, item) => sum + Number(item.nodeBudget || 0), 0)
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      getProjForm(this.$route.params.id).then(r => {
        this.form = r.data || this.form
      }).catch(() => this.$message.error('项目详情加载失败'))
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
      }).catch(() => {}).finally(() => {
        this.submitting = false
      })
    },
    statusLabel(status) {
      const item = STATUS_OPTIONS.find(i => i.value === status)
      return item ? item.label : status
    },
    statusTag(status) {
      return ({ '0': 'info', '1': 'warning', '2': 'success', '3': 'danger', '4': '', '5': 'success' })[status] || 'info'
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    }
  }
}
</script>

<style scoped>
.project-detail-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.detail-heading,
.detail-panel,
.summary-item {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
}

.detail-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  margin-bottom: 12px;
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
  color: #7a8797;
}

.heading-actions {
  display: flex;
  gap: 8px;
}

.summary-row {
  margin-bottom: 12px;
}

.summary-item {
  padding: 16px 18px;
  min-height: 86px;
}

.summary-item span {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  color: #7a8797;
}

.summary-item strong {
  color: #1f2d3d;
  font-size: 24px;
  font-weight: 600;
}

.detail-panel {
  padding: 18px 20px;
  margin-bottom: 12px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.section-subtitle {
  color: #8c98a8;
  font-size: 12px;
}

@media (max-width: 768px) {
  .detail-heading,
  .heading-actions {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
