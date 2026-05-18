<template>
  <report-page
    type="projectSummary"
    title="项目资金汇总表"
    desc="按项目维度汇总合同金额、预算、已执行成本、余额和利润空间，适合管理层快速看全局。"
    :filters="['project']"
    :filter-columns="3"
    filter-hint="按项目和入账时间统计资金执行情况，默认展示本月发生额"
    :columns="columns"
  />
</template>

<script>
import ReportPage from './components/ReportPage.vue'

export default {
  name: 'CostReportProjectSummary',
  components: { ReportPage },
  data() {
    return {
      columns: [
        { prop: 'projNo', label: '项目编号', width: 140 },
        { prop: 'projName', label: '项目名称', width: 180 },
        { prop: 'contractAmount', label: '合同金额', width: 120, align: 'right', money: true },
        { prop: 'totalBudget', label: '预算总额', width: 120, align: 'right', money: true },
        { prop: 'actualCost', label: '已执行', width: 120, align: 'right', money: true },
        { prop: 'budgetBalance', label: '余额', width: 120, align: 'right', money: true },
        { prop: 'profitMargin', label: '利润空间', width: 120, align: 'right', money: true, sign: true },
        { prop: 'execRate', label: '执行率', width: 100, align: 'center', rate: true }
      ]
    }
  }
}
</script>
