<template>
  <div class="app-container alert-rule-page">
    <div class="page-heading">
      <div>
        <h2>预警规则配置</h2>
        <p>维护预算执行率、单笔金额、预算余额、逾期停滞等预警阈值和适用范围</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd">新增规则</el-button>
    </div>

    <div class="filter-panel">
      <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" label-width="78px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="queryParams.ruleName" clearable placeholder="输入规则名称" @keyup.enter.native="handleQuery" />
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
        <el-form-item label="启用状态" prop="enabled">
          <el-select v-model="queryParams.enabled" clearable placeholder="全部状态" class="filter-select">
            <el-option label="启用" value="1" />
            <el-option label="停用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <div class="table-toolbar">
        <div><span class="toolbar-title">规则列表</span><span class="toolbar-count">共 {{ total }} 条</span></div>
      </div>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="ruleName" label="规则名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="规则类型" width="120" align="center">
          <template slot-scope="scope">{{ ruleTypeLabel(scope.row.ruleType) }}</template>
        </el-table-column>
        <el-table-column label="级别" width="104" align="center">
          <template slot-scope="scope"><el-tag size="small" :type="levelTag(scope.row.alertLevel)">{{ levelLabel(scope.row.alertLevel) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="阈值" width="130" align="center">
          <template slot-scope="scope">{{ scope.row.compareOperator }} {{ scope.row.thresholdValue }}</template>
        </el-table-column>
        <el-table-column label="范围" width="120" align="center">
          <template slot-scope="scope">{{ scopeLabel(scope.row.scopeType) }}</template>
        </el-table-column>
        <el-table-column label="通知" width="140" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.notifyEnabled === '1' ? '启用' : '停用' }}</span>
            <span class="muted"> / {{ scope.row.notifySilenceHours || 0 }}h</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.enabled" active-value="1" inactive-value="0" @change="handleToggle(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" align="center" />
        <el-table-column label="操作" width="170" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
        <template slot="empty">
          <el-empty description="暂无预警规则" />
        </template>
      </el-table>
      <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="620px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="108px">
        <el-row :gutter="14">
          <el-col :span="24">
            <el-form-item label="规则名称" prop="ruleName">
              <el-input v-model="form.ruleName" maxlength="100" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则类型" prop="ruleType">
              <el-select v-model="form.ruleType" placeholder="选择规则类型" class="full-width">
                <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预警级别" prop="alertLevel">
              <el-select v-model="form.alertLevel" placeholder="选择级别" class="full-width">
                <el-option v-for="item in levelOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="比较符" prop="compareOperator">
              <el-select v-model="form.compareOperator" class="full-width">
                <el-option label="大于等于" value=">=" />
                <el-option label="大于" value=">" />
                <el-option label="小于等于" value="<=" />
                <el-option label="小于" value="<" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="阈值" prop="thresholdValue">
              <el-input-number v-model="form.thresholdValue" :min="0" :precision="2" :step="1" class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用范围" prop="scopeType">
              <el-select v-model="form.scopeType" class="full-width">
                <el-option label="全局" value="0" />
                <el-option label="指定项目" value="1" />
                <el-option label="指定科目" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="范围值">
              <el-input v-model="form.scopeValue" placeholder="多个ID用英文逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系统通知">
              <el-switch v-model="form.notifyEnabled" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="静默小时">
              <el-input-number v-model="form.notifySilenceHours" :min="0" :max="720" :step="1" class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addAlertRule, delAlertRule, getAlertRule, listAlertRules, toggleAlertRule, updateAlertRule } from '@/api/project/alertRule'
import { ALERT_LEVEL_OPTIONS, ALERT_RULE_TYPE_OPTIONS, alertLevelLabel, alertLevelTagType, alertRuleTypeLabel } from '@/utils/project'

export default {
  name: 'AlertRule',
  data() {
    return {
      loading: false,
      open: false,
      title: '',
      list: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, ruleName: null, ruleType: null, alertLevel: null, enabled: null },
      form: {},
      levelOptions: ALERT_LEVEL_OPTIONS,
      ruleTypeOptions: ALERT_RULE_TYPE_OPTIONS,
      rules: {
        ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
        ruleType: [{ required: true, message: '规则类型不能为空', trigger: 'change' }],
        alertLevel: [{ required: true, message: '预警级别不能为空', trigger: 'change' }],
        compareOperator: [{ required: true, message: '比较符不能为空', trigger: 'change' }],
        thresholdValue: [{ required: true, message: '阈值不能为空', trigger: 'blur' }],
        scopeType: [{ required: true, message: '适用范围不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    levelLabel: alertLevelLabel,
    levelTag: alertLevelTagType,
    ruleTypeLabel: alertRuleTypeLabel,
    getList() {
      this.loading = true
      listAlertRules(this.queryParams).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
      }).catch(() => {
        this.$message.error('预警规则加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    reset() {
      this.form = { ruleId: null, ruleName: null, ruleType: 'EXEC_RATE', alertLevel: '1', thresholdValue: 80, compareOperator: '>=', scopeType: '0', scopeValue: null, notifyEnabled: '1', notifyChannels: 'SYSTEM', notifyRoles: null, notifySilenceHours: 24, enabled: '1', remark: null }
      this.resetForm('form')
    },
    handleAdd() {
      this.reset()
      this.title = '新增预警规则'
      this.open = true
    },
    handleUpdate(row) {
      this.reset()
      getAlertRule(row.ruleId).then(res => {
        this.form = res.data
        this.title = '修改预警规则'
        this.open = true
      }).catch(() => {
        this.$message.error('预警规则详情加载失败')
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const api = this.form.ruleId ? updateAlertRule : addAlertRule
        api(this.form).then(() => {
          this.$message.success('保存成功')
          this.open = false
          this.getList()
        }).catch(() => {
          this.$message.error('保存失败')
        })
      })
    },
    handleToggle(row) {
      const previous = row.enabled === '1' ? '0' : '1'
      toggleAlertRule(row.ruleId, row.enabled).then(() => {
        this.$message.success(row.enabled === '1' ? '已启用' : '已停用')
      }).catch(() => {
        row.enabled = previous
        this.$message.error('状态更新失败')
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除规则 "' + row.ruleName + '" 吗？').then(() => delAlertRule(row.ruleId)).then(() => {
        this.$message.success('删除成功')
        this.getList()
      }).catch(err => {
        if (err !== 'cancel' && err !== 'close') {
          this.$message.error('删除失败')
        }
      })
    },
    scopeLabel(scopeType) {
      return ({ '0': '全局', '1': '指定项目', '2': '指定科目' })[scopeType] || scopeType
    }
  }
}
</script>

<style scoped>
.alert-rule-page { background: #f5f7fa; min-height: calc(100vh - 84px); }
.page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 16px; }
.page-heading h2 { margin: 0 0 6px; color: #1f2d3d; font-size: 22px; font-weight: 600; }
.page-heading p { margin: 0; color: #8c98a8; font-size: 13px; }
.filter-panel, .table-panel { background: #fff; border: 1px solid #e6ebf2; border-radius: 6px; padding: 18px 20px; margin-bottom: 16px; }
.filter-select { width: 132px; }
.filter-actions { margin-left: 4px; }
.table-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.toolbar-title { color: #1f2d3d; font-weight: 600; margin-right: 8px; }
.toolbar-count { color: #8c98a8; font-size: 13px; }
.muted { color: #8c98a8; }
.full-width { width: 100%; }
::v-deep .el-input-number .el-input__inner { text-align: left; }
</style>
