<template>
  <report-page
    type="categoryDetail"
    title="科目成本明细表"
    desc="按一级和二级成本科目查看预算、已执行、余额和执行率，适合成本会计核对科目消耗。"
    :filters="['project', 'category']"
    :filter-columns="4"
    filter-hint="先选项目范围，再按一级成本科目过滤；未选择时展示全部科目"
    :columns="columns"
  />
</template>

<script>
import ReportPage from './components/ReportPage.vue'

export default {
  name: 'CostReportCategoryDetail',
  components: { ReportPage },
  data() {
    return {
      columns: [
        { prop: 'projName', label: '项目名称', width: 180 },
        { prop: 'rootCategoryName', label: '一级科目', width: 120 },
        { prop: 'childCategoryName', label: '二级科目', width: 120 },
        { prop: 'budgetAmount', label: '预算金额', width: 120, align: 'right', money: true },
        { prop: 'actualCost', label: '已执行金额', width: 120, align: 'right', money: true },
        { prop: 'budgetBalance', label: '余额', width: 120, align: 'right', money: true },
        { prop: 'execRate', label: '执行率', width: 100, align: 'center', rate: true }
      ]
    }
  }
}
</script>
