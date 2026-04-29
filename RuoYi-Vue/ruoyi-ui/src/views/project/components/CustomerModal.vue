<template>
  <el-dialog :visible.sync="open" title="新建客户" width="500px">
    <el-form :model="form" label-width="88px">
      <el-form-item label="客户名称"><el-input v-model="form.customerName" /></el-form-item>
      <el-form-item label="联系人"><el-input v-model="form.contactPerson" /></el-form-item>
      <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
    </el-form>
    <span slot="footer">
      <el-button @click="open=false">取消</el-button>
      <el-button type="primary" @click="submit">确认</el-button>
    </span>
  </el-dialog>
</template>
<script>
import { addCustomer } from '@/api/project/projCustomer'
export default {
  data() { return { open: false, form: {} } },
  methods: {
    show() { this.form = {}; this.open = true },
    submit() {
      if (!this.form.customerName) {
        this.$message.warning('客户名称不能为空')
        return
      }
      addCustomer(this.form).then(res => {
        this.$message.success('已创建')
        this.open = false
        this.$emit('done', res.data)
      }).catch(() => this.$message.error('客户创建失败'))
    }
  }
}
</script>
