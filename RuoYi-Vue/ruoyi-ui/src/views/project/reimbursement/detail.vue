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
      <div class="section-title">基本信息</div>
      <div class="detail-grid">
        <div class="detail-item"><span>所属项目</span><strong>{{ form.reimbursement.projName }}</strong></div>
        <div class="detail-item"><span>WBS节点</span><strong>{{ form.reimbursement.nodeName }}</strong></div>
        <div class="detail-item"><span>成本科目</span><strong>{{ form.reimbursement.categoryName }}</strong></div>
        <div class="detail-item"><span>费用类型</span><strong>{{ expenseTypeLabel(form.reimbursement.expenseType) }}</strong></div>
        <div class="detail-item detail-item-full"><span>费用说明</span><strong>{{ form.reimbursement.expenseDesc || '-' }}</strong></div>
        <div v-if="form.reimbursement.rejectReason" class="detail-item detail-item-full"><span>驳回原因</span><strong>{{ form.reimbursement.rejectReason }}</strong></div>
      </div>
    </div>

    <div v-if="form.reimbursement" class="detail-card">
      <div class="section-title">发票附件</div>
      <div v-if="form.attachments.length" class="attachment-list"><el-link v-for="item in form.attachments" :key="item.attachmentId || item.filePath" :href="baseUrl + item.filePath" target="_blank" icon="el-icon-paperclip">{{ item.originalName || item.fileName }}</el-link></div>
      <el-empty v-else description="暂无附件" />
    </div>

    <div v-if="form.reimbursement" class="detail-card">
      <div class="section-title">审批轨迹</div>
      <div class="detail-grid">
        <div class="detail-item"><span>创建时间</span><strong>{{ form.reimbursement.createTime || '-' }}</strong></div>
        <div class="detail-item"><span>提交时间</span><strong>{{ form.reimbursement.submitTime || '-' }}</strong></div>
        <div class="detail-item"><span>审批人</span><strong>{{ form.reimbursement.approveBy || '-' }}</strong></div>
        <div class="detail-item"><span>审批时间</span><strong>{{ form.reimbursement.approveTime || '-' }}</strong></div>
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
    approveIt() { this.submitting = true; approveReimbursement(this.form.reimbursement.reimburseId).then(() => { this.$modal.msgSuccess('审批通过'); this.load() }).finally(() => { this.submitting = false }) },
    rejectIt() { this.$prompt('请输入驳回原因', '驳回报销申请', { inputType: 'textarea', inputValidator: v => !!v || '请填写驳回原因' }).then(({ value }) => rejectReimbursement(this.form.reimbursement.reimburseId, { rejectReason: value })).then(() => { this.$modal.msgSuccess('已驳回'); this.load() }) },
    postIt() { this.submitting = true; postReimbursement(this.form.reimbursement.reimburseId).then(() => { this.$modal.msgSuccess('入账成功'); this.load() }).finally(() => { this.submitting = false }) },
    expenseTypeLabel(value) { const item = this.dict.type.exp_expense_type.find(i => i.value === value); return item ? item.label : value },
    statusLabel(status) { return reimbursementStatusLabel(status) },
    statusTag(status) { return reimbursementStatusTagType(status) },
    formatMoney
  }
}
</script>

<style scoped>
.page-heading,.detail-card{background:#fff;border:1px solid #e5e7eb;border-radius:6px;padding:18px 24px;margin-bottom:16px}.page-heading{display:flex;justify-content:space-between;align-items:center}.title-line{display:flex;align-items:center;gap:10px}.title-line h2{margin:0;font-size:24px;color:#1f2937}.page-heading p{margin:6px 0 0;color:#667085}.summary-row{margin-bottom:16px}.summary-item{background:#fff;border:1px solid #e5e7eb;border-radius:6px;padding:18px 22px}.summary-item span{display:block;color:#667085;margin-bottom:8px}.summary-item strong{font-size:22px;color:#1890ff}.section-title{font-size:18px;font-weight:600;color:#1f2937;margin-bottom:18px;padding-left:10px;border-left:4px solid #1890ff}.detail-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px}.detail-item{display:grid;grid-template-columns:110px minmax(0,1fr);align-items:center;background:#f8fafc;border:1px solid #eef2f7;border-radius:6px;padding:12px 14px}.detail-item-full{grid-column:1 / -1}.detail-item span{color:#667085}.detail-item strong{font-weight:500;color:#1f2937;word-break:break-word}.attachment-list{display:flex;flex-direction:column;align-items:flex-start;gap:10px}
</style>
