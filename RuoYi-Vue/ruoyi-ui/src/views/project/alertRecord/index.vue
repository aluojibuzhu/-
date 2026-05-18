<template>
  <div class="app-container alert-record-page">
    <div class="page-heading">
      <div>
        <h2>预警中心</h2>
        <p>集中查看和处理预算执行、单笔入账、余额不足、逾期停滞等预警记录</p>
      </div>
    </div>

    <div class="filter-panel">
      <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" label-width="78px">
        <el-form-item label="预警编号" prop="alertNo">
          <el-input v-model="queryParams.alertNo" clearable placeholder="输入预警编号" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="项目名称" prop="projName">
          <el-input v-model="queryParams.projName" clearable placeholder="输入项目名称" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="queryParams.ruleType" clearable placeholder="全部类型" class="filter-select">
            <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警级别" prop="alertLevel">
          <el-select v-model="queryParams.alertLevel" clearable placeholder="全部级别" class="filter-select">
            <el-option v-for="item in levelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" class="filter-select">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发日期">
          <el-date-picker v-model="dateRange" type="daterange" value-format="yyyy-MM-dd" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <div class="table-toolbar">
        <div><span class="toolbar-title">预警记录</span><span class="toolbar-count">共 {{ total }} 条</span></div>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="alertNo" label="预警编号" width="160" align="center" />
        <el-table-column label="级别" width="104" align="center">
          <template slot-scope="scope"><el-tag size="small" :type="levelTag(scope.row.alertLevel)">{{ levelLabel(scope.row.alertLevel) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="规则类型" width="120" align="center">
          <template slot-scope="scope">{{ ruleTypeLabel(scope.row.ruleType) }}</template>
        </el-table-column>
        <el-table-column prop="projName" label="项目名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="conditionDesc" label="触发条件" min-width="210" show-overflow-tooltip />
        <el-table-column label="预算快照" width="170" align="center">
          <template slot-scope="scope">
            <div class="money-cell">预算 {{ formatMoney(scope.row.totalBudget) }}</div>
            <div class="money-cell muted">已用 {{ formatMoney(scope.row.actualCost) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope"><el-tag size="small" :type="statusTag(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="触发时间" width="160" align="center" />
        <el-table-column label="操作" width="360" align="center" class-name="alert-action-column">
          <template slot-scope="scope">
            <div class="action-cell">
              <el-button type="text" icon="el-icon-view" :disabled="submitting" @click="openDetail(scope.row)">详情</el-button>
              <el-button v-if="canHandle(scope.row)" type="text" icon="el-icon-check" :loading="submitting" :disabled="submitting" @click="handleAction(scope.row, 'confirm')">确认</el-button>
              <el-button v-if="canHandle(scope.row)" type="text" icon="el-icon-position" :loading="submitting" :disabled="submitting" @click="handleAction(scope.row, 'follow')">跟进</el-button>
              <el-button v-if="canHandle(scope.row)" type="text" icon="el-icon-remove-outline" :loading="submitting" :disabled="submitting" @click="handleAction(scope.row, 'ignore')">忽略</el-button>
              <el-button v-if="scope.row.status !== '4'" type="text" icon="el-icon-circle-close" :loading="submitting" :disabled="submitting" @click="handleAction(scope.row, 'close')">关闭</el-button>
            </div>
          </template>
        </el-table-column>
        <template slot="empty">
          <el-empty description="暂无预警记录" />
        </template>
      </el-table>
      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-drawer :visible.sync="drawerOpen" title="预警详情" size="520px" custom-class="alert-drawer">
      <div v-if="detail" class="drawer-body">
        <div class="detail-title">
          <strong>{{ detail.alertNo }}</strong>
          <el-tag size="small" :type="levelTag(detail.alertLevel)">{{ levelLabel(detail.alertLevel) }}</el-tag>
        </div>
        <div class="info-grid">
          <span>项目名称</span><strong>{{ detail.projName || '-' }}</strong>
          <span>规则类型</span><strong>{{ ruleTypeLabel(detail.ruleType) }}</strong>
          <span>触发条件</span><strong>{{ detail.conditionDesc || '-' }}</strong>
          <span>触发单据</span><strong>{{ detail.triggerBillNo || '-' }}</strong>
          <span>预算总额</span><strong>{{ formatMoney(detail.totalBudget) }}</strong>
          <span>实际成本</span><strong>{{ formatMoney(detail.actualCost) }}</strong>
          <span>预算余额</span><strong>{{ formatMoney(detail.budgetBalance) }}</strong>
          <span>当前状态</span><strong>{{ statusLabel(detail.status) }}</strong>
        </div>
        <div class="drawer-section">
          <div class="section-title">处理轨迹</div>
          <div class="timeline">
            <div v-for="item in logs" :key="item.logId" class="timeline-item">
              <div class="dot"></div>
              <div>
                <div class="timeline-head"><strong>{{ actionLabel(item.actionType) }}</strong><span>{{ item.actionTime }}</span></div>
                <p>{{ item.actionRemark || '无备注' }}</p>
              </div>
            </div>
            <el-empty v-if="!logs.length" description="暂无处理记录" />
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { closeAlert, confirmAlert, followAlert, getAlertRecord, ignoreAlert, listAlertLogs, listAlertRecords } from '@/api/project/alertRecord'
import { ALERT_LEVEL_OPTIONS, ALERT_RULE_TYPE_OPTIONS, ALERT_STATUS_OPTIONS, alertLevelLabel, alertLevelTagType, alertRuleTypeLabel, alertStatusLabel, alertStatusTagType, formatMoney } from '@/utils/project'

export default {
  name: 'AlertRecord',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      dateRange: [],
      drawerOpen: false,
      detail: null,
      logs: [],
      submitting: false,
      queryParams: { pageNum: 1, pageSize: 10, alertNo: null, projName: null, ruleType: null, alertLevel: null, status: null, beginTime: null, endTime: null },
      levelOptions: ALERT_LEVEL_OPTIONS,
      statusOptions: ALERT_STATUS_OPTIONS,
      ruleTypeOptions: ALERT_RULE_TYPE_OPTIONS
    }
  },
  created() {
    this.getList()
  },
  methods: {
    formatMoney,
    levelLabel: alertLevelLabel,
    levelTag: alertLevelTagType,
    statusLabel: alertStatusLabel,
    statusTag: alertStatusTagType,
    ruleTypeLabel: alertRuleTypeLabel,
    getList() {
      this.loading = true
      this.queryParams.beginTime = this.dateRange && this.dateRange.length ? this.dateRange[0] : null
      this.queryParams.endTime = this.dateRange && this.dateRange.length ? this.dateRange[1] : null
      listAlertRecords(this.queryParams).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
      }).catch(() => {
        this.$message.error('预警记录加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    openDetail(row) {
      Promise.all([getAlertRecord(row.alertId), listAlertLogs(row.alertId)]).then(res => {
        this.detail = res[0].data
        this.logs = res[1].data || []
        this.drawerOpen = true
      }).catch(() => {
        this.$message.error('预警详情加载失败')
      })
    },
    canHandle(row) {
      return row.status === '0' || row.status === '3'
    },
    handleAction(row, action) {
      if (this.submitting) {
        return
      }
      const title = ({ confirm: '确认预警', follow: '标记跟进', ignore: '忽略预警', close: '关闭预警' })[action]
      this.submitting = true
      this.$prompt('请输入处理备注', title, { inputType: 'textarea', inputPlaceholder: '说明处理结论或后续动作' }).then(({ value }) => {
        const data = action === 'close' ? { closeRemark: value } : { handleRemark: value }
        const api = { confirm: confirmAlert, follow: followAlert, ignore: ignoreAlert, close: closeAlert }[action]
        return api(row.alertId, data)
      }).then(() => {
        this.$message.success('处理成功')
        this.getList()
        if (this.drawerOpen && this.detail && this.detail.alertId === row.alertId) {
          this.openDetail(row)
        }
      }).catch(err => {
        if (err !== 'cancel' && err !== 'close') {
          this.$message.error('处理失败')
        }
      }).finally(() => {
        this.submitting = false
      })
    },
    actionLabel(type) {
      return ({ TRIGGER: '触发预警', NOTIFY: '系统通知', CONFIRM: '确认', IGNORE: '忽略', FOLLOW: '跟进', CLOSE: '关闭' })[type] || type
    }
  }
}
</script>

<style scoped>
.alert-record-page { background: #f5f7fa; min-height: calc(100vh - 84px); }
.page-heading { margin-bottom: 16px; }
.page-heading h2 { margin: 0 0 6px; color: #1f2d3d; font-size: 22px; font-weight: 600; }
.page-heading p { margin: 0; color: #8c98a8; font-size: 13px; }
.filter-panel, .table-panel { background: #fff; border: 1px solid #e6ebf2; border-radius: 6px; padding: 18px 20px; margin-bottom: 16px; }
.filter-select { width: 132px; }
.filter-actions { margin-left: 4px; }
.table-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.toolbar-title { color: #1f2d3d; font-weight: 600; margin-right: 8px; }
.toolbar-count { color: #8c98a8; font-size: 13px; }
.action-cell { display: flex; align-items: center; justify-content: center; gap: 12px; white-space: nowrap; }
.action-cell ::v-deep .el-button { margin-left: 0; padding: 0; line-height: 20px; }
.money-cell { line-height: 20px; }
.money-cell.muted { color: #8c98a8; font-size: 12px; }
.drawer-body { padding: 0 24px 24px; }
.detail-title { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; color: #1f2d3d; font-size: 18px; }
.info-grid { display: grid; grid-template-columns: 96px 1fr; gap: 12px 16px; padding: 16px; background: #f8fafc; border: 1px solid #e6ebf2; border-radius: 6px; }
.info-grid span { color: #8c98a8; }
.info-grid strong { color: #1f2d3d; font-weight: 600; word-break: break-word; }
.drawer-section { margin-top: 18px; }
.section-title { color: #1f2d3d; font-weight: 600; margin-bottom: 12px; }
.timeline-item { position: relative; display: grid; grid-template-columns: 18px 1fr; gap: 10px; padding-bottom: 16px; }
.timeline-item:before { content: ''; position: absolute; left: 6px; top: 16px; bottom: 0; width: 1px; background: #dbe3ef; }
.timeline-item:last-child:before { display: none; }
.dot { width: 13px; height: 13px; margin-top: 3px; border-radius: 50%; background: #1890ff; border: 3px solid #dbeafe; }
.timeline-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; color: #1f2d3d; }
.timeline-head span { color: #8c98a8; font-size: 12px; white-space: nowrap; }
.timeline-item p { margin: 6px 0 0; color: #8c98a8; line-height: 20px; }
</style>
