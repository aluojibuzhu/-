<template>
  <div class="app-container project-form-page">
    <div class="page-heading">
      <div>
        <h2>{{ form.projInfo.projId ? '编辑项目立项' : '新建项目立项' }}</h2>
        <p>一次性维护项目基本信息、WBS节点和成本分配</p>
      </div>
      <div class="heading-actions">
        <el-button icon="el-icon-back" @click="$router.push('/project/projInfo')">返回列表</el-button>
        <el-button type="primary" icon="el-icon-document-checked" :loading="saving" @click="save">保存草稿</el-button>
      </div>
    </div>

    <div class="form-body">
      <div class="form-group">
        <h3 class="form-header">基本信息</h3>
        <el-form ref="form" size="small" :model="form.projInfo" :rules="rules" label-width="100px">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="项目编号">
                <el-input v-model="form.projInfo.projNo" disabled placeholder="保存后自动生成" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="项目名称" prop="projName">
                <el-input v-model="form.projInfo.projName" clearable maxlength="50" show-word-limit placeholder="请输入项目名称" @input="markDirty" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="合同金额" prop="contractAmount">
                <el-input-number v-model="form.projInfo.contractAmount" :min="0" :precision="2" controls-position="right" class="field-full" @change="markDirty" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="关联客户" prop="customerId">
                <div class="customer-picker">
                  <el-select v-model="form.projInfo.customerId" filterable clearable placeholder="请选择客户" @change="markDirty">
                    <el-option v-for="c in customers" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                  </el-select>
                  <el-button icon="el-icon-plus" @click="$refs.customer.show()">新建</el-button>
                </div>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="预计开工" prop="planStartDate">
                <el-date-picker v-model="form.projInfo.planStartDate" value-format="yyyy-MM-dd" type="date" placeholder="选择日期" class="field-full" @change="markDirty" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="预计竣工" prop="planEndDate">
                <el-date-picker v-model="form.projInfo.planEndDate" value-format="yyyy-MM-dd" type="date" placeholder="选择日期" class="field-full" @change="markDirty" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="项目简介">
                <el-input v-model="form.projInfo.projDesc" clearable type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入项目简介" @input="markDirty" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <div class="form-group">
        <div class="subtable-header">
          <h3 class="form-header">WBS节点-成本分配</h3>
          <div>
            <span class="subtable-summary">节点 {{ form.wbsNodes.length }} 个 · 总预算 {{ formatMoney(nodeTotal) }}</span>
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="addNode">新增节点</el-button>
          </div>
        </div>

        <el-table :data="form.wbsNodes" border stripe>
          <el-table-column label="序号" width="64" align="center">
            <template slot-scope="scope">{{ scope.$index + 1 }}</template>
          </el-table-column>
          <el-table-column prop="nodeNo" label="节点编号" width="112" align="center">
            <template slot-scope="scope">{{ scope.row.nodeNo || nextNodeNo(scope.$index) }}</template>
          </el-table-column>
          <el-table-column label="节点名称" min-width="180" align="center">
            <template slot-scope="scope">
              <el-input v-model="scope.row.nodeName" clearable maxlength="30" placeholder="如：基础工程" @input="markDirty" />
            </template>
          </el-table-column>
          <el-table-column label="预计完成" width="160" align="center">
            <template slot-scope="scope">
              <el-date-picker v-model="scope.row.planFinishDate" value-format="yyyy-MM-dd" type="date" class="table-date" @change="markDirty" />
            </template>
          </el-table-column>
          <el-table-column v-for="category in costColumns" :key="category.categoryId" :label="category.categoryName" width="150" align="center">
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.allocations[category.categoryId]"
                type="number"
                min="0"
                step="0.01"
                class="table-number amount-input"
                @input="handleAllocationInput(scope.row)"
                @blur="normalizeAllocation(scope.row, category.categoryId)"
              />
            </template>
          </el-table-column>
          <el-table-column label="节点预算" width="150" align="center">
            <template slot-scope="scope">
              <span class="budget-text">{{ formatMoney(scope.row.nodeBudget) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="188" align="center" fixed="right">
            <template slot-scope="scope">
              <el-button type="text" icon="el-icon-top" :disabled="scope.$index === 0" @click="moveNode(scope.$index, -1)">上移</el-button>
              <el-button type="text" icon="el-icon-bottom" :disabled="scope.$index === form.wbsNodes.length - 1" @click="moveNode(scope.$index, 1)">下移</el-button>
              <el-button type="text" icon="el-icon-delete" class="danger-action" @click="removeNode(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!form.wbsNodes.length" description="暂无WBS节点" />
      </div>
    </div>

    <div class="action-bar">
      <div class="budget-total">项目总预算：<strong>{{ formatMoney(nodeTotal) }}</strong></div>
      <div>
        <el-button type="primary" icon="el-icon-document-checked" :loading="saving" @click="save">保存草稿</el-button>
        <el-button type="success" icon="el-icon-upload2" :disabled="!form.projInfo.projId" @click="submit">提交审批</el-button>
        <el-button icon="el-icon-close" @click="$router.push('/project/projInfo')">取消</el-button>
      </div>
    </div>

    <customer-modal ref="customer" @done="handleCustomerDone" />
  </div>
</template>

<script>
import { listCostCategories } from '@/api/project/costCategory'
import { getProjForm, saveDraft, submitForApproval } from '@/api/project/projInfo'
import { listCustomers } from '@/api/project/projCustomer'
import CustomerModal from '../components/CustomerModal'
import { formatMoney } from '@/utils/project'

export default {
  name: 'ProjInfoForm',
  components: { CustomerModal },
  data() {
    const validateEndDate = (rule, value, callback) => {
      if (!value) return callback(new Error('请选择预计竣工日期'))
      if (this.form.projInfo.planStartDate && value < this.form.projInfo.planStartDate) {
        return callback(new Error('预计竣工日期必须晚于预计开工日期'))
      }
      callback()
    }
    return {
      saving: false,
      dirty: false,
      tempNodeSeq: 0,
      customers: [],
      costCategories: [],
      form: { projInfo: {}, wbsNodes: [], allocations: [] },
      rules: {
        projName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
        customerId: [{ required: true, message: '请选择关联客户', trigger: 'change' }],
        contractAmount: [{ required: true, message: '请输入合同金额', trigger: 'change' }],
        planStartDate: [{ required: true, message: '请选择预计开工日期', trigger: 'change' }],
        planEndDate: [{ required: true, validator: validateEndDate, trigger: 'change' }]
      }
    }
  },
  computed: {
    costColumns() {
      return this.costCategories.filter(item => Number(item.parentId || 0) === 0)
    },
    nodeTotal() {
      return this.form.wbsNodes.reduce((sum, item) => sum + Number(item.nodeBudget || 0), 0)
    }
  },
  created() {
    Promise.all([this.loadCustomers(), this.loadCostCategories()]).then(() => {
      if (this.$route.query.id) {
        this.loadForm(this.$route.query.id)
      }
    })
  },
  beforeRouteLeave(to, from, next) {
    if (!this.dirty) {
      next()
      return
    }
    this.$confirm('当前页面有未保存内容，确认离开吗？', '提示', { type: 'warning' }).then(() => next()).catch(() => next(false))
  },
  methods: {
    loadForm(id) {
      return getProjForm(id).then(res => {
        const data = res.data || { projInfo: {}, wbsNodes: [], allocations: [] }
        this.form.projInfo = data.projInfo || {}
        this.form.allocations = data.allocations || []
        this.form.wbsNodes = (data.wbsNodes || []).map(node => this.decorateNode(node))
        this.dirty = false
      }).catch(() => this.$message.error('项目详情加载失败'))
    },
    loadCustomers() {
      return listCustomers({ pageNum: 1, pageSize: 500 }).then(r => {
        this.customers = r.rows || []
      }).catch(() => this.$message.error('客户列表加载失败'))
    },
    loadCostCategories() {
      return listCostCategories({ status: '0' }).then(res => {
        this.costCategories = res.data || []
      }).catch(() => this.$message.error('成本科目加载失败'))
    },
    decorateNode(node) {
      const allocations = {}
      this.costColumns.forEach(category => {
        const found = this.form.allocations.find(item => String(item.nodeId) === String(node.nodeId) && String(item.categoryId) === String(category.categoryId))
        this.$set(allocations, category.categoryId, found ? Number(found.allocationAmount || 0) : 0)
      })
      return Object.assign({}, node, { allocations, nodeBudget: this.sumAllocations(allocations) })
    },
    handleCustomerDone(customer) {
      this.loadCustomers()
      if (customer && customer.customerId) {
        this.form.projInfo.customerId = customer.customerId
        this.form.projInfo.customerName = customer.customerName
        this.markDirty()
      }
    },
    addNode() {
      this.tempNodeSeq += 1
      this.form.wbsNodes.push(this.decorateNode({ nodeId: -this.tempNodeSeq, nodeNo: '', nodeName: '', planFinishDate: '', nodeBudget: 0 }))
      this.markDirty()
    },
    removeNode(index) {
      this.form.wbsNodes.splice(index, 1)
      this.markDirty()
    },
    moveNode(index, step) {
      const targetIndex = index + step
      if (targetIndex < 0 || targetIndex >= this.form.wbsNodes.length) return
      const current = this.form.wbsNodes[index]
      this.form.wbsNodes.splice(index, 1)
      this.form.wbsNodes.splice(targetIndex, 0, current)
      this.markDirty()
    },
    nextNodeNo(index) {
      return 'ND-' + String(index + 1).padStart(3, '0')
    },
    handleAllocationInput(row) {
      row.nodeBudget = this.sumAllocations(row.allocations)
      this.markDirty()
    },
    normalizeAllocation(row, categoryId) {
      this.$set(row.allocations, categoryId, this.toValidAmount(row.allocations[categoryId]))
      row.nodeBudget = this.sumAllocations(row.allocations)
    },
    sumAllocations(allocations) {
      return Object.keys(allocations || {}).reduce((sum, key) => sum + this.toValidAmount(allocations[key]), 0)
    },
    toValidAmount(value) {
      const amount = Number(value || 0)
      if (!Number.isFinite(amount) || amount < 0) {
        return 0
      }
      return Number(amount.toFixed(2))
    },
    buildPayload() {
      const customer = this.customers.find(i => i.customerId === this.form.projInfo.customerId)
      const wbsNodes = this.form.wbsNodes.map(node => ({
        nodeId: node.nodeId,
        nodeNo: node.nodeNo,
        nodeName: node.nodeName,
        planFinishDate: node.planFinishDate,
        nodeBudget: this.sumAllocations(node.allocations),
        orderNum: node.orderNum
      }))
      const allocations = []
      this.form.wbsNodes.forEach(node => {
        this.costColumns.forEach(category => {
          allocations.push({
            nodeId: node.nodeId,
            categoryId: category.categoryId,
            categoryName: category.categoryName,
            allocationAmount: this.toValidAmount(node.allocations[category.categoryId])
          })
        })
      })
      return {
        projInfo: Object.assign({}, this.form.projInfo, {
          customerName: customer ? customer.customerName : '',
          totalBudget: this.nodeTotal
        }),
        wbsNodes,
        allocations
      }
    },
    validateNodes() {
      if (!this.form.wbsNodes.length) {
        this.$message.warning('请至少新增一个WBS节点')
        return false
      }
      const invalid = this.form.wbsNodes.find(node => !node.nodeName || !node.planFinishDate)
      if (invalid) {
        this.$message.warning('请完善WBS节点名称和预计完成日期')
        return false
      }
      return true
    },
    persistDraft() {
      return new Promise((resolve, reject) => {
        this.$refs.form.validate(valid => {
          if (!valid || !this.validateNodes()) {
            reject(new Error('invalid'))
            return
          }
          this.saving = true
          saveDraft(this.buildPayload()).then(res => {
            this.form.projInfo = res.data || this.form.projInfo
            this.dirty = false
            resolve(this.form.projInfo)
          }).catch(reject).finally(() => {
            this.saving = false
          })
        })
      })
    },
    save() {
      this.persistDraft().then(() => {
        this.$message.success('保存成功')
        if (this.form.projInfo.projId) {
          this.loadForm(this.form.projInfo.projId)
        }
      }).catch(err => {
        if (err.message !== 'invalid') {
          this.$message.error('保存失败')
        }
      })
    },
    submit() {
      const doSubmit = () => submitForApproval(this.form.projInfo.projId).then(() => {
        this.dirty = false
        this.$message.success('已提交审批')
        this.$router.push('/project/projInfo')
      }).catch(() => this.$message.error('提交审批失败'))

      if (!this.dirty) {
        doSubmit()
        return
      }

      this.persistDraft().then(() => {
        doSubmit()
      }).catch(err => {
        if (err.message !== 'invalid') {
          this.$message.error('提交前保存失败')
        }
      })
    },
    formatMoney(value) {
      return formatMoney(value)
    },
    markDirty() {
      this.dirty = true
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

.page-heading,
.form-group,
.action-bar {
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
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
}

.page-heading p {
  margin: 0;
  font-size: 13px;
  color: #8c98a8;
}

.heading-actions {
  display: flex;
  gap: 8px;
}

.form-group {
  padding: 18px 20px 6px;
  margin-bottom: 16px;
}

.form-header {
  position: relative;
  margin: 0 0 18px;
  padding-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
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

.subtable-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}

.subtable-header .form-header {
  margin-bottom: 0;
}

.subtable-summary {
  margin-right: 12px;
  font-size: 13px;
  color: #8c98a8;
}

.field-full,
.table-date,
.table-number {
  width: 100%;
}

.amount-input /deep/ input {
  text-align: center;
}

.amount-input /deep/ input::-webkit-outer-spin-button,
.amount-input /deep/ input::-webkit-inner-spin-button {
  margin: 0;
  appearance: none;
}

.amount-input /deep/ input[type='number'] {
  appearance: textfield;
}

.customer-picker {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 72px;
  column-gap: 8px;
}

.customer-picker .el-select {
  width: 100%;
}

.budget-text {
  font-weight: 600;
  color: #1f2d3d;
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
  color: #ff4d4f;
}

@media (max-width: 992px) {
  .action-bar {
    left: 20px;
  }
}

@media (max-width: 768px) {
  .page-heading,
  .heading-actions,
  .subtable-header,
  .action-bar {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }
}
</style>
