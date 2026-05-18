<template>
  <report-page
    type="nodeExecution"
    title="节点预算执行表"
    desc="按WBS节点查看节点预算、已执行、余额、执行率和计划完成日期，适合项目经理跟进节点成本。"
    :filters="['project', 'node']"
    :filter-columns="4"
    filter-hint="选择项目后节点列表会自动收敛到对应项目范围"
    :columns="columns"
  />
</template>

<script>
import ReportPage from './components/ReportPage.vue'

export default {
  name: 'CostReportNodeExecution',
  components: { ReportPage },
  data() {
    return {
      columns: [
        { prop: 'projName', label: '项目名称', width: 180 },
        { prop: 'nodeNo', label: '节点编号', width: 120 },
        { prop: 'nodeName', label: '节点名称', width: 180 },
        { prop: 'nodeBudget', label: '节点预算', width: 120, align: 'right', money: true },
        { prop: 'actualCost', label: '已执行', width: 120, align: 'right', money: true },
        { prop: 'budgetBalance', label: '余额', width: 120, align: 'right', money: true },
        { prop: 'execRate', label: '执行率', width: 100, align: 'center', rate: true },
        { prop: 'planFinishDate', label: '计划完成日期', width: 120, align: 'center' }
      ]
    }
  }
}
</script>
