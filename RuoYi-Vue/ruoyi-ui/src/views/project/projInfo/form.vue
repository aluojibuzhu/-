<template>
  <div class="app-container project-form-page">
    <div class="page-heading">
      <div>
        <h2>{{ form.projInfo.projId ? '编辑项目立项' : '新建项目立项' }}</h2>
        <p>一次性维护项目基本信息、WBS节点和预算计划</p>
      </div>
      <el-button icon="el-icon-back" @click="$router.push('/project/projInfo')">返回列表</el-button>
    </div>

    <div class="form-panel">
      <div class="section-head">
        <div>
          <span class="section-title">基本信息</span>
          <span class="section-subtitle">项目编号保存后自动生成</span>
        </div>
      </div>

      <el-form ref="form" :model="form.projInfo" :rules="rules" label-width="96px">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="项目编号">
              <el-input v-model="form.projInfo.projNo" disabled placeholder="保存后自动生成" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="项目名称" prop="projName">
              <el-input v-model="form.projInfo.projName" maxlength="50" show-word-limit placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="合同金额" prop="contractAmount">
              <el-input-number v-model="form.projInfo.contractAmount" :min="0" :precision="2" controls-position="right" class="field-full" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="关联客户" prop="customerId">
              <div class="customer-picker">
                <el-select v-model="form.projInfo.customerId" filterable clearable placeholder="请选择客户">
                  <el-option v-for="c in customers" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                </el-select>
                <el-button icon="el-icon-plus" @click="$refs.customer.show()">新建</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="预计开工" prop="planStartDate">
              <el-date-picker v-model="form.projInfo.planStartDate" value-format="yyyy-MM-dd" type="date" placeholder="选择日期" class="field-full" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="8">
            <el-form-item label="预计竣工" prop="planEndDate">
              <el-date-picker v-model="form.projInfo.planEndDate" value-format="yyyy-MM-dd" type="date" placeholder="选择日期" class="field-full" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="项目简介">
              <el-input v-model="form.projInfo.projDesc" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入项目简介" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div class="form-panel">
      <div class="section-head">
        <div>
          <span class="section-title">WBS节点</span>
          <span class="section-subtitle">当前 {{ form.wbsNodes.length }} 个节点，总预算 {{ formatMoney(nodeTotal) }}</span>
        </div>
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="addNode">新增节点</el-button>
      </div>

      <el-table :data="form.wbsNodes" border>
        <el-table-column label="序号" width="70" align="center">
          <template slot-scope="scope">{{ scope.$index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="nodeNo" label="节点编号" width="120" align="center">
          <template slot-scope="scope">{{ scope.row.nodeNo || nextNodeNo(scope.$index) }}</template>
        </el-table-column>
        <el-table-column label="节点名称" min-width="220">
          <template slot-scope="scope"><el-input v-model="scope.row.nodeName" maxlength="30" placeholder="如：基础工程" /></template>
        </el-table-column>
        <el-table-column label="预计完成" width="180">
          <template slot-scope="scope"><el-date-picker v-model="scope.row.planFinishDate" value-format="yyyy-MM-dd" type="date" class="table-date" /></template>
        </el-table-column>
        <el-table-column label="节点预算" width="180">
          <template slot-scope="scope"><el-input-number v-model="scope.row.nodeBudget" :min="0" :precision="2" controls-position="right" class="table-number" /></template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template slot-scope="scope"><el-button type="text" icon="el-icon-delete" class="danger-action" @click="form.wbsNodes.splice(scope.$index, 1)">删除</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <div class="action-bar">
      <div class="budget-total">项目总预算：<strong>{{ formatMoney(nodeTotal) }}</strong></div>
      <div>
        <el-button type="primary" :loading="saving" @click="save">保存草稿</el-button>
        <el-button type="success" :disabled="!form.projInfo.projId" @click="submit">提交审批</el-button>
        <el-button @click="$router.push('/project/projInfo')">取消</el-button>
      </div>
    </div>

    <customer-modal ref="customer" @done="handleCustomerDone" />
  </div>
</template>

<script>
import CustomerModal from '../components/CustomerModal'
import { getProjForm, saveDraft, submitForApproval } from '@/api/project/projInfo'
import { listCustomers } from '@/api/project/projCustomer'

export default {
  components: { CustomerModal },
  data() {
    return {
      saving: false,
      customers: [],
      form: { projInfo: {}, wbsNodes: [], allocations: [] },
      rules: {
        projName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
        customerId: [{ required: true, message: '请选择关联客户', trigger: 'change' }],
        contractAmount: [{ required: true, message: '请输入合同金额', trigger: 'change' }],
        planStartDate: [{ required: true, message: '请选择预计开工日期', trigger: 'change' }],
        planEndDate: [{ required: true, message: '请选择预计竣工日期', trigger: 'change' }]
      }
    }
  },
  computed: {
    nodeTotal() {
      return this.form.wbsNodes.reduce((sum, item) => sum + Number(item.nodeBudget || 0), 0)
    }
  },
  created() {
    this.loadCustomers()
    if (this.$route.query.id) {
      this.loadForm(this.$route.query.id)
    }
  },
  methods: {
    loadForm(id) {
      getProjForm(id).then(res => {
        this.form = res.data || this.form
      }).catch(() => this.$message.error('项目详情加载失败'))
    },
    loadCustomers() {
      listCustomers({ pageNum: 1, pageSize: 500 }).then(r => {
        this.customers = r.rows || []
      }).catch(() => this.$message.error('客户列表加载失败'))
    },
    handleCustomerDone(customer) {
      this.loadCustomers()
      if (customer && customer.customerId) {
        this.form.projInfo.customerId = customer.customerId
        this.form.projInfo.customerName = customer.customerName
      }
    },
    addNode() {
      this.form.wbsNodes.push({ nodeNo: '', nodeName: '', planFinishDate: '', nodeBudget: 0 })
    },
    nextNodeNo(index) {
      return 'ND-' + String(index + 1).padStart(3, '0')
    },
    save() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (!this.form.wbsNodes.length) {
          this.$message.warning('请至少新增一个WBS节点')
          return
        }
        const customer = this.customers.find(i => i.customerId === this.form.projInfo.customerId)
        this.form.projInfo.customerName = customer ? customer.customerName : ''
        this.saving = true
        saveDraft(this.form).then(res => {
          this.form.projInfo = res.data || this.form.projInfo
          this.$message.success('保存成功')
        }).catch(() => this.$message.error('保存失败')).finally(() => {
          this.saving = false
        })
      })
    },
    submit() {
      submitForApproval(this.form.projInfo.projId).then(() => {
        this.$message.success('已提交审批')
        this.$router.push('/project/projInfo')
      }).catch(() => this.$message.error('提交审批失败'))
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    }
  }
}
</script>

<style scoped>
.project-form-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
  padding-bottom: 86px;
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

.form-panel {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
  padding: 18px 20px 6px;
  margin-bottom: 12px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.section-subtitle {
  margin-left: 10px;
  font-size: 12px;
  color: #8c98a8;
}

.field-full,
.table-date,
.table-number {
  width: 100%;
}

.customer-picker {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 72px;
  column-gap: 8px;
}

.customer-picker .el-select {
  width: 100%;
}

.action-bar {
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
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
  box-shadow: 0 6px 18px rgba(31, 45, 61, 0.08);
}

.budget-total {
  font-size: 14px;
  color: #606266;
}

.budget-total strong {
  color: #1f2d3d;
  font-size: 18px;
}

.danger-action {
  color: #f56c6c;
}

@media (max-width: 992px) {
  .action-bar {
    left: 20px;
  }
}

@media (max-width: 768px) {
  .page-heading,
  .section-head,
  .action-bar {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }
}
</style>
