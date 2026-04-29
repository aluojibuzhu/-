<template>
  <div class="app-container">
    <el-button type="primary" @click="$router.push('/project/projInfo/form')">新建项目</el-button>
    <el-table :data="list" style="margin-top:12px">
      <el-table-column prop="projNo" label="项目编号" />
      <el-table-column prop="projName" label="项目名称" />
      <el-table-column prop="customerName" label="客户" />
      <el-table-column prop="totalBudget" label="总预算" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="text" @click="$router.push('/project/projInfo/detail/'+scope.row.projId)">详情</el-button>
          <el-button type="text" @click="submit(scope.row)" v-if="scope.row.status==='0'">提交</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script>
import { listProjInfos, submitForApproval } from '@/api/project/projInfo'
export default {
  data() { return { list: [] } },
  created() { this.load() },
  methods: {
    load() { listProjInfos({}).then(res => this.list = res.rows || []) },
    submit(row) { submitForApproval(row.projId).then(() => { this.$message.success('已提交'); this.load() }) }
  }
}
</script>

