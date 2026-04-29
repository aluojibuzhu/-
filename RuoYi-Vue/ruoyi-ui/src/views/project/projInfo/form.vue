<template>
  <div class="app-container">
    <el-form :model="form.projInfo" label-width="110px">
      <el-form-item label="项目名称"><el-input v-model="form.projInfo.projName" /></el-form-item>
      <el-form-item label="关联客户">
        <el-select v-model="form.projInfo.customerId" filterable style="width:240px">
          <el-option v-for="c in customers" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
        </el-select>
        <el-button @click="$refs.customer.show()">新建客户</el-button>
      </el-form-item>
    </el-form>
    <el-button type="primary" @click="addNode">新增节点</el-button>
    <el-table :data="form.wbsNodes" style="margin-top:10px">
      <el-table-column prop="nodeNo" label="节点编号" width="120" />
      <el-table-column label="节点名称"><template slot-scope="s"><el-input v-model="s.row.nodeName"/></template></el-table-column>
      <el-table-column label="节点预算"><template slot-scope="s"><el-input-number v-model="s.row.nodeBudget" :min="0"/></template></el-table-column>
    </el-table>
    <div style="margin-top:14px">
      <el-button type="primary" @click="save">保存草稿</el-button>
    </div>
    <customer-modal ref="customer" @done="loadCustomers" />
  </div>
</template>
<script>
import CustomerModal from '../components/CustomerModal'
import { saveDraft } from '@/api/project/projInfo'
import { listCustomers } from '@/api/project/projCustomer'
export default {
  components: { CustomerModal },
  data() { return { customers: [], form: { projInfo: {}, wbsNodes: [], allocations: [] } } },
  created() { this.loadCustomers() },
  methods: {
    loadCustomers() { listCustomers({ pageNum: 1, pageSize: 200 }).then(r => this.customers = r.rows || []) },
    addNode() { this.form.wbsNodes.push({ nodeNo: 'ND-' + String(this.form.wbsNodes.length + 1).padStart(3, '0'), nodeName: '', nodeBudget: 0 }) },
    save() {
      const c = this.customers.find(i => i.customerId === this.form.projInfo.customerId)
      this.form.projInfo.customerName = c ? c.customerName : ''
      saveDraft(this.form).then(() => this.$message.success('保存成功'))
    }
  }
}
</script>

