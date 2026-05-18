<template>
  <report-page
    type="postingFlow"
    title="入账流水明细表"
    desc="逐笔查看工时和报销入账流水，适合追踪单据来源、入账日期和审批人。"
    :filters="['project', 'billType', 'category', 'approver']"
    :filter-columns="5"
    filter-hint="支持按项目、单据类型、成本科目和审批人交叉筛选"
    :columns="columns"
  />
</template>

<script>
import ReportPage from './components/ReportPage.vue'

export default {
  name: 'CostReportPostingFlow',
  components: { ReportPage },
  data() {
    return {
      columns: [
        { prop: 'billNo', label: '单据编号', width: 150 },
        { prop: 'projName', label: '项目名称', width: 180 },
        { prop: 'billTypeName', label: '单据类型', width: 100, align: 'center' },
        { prop: 'categoryName', label: '成本科目', width: 120 },
        { prop: 'amount', label: '金额', width: 120, align: 'right', money: true },
        { prop: 'postDate', label: '入账日期', width: 120, align: 'center' },
        { prop: 'approver', label: '审批人', width: 110, align: 'center' }
      ]
    }
  }
}
</script>
