<template>
  <div class="app-container reimbursement-detail-page" v-loading="loading">
    <div class="page-heading" v-if="form.reimbursement">
      <div><div class="title-line"><h2>{{ form.reimbursement.reimburseNo }}</h2><el-tag size="small" :type="statusTag(form.reimbursement.status)">{{ statusLabel(form.reimbursement.status) }}</el-tag></div><p>{{ form.reimbursement.projName }} · {{ form.reimbursement.nodeName }}</p></div>
      <div>
        <template v-if="form.reimbursement.status === '1'"><el-button type="success" icon="el-icon-check" :loading="submitting" @click="approveIt" v-hasPermi="['cost:reimbursement:approve']">通过</el-button><el-button type="danger" icon="el-icon-close" :loading="submitting" @click="rejectIt" v-hasPermi="['cost:reimbursement:approve']">驳回</el-button></template>
        <el-button v-if="form.reimbursement.status === '2'" type="primary" icon="el-icon-finished" :loading="submitting" @click="postIt" v-hasPermi="['cost:reimbursement:post']">入账</el-button>
        <el-button icon="el-icon-back" @click="$router.push('/cost/reimbursement')">返回</el-button>
      </div>
    </div>

    <el-row v-if="form.reimbursement" :gutter="16" class="summary-row">
      <el-col :span="8"><div class="summary-item"><span>报销金额</span><strong>{{ formatMoney(form.reimbursement.amount) }}</strong></div></el-col>
      <el-col :span="8"><div class="summary-item"><span>发票张数</span><strong>{{ form.reimbursement.invoiceCount || 0 }}</strong></div></el-col>
      <el-col :span="8"><div class="summary-item"><span>发生日期</span><strong>{{ form.reimbursement.expenseDate }}</strong></div></el-col>
    </el-row>

    <div v-if="form.reimbursement" class="detail-card">
      <div class="detail-card-title">基本信息</div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">所属项目</span><span class="detail-value">{{ form.reimbursement.projName }}</span></div>
        <div class="detail-item"><span class="detail-label">WBS节点</span><span class="detail-value">{{ form.reimbursement.nodeName }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">成本科目</span><span class="detail-value">{{ form.reimbursement.categoryName }}</span></div>
        <div class="detail-item"><span class="detail-label">费用类型</span><span class="detail-value">{{ expenseTypeLabel(form.reimbursement.expenseType) }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item detail-item-full"><span class="detail-label">费用说明</span><span class="detail-value">{{ form.reimbursement.expenseDesc || '-' }}</span></div>
      </div>
      <div v-if="form.reimbursement.rejectReason" class="detail-row">
        <div class="detail-item detail-item-full"><span class="detail-label">驳回原因</span><span class="detail-value">{{ form.reimbursement.rejectReason }}</span></div>
      </div>
    </div>

    <div v-if="form.reimbursement" class="detail-card">
      <div class="detail-card-title">发票附件</div>
      <div v-if="form.attachments.length" class="attachment-list"><el-link v-for="item in form.attachments" :key="item.attachmentId || item.filePath" :href="baseUrl + item.filePath" target="_blank" icon="el-icon-paperclip">{{ item.originalName || item.fileName }}</el-link></div>
      <el-empty v-else description="暂无附件" />
    </div>

    <div v-if="form.reimbursement" class="detail-card">
      <div class="detail-card-title">审批轨迹</div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">创建时间</span><span class="detail-value">{{ form.reimbursement.createTime || '-' }}</span></div>
        <div class="detail-item"><span class="detail-label">提交时间</span><span class="detail-value">{{ form.reimbursement.submitTime || '-' }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">审批人</span><span class="detail-value">{{ form.reimbursement.approveBy || '-' }}</span></div>
        <div class="detail-item"><span class="detail-label">审批时间</span><span class="detail-value">{{ form.reimbursement.approveTime || '-' }}</span></div>
      </div>
    </div>
  </div>
</template>

<script>
import { approveReimbursement, getReimbursement, postReimbursement, rejectReimbursement } from '@/api/project/reimbursement'
import { formatMoney, reimbursementStatusLabel, reimbursementStatusTagType } from '@/utils/project'

export default {
  name: 'ReimbursementDetail',
  dicts: ['exp_expense_type'],
  data() { return { loading: false, submitting: false, baseUrl: process.env.VUE_APP_BASE_API, form: { reimbursement: null, attachments: [] } } },
  created() { this.load() },
  methods: {
    load() {
      const id = this.$route.params.id
      if (!id || id === 'undefined') { this.$message.error('报销参数缺失'); this.$router.replace('/cost/reimbursement'); return }
      this.loading = true
      getReimbursement(id).then(res => { const data = res.data || {}; this.form = { reimbursement: data.reimbursement, attachments: data.attachments || [] } }).catch(() => this.$message.error('报销详情加载失败')).finally(() => { this.loading = false })
    },
    approveIt() {
      this.submitting = true
      approveReimbursement(this.form.reimbursement.reimburseId).then(() => {
        this.$message.success('审批通过')
        this.load()
      }).catch(() => this.$message.error('审批失败')).finally(() => { this.submitting = false })
    },
    rejectIt() {
      if (this.submitting) return
      this.submitting = true
      this.$prompt('请输入驳回原因', '驳回报销申请', { inputType: 'textarea', inputValidator: v => !!v || '请填写驳回原因' }).then(({ value }) => {
        return rejectReimbursement(this.form.reimbursement.reimburseId, { rejectReason: value })
      }).then(() => {
        this.$message.success('已驳回')
        this.load()
      }).catch(error => {
        if (error !== 'cancel') {
          this.$message.error('驳回失败')
        }
      }).finally(() => { this.submitting = false })
    },
    postIt() {
      this.submitting = true
      postReimbursement(this.form.reimbursement.reimburseId).then(() => {
        this.$message.success('入账成功')
        this.load()
      }).catch(() => this.$message.error('入账失败')).finally(() => { this.submitting = false })
    },
    expenseTypeLabel(value) { const item = this.dict.type.exp_expense_type.find(i => i.value === value); return item ? item.label : value },
    statusLabel(status) { return reimbursementStatusLabel(status) },
    statusTag(status) { return reimbursementStatusTagType(status) },
    formatMoney
  }
}
</script>

<style scoped>
.reimbursement-detail-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-heading,
.summary-item,
.detail-card {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  margin-bottom: 16px;
}

.title-line {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-line h2 {
  margin: 0;
  color: #1f2d3d;
  font-size: 22px;
  font-weight: 600;
}

.page-heading p {
  margin: 8px 0 0;
  color: #8c98a8;
  font-size: 13px;
}

.summary-row {
  margin-bottom: 16px;
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
  color: #8c98a8;
  font-size: 13px;
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
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 600;
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
  grid-template-columns: 110px minmax(0, 1fr);
  min-height: 48px;
}

.detail-item-full {
  grid-column: 1 / -1;
}

.detail-label,
.detail-value {
  padding: 12px;
  line-height: 1.6;
}

.detail-label {
  color: #606266;
  white-space: nowrap;
  background: #f8f8f9;
}

.detail-value {
  color: #1f2d3d;
  overflow-wrap: anywhere;
}

.detail-item-full .detail-value {
  white-space: pre-wrap;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@media (max-width: 768px) {
  .page-heading {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .detail-row {
    grid-template-columns: 1fr;
  }
}
</style>
