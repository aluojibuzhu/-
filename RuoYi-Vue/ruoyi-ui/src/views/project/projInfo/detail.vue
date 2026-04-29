<template>
  <div class="app-container" v-if="form.projInfo">
    <h3>{{ form.projInfo.projName }}</h3>
    <p>客户：{{ form.projInfo.customerName }} | 状态：{{ form.projInfo.status }}</p>
    <el-table :data="form.wbsNodes"><el-table-column prop="nodeNo" label="节点"/><el-table-column prop="nodeName" label="名称"/><el-table-column prop="nodeBudget" label="预算"/></el-table>
    <div style="margin-top:10px">
      <el-button type="success" @click="approveIt">通过</el-button>
      <el-button type="danger" @click="rejectIt">驳回</el-button>
    </div>
  </div>
</template>
<script>
import { getProjForm, approve, reject } from '@/api/project/projInfo'
export default {
  data() { return { form: { projInfo: null, wbsNodes: [] } } },
  created() { getProjForm(this.$route.params.id).then(r => this.form = r.data || this.form) },
  methods: {
    approveIt() { approve(this.form.projInfo.projId).then(() => this.$message.success('已通过')) },
    rejectIt() { reject(this.form.projInfo.projId, { rejectReason: '需补充信息' }).then(() => this.$message.success('已驳回')) }
  }
}
</script>
