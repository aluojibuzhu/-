<template>
  <div class="app-container reimbursement-form-page">
    <div class="page-heading">
      <div><h2>{{ form.reimbursement.reimburseId ? '编辑报销申请' : '新建报销申请' }}</h2><p>填写项目费用信息并上传发票附件</p></div>
      <div><el-button icon="el-icon-back" @click="$router.push('/cost/reimbursement')">返回列表</el-button><el-button type="primary" icon="el-icon-document-checked" :loading="saving" @click="saveDraft">保存草稿</el-button></div>
    </div>

    <el-form ref="form" :model="form.reimbursement" :rules="rules" size="small" label-width="100px">
      <div class="form-panel">
        <div class="form-header">报销信息</div>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="报销编号"><el-input v-model="form.reimbursement.reimburseNo" disabled placeholder="保存后自动生成" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="所属项目" prop="projId"><el-select v-model="form.reimbursement.projId" filterable clearable class="field-full" placeholder="请选择已立项项目" @change="handleProjectChange"><el-option v-for="item in projects" :key="item.projId" :label="item.projName" :value="item.projId" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="WBS节点" prop="nodeId"><el-select v-model="form.reimbursement.nodeId" filterable clearable class="field-full" placeholder="请选择WBS节点" @change="handleNodeChange"><el-option v-for="item in nodes" :key="item.nodeId" :label="item.nodeName" :value="item.nodeId" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="成本科目" prop="categoryId"><el-select v-model="form.reimbursement.categoryId" filterable clearable class="field-full" placeholder="请选择二级科目" @change="handleCategoryChange"><el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="费用类型" prop="expenseType"><el-select v-model="form.reimbursement.expenseType" clearable class="field-full" placeholder="请选择费用类型" @change="markDirty"><el-option v-for="dict in dict.type.exp_expense_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="发生日期" prop="expenseDate"><el-date-picker v-model="form.reimbursement.expenseDate" value-format="yyyy-MM-dd" type="date" class="field-full" placeholder="选择日期" @change="markDirty" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="报销金额" prop="amount"><el-input-number v-model="form.reimbursement.amount" :min="0.01" :step="100" :precision="2" controls-position="right" class="field-full" @change="markDirty" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="发票张数" prop="invoiceCount"><el-input-number v-model="form.reimbursement.invoiceCount" :min="1" :step="1" controls-position="right" class="field-full" @change="markDirty" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="费用说明" prop="expenseDesc"><el-input v-model="form.reimbursement.expenseDesc" clearable type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入费用说明" @input="markDirty" /></el-form-item></el-col>
        </el-row>
      </div>

      <div class="form-panel">
        <div class="form-header">发票附件</div>
        <el-form-item label="附件" prop="attachmentPaths"><file-upload v-model="attachmentPaths" :limit="8" :file-size="10" :file-type="['jpg','jpeg','png','pdf']" @input="markDirty" /></el-form-item>
      </div>
    </el-form>

    <div class="footer-actions">
      <div class="amount-total">报销金额：<strong>{{ formatMoney(form.reimbursement.amount) }}</strong></div>
      <div><el-button type="primary" :loading="saving" @click="saveDraft">保存草稿</el-button><el-button type="success" icon="el-icon-upload2" :disabled="!form.reimbursement.reimburseId" @click="submit">提交审批</el-button><el-button @click="$router.push('/cost/reimbursement')">取消</el-button></div>
    </div>
  </div>
</template>

<script>
import { getReimbursement, listReimbursementWbsNodes, saveReimbursementDraft, submitReimbursement } from '@/api/project/reimbursement'
import { listProjInfos } from '@/api/project/projInfo'
import { listCostCategories } from '@/api/project/costCategory'
import { formatMoney } from '@/utils/project'

export default {
  name: 'ReimbursementForm',
  dicts: ['exp_expense_type'],
  data() {
    return {
      saving: false, dirty: false, projects: [], nodes: [], categories: [], attachmentPaths: '',
      form: { reimbursement: { amount: 0.01, invoiceCount: 1, status: '0' }, attachments: [] },
      rules: {
        projId: [{ required: true, message: '请选择项目', trigger: 'change' }],
        nodeId: [{ required: true, message: '请选择WBS节点', trigger: 'change' }],
        categoryId: [{ required: true, message: '请选择成本科目', trigger: 'change' }],
        expenseType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
        expenseDate: [{ required: true, message: '请选择发生日期', trigger: 'change' }],
        amount: [{ required: true, message: '请输入报销金额', trigger: 'change' }],
        invoiceCount: [{ required: true, message: '请输入发票张数', trigger: 'change' }],
        expenseDesc: [{ required: true, message: '请输入费用说明', trigger: 'blur' }]
      }
    }
  },
  created() {
    Promise.all([this.loadProjects(), this.loadCategories()]).then(() => { if (this.$route.query.id) this.loadForm(this.$route.query.id) })
  },
  beforeRouteLeave(to, from, next) {
    if (!this.dirty) { next(); return }
    this.$confirm('当前页面有未保存内容，确认离开吗？', '提示', { type: 'warning' }).then(() => next()).catch(() => next(false))
  },
  methods: {
    loadProjects() { return Promise.all([listProjInfos({ pageNum: 1, pageSize: 100, status: '2' }), listProjInfos({ pageNum: 1, pageSize: 100, status: '4' })]).then(([a, b]) => { this.projects = [].concat(a.rows || [], b.rows || []) }) },
    loadCategories() { return listCostCategories({ status: '0' }).then(res => { this.categories = (res.data || []).filter(item => item.categoryLevel === 2) }) },
    loadForm(id) { return getReimbursement(id).then(res => { const data = res.data || {}; this.form = { reimbursement: data.reimbursement || {}, attachments: data.attachments || [] }; this.attachmentPaths = this.form.attachments.map(item => item.filePath).join(','); if (this.form.reimbursement.projId) this.loadNodes(this.form.reimbursement.projId); this.dirty = false }) },
    loadNodes(projId) { return listReimbursementWbsNodes(projId).then(res => { this.nodes = res.data || [] }) },
    handleProjectChange(value) { this.form.reimbursement.nodeId = undefined; this.form.reimbursement.nodeName = ''; this.nodes = []; const project = this.projects.find(item => item.projId === value); this.form.reimbursement.projName = project ? project.projName : ''; if (value) this.loadNodes(value); this.markDirty() },
    handleNodeChange(value) { const node = this.nodes.find(item => item.nodeId === value); this.form.reimbursement.nodeName = node ? node.nodeName : ''; this.markDirty() },
    handleCategoryChange(value) { const category = this.categories.find(item => item.categoryId === value); this.form.reimbursement.categoryName = category ? category.categoryName : ''; this.markDirty() },
    buildPayload() { const paths = this.attachmentPaths ? this.attachmentPaths.split(',').filter(Boolean) : []; return { reimbursement: Object.assign({}, this.form.reimbursement), attachments: paths.map(path => ({ fileName: path.substring(path.lastIndexOf('/') + 1), originalName: path.substring(path.lastIndexOf('/') + 1), filePath: path })) } },
    saveDraft() {
      this.persistDraft().then(() => this.$message.success('保存成功')).catch(error => {
        if (!['invalid', 'attachment required'].includes(error.message)) {
          this.$message.error('保存失败')
        }
      })
    },
    persistDraft() {
      return new Promise((resolve, reject) => {
        this.$refs.form.validate(valid => {
          if (!valid) { reject(new Error('invalid')); return }
          if (!this.attachmentPaths) {
            this.$message.warning('请上传发票附件')
            reject(new Error('attachment required'))
            return
          }
          this.saving = true
          saveReimbursementDraft(this.buildPayload()).then(res => { this.form.reimbursement = res.data || this.form.reimbursement; this.dirty = false; resolve(this.form.reimbursement) }).catch(reject).finally(() => { this.saving = false })
        })
      })
    },
    submit() {
      const doSubmit = () => submitReimbursement(this.form.reimbursement.reimburseId).then(() => { this.$message.success('提交成功'); this.dirty = false; this.$router.push('/cost/reimbursement') }).catch(() => this.$message.error('提交失败'))
      this.dirty ? this.persistDraft().then(doSubmit).catch(error => {
        if (!['invalid', 'attachment required'].includes(error.message)) {
          this.$message.error('保存失败')
        }
      }) : doSubmit()
    },
    markDirty() { this.dirty = true },
    formatMoney
  }
}
</script>

<style scoped>
.reimbursement-form-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
  padding-bottom: 76px;
}

.page-heading,
.form-panel,
.footer-actions {
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

.page-heading h2 {
  margin: 0 0 6px;
  color: #1f2d3d;
  font-size: 22px;
  font-weight: 600;
}

.page-heading p {
  margin: 0;
  color: #8c98a8;
  font-size: 13px;
}

.form-panel {
  padding: 18px 20px 16px;
  margin-bottom: 16px;
}

.form-header {
  position: relative;
  margin: 0 0 18px;
  padding-bottom: 10px;
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 600;
}

.form-header::after {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 36px;
  height: 3px;
  content: '';
  background: #1890ff;
  border-radius: 2px;
}

.field-full {
  width: 100%;
}

.footer-actions {
  position: fixed;
  right: 20px;
  bottom: 18px;
  left: 220px;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 56px;
  padding: 10px 18px;
}

.amount-total {
  color: #606266;
  font-size: 14px;
}

.amount-total strong {
  color: #1f2d3d;
  font-size: 18px;
}

@media (max-width: 992px) {
  .footer-actions {
    left: 20px;
  }
}

@media (max-width: 768px) {
  .page-heading,
  .footer-actions {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }
}
</style>
