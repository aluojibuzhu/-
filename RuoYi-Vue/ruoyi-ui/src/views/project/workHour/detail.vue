<template>
  <div class="app-container work-hour-detail-page" v-loading="loading">
    <div class="page-heading detail-heading" v-if="form.workHour">
      <div>
        <div class="title-row">
          <h2>{{ form.workHour.whNo }}</h2>
          <el-tag size="small" :type="statusTag(form.workHour.status)">{{ statusLabel(form.workHour.status) }}</el-tag>
        </div>
        <p>{{ form.workHour.projName }} · {{ form.workHour.nodeName }}</p>
      </div>
      <div class="heading-actions">
        <template v-if="form.workHour.status === '1'">
          <el-button type="success" icon="el-icon-check" :loading="submitting" @click="approveIt" v-hasPermi="['cost:workHour:approve']">通过</el-button>
          <el-button type="danger" icon="el-icon-close" :loading="submitting" @click="rejectIt" v-hasPermi="['cost:workHour:approve']">驳回</el-button>
        </template>
        <el-button v-if="form.workHour.status === '2'" type="primary" icon="el-icon-finished" :loading="submitting" @click="postIt" v-hasPermi="['cost:workHour:post']">入账</el-button>
        <el-button icon="el-icon-back" @click="$router.push('/cost/workHour')">返回</el-button>
      </div>
    </div>

    <el-row v-if="form.workHour" :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="8" class="summary-col">
        <div class="summary-item"><span>工时数</span><strong>{{ formatHours(form.workHour.workHours) }}</strong></div>
      </el-col>
      <el-col :xs="24" :sm="8" class="summary-col">
        <div class="summary-item"><span>工时单价</span><strong>{{ formatMoney(form.workHour.unitPrice) }}</strong></div>
      </el-col>
      <el-col :xs="24" :sm="8" class="summary-col">
        <div class="summary-item"><span>工时成本</span><strong>{{ formatMoney(form.workHour.workCost) }}</strong></div>
      </el-col>
    </el-row>

    <div v-if="form.workHour" class="detail-card">
      <div class="detail-card-title">基本信息</div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">所属项目</span><span class="detail-value">{{ form.workHour.projName }}</span></div>
        <div class="detail-item"><span class="detail-label">WBS节点</span><span class="detail-value">{{ form.workHour.nodeName }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">成本科目</span><span class="detail-value">{{ form.workHour.categoryName }}</span></div>
        <div class="detail-item"><span class="detail-label">工作类型</span><span class="detail-value">{{ workTypeLabel(form.workHour.workType) }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">填报日期</span><span class="detail-value">{{ form.workHour.workDate }}</span></div>
        <div class="detail-item"><span class="detail-label">填报人</span><span class="detail-value">{{ form.workHour.createBy || '-' }}</span></div>
      </div>
    </div>

    <div v-if="form.workHour" class="detail-card">
      <div class="detail-card-title">工作说明</div>
      <div class="detail-row">
        <div class="detail-item detail-item-full"><span class="detail-label">工作内容</span><span class="detail-value">{{ form.workHour.workDesc || '-' }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item detail-item-full"><span class="detail-label">驳回原因</span><span class="detail-value">{{ form.workHour.rejectReason || '-' }}</span></div>
      </div>
    </div>

    <div v-if="form.workHour" class="detail-card">
      <div class="detail-card-title">附件列表</div>
      <div v-if="form.attachments.length" class="attachment-list">
        <el-link v-for="item in form.attachments" :key="item.attachmentId || item.filePath" :href="baseUrl + item.filePath" target="_blank" icon="el-icon-document">
          {{ fileName(item) }}
        </el-link>
      </div>
      <el-empty v-else description="暂无附件" />
    </div>

    <div v-if="form.workHour" class="detail-card">
      <div class="detail-card-title">审计轨迹</div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">创建时间</span><span class="detail-value">{{ form.workHour.createTime || '-' }}</span></div>
        <div class="detail-item"><span class="detail-label">提交时间</span><span class="detail-value">{{ form.workHour.submitTime || '-' }}</span></div>
      </div>
      <div class="detail-row">
        <div class="detail-item"><span class="detail-label">审批人</span><span class="detail-value">{{ form.workHour.approveBy || '-' }}</span></div>
        <div class="detail-item"><span class="detail-label">审批时间</span><span class="detail-value">{{ form.workHour.approveTime || '-' }}</span></div>
      </div>
    </div>
  </div>
</template>

<script>
import { approveWorkHour, getWorkHour, postWorkHour, rejectWorkHour } from '@/api/project/workHour'
import { formatHours, formatMoney, workHourStatusLabel, workHourStatusTagType } from '@/utils/project'

export default {
  name: 'WorkHourDetail',
  dicts: ['wh_work_type'],
  data() {
    return {
      loading: false,
      submitting: false,
      baseUrl: process.env.VUE_APP_BASE_API,
      form: { workHour: null, attachments: [] }
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      const whId = this.$route.params.id
      if (!whId || whId === 'undefined') {
        this.$message.warning('请先从工时列表选择详情')
        this.$router.replace('/cost/workHour')
        return
      }
      this.loading = true
      getWorkHour(whId).then(res => {
        this.form = res.data || this.form
        this.form.attachments = this.form.attachments || []
      }).catch(() => this.$message.error('工时详情加载失败')).finally(() => {
        this.loading = false
      })
    },
    approveIt() {
      this.submitting = true
      approveWorkHour(this.form.workHour.whId).then(() => {
        this.$message.success('已通过')
        this.load()
      }).catch(() => this.$message.error('审批失败')).finally(() => {
        this.submitting = false
      })
    },
    rejectIt() {
      this.$prompt('请输入驳回原因', '驳回工时单', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputValidator: value => !!value,
        inputErrorMessage: '驳回原因不能为空'
      }).then(({ value }) => {
        this.submitting = true
        return rejectWorkHour(this.form.workHour.whId, { rejectReason: value })
      }).then(() => {
        this.$message.success('已驳回')
        this.load()
      }).catch(error => {
        if (error !== 'cancel' && error !== 'close') this.$message.error('驳回失败')
      }).finally(() => {
        this.submitting = false
      })
    },
    postIt() {
      this.submitting = true
      postWorkHour(this.form.workHour.whId).then(() => {
        this.$message.success('已入账')
        this.load()
      }).catch(() => this.$message.error('入账失败')).finally(() => {
        this.submitting = false
      })
    },
    workTypeLabel(value) {
      return this.selectDictLabel(this.dict.type.wh_work_type, value) || value || '-'
    },
    fileName(item) {
      return item.originalName || item.fileName || (item.filePath || '').split('/').pop()
    },
    statusLabel(status) { return workHourStatusLabel(status) },
    statusTag(status) { return workHourStatusTagType(status) },
    formatMoney(value) { return formatMoney(value) },
    formatHours(value) { return formatHours(value) }
  }
}
</script>

<style scoped>
.work-hour-detail-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.detail-heading,
.detail-card,
.summary-item {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
}

.detail-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  margin-bottom: 16px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-row h2 {
  margin: 0;
  color: #1f2d3d;
  font-size: 22px;
  font-weight: 600;
}

.detail-heading p {
  margin: 8px 0 0;
  color: #8c98a8;
}

.heading-actions {
  display: flex;
  gap: 8px;
}

.summary-row {
  margin-bottom: 16px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 86px;
  padding: 16px 18px;
}

.summary-item span {
  display: block;
  margin-bottom: 10px;
  color: #8c98a8;
  font-size: 13px;
}

.summary-item strong {
  color: #1f2d3d;
  font-size: 24px;
  font-weight: 600;
}

.detail-card {
  padding: 18px 20px;
  margin-bottom: 16px;
}

.detail-card-title {
  position: relative;
  margin-bottom: 16px;
  padding-bottom: 10px;
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 600;
}

.detail-card-title::after {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 36px;
  height: 3px;
  content: '';
  background: #1890ff;
  border-radius: 2px;
}

.detail-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-top: 1px solid #eef2f6;
}

.detail-row:first-of-type {
  border-top: 0;
}

.detail-item {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr);
  min-height: 48px;
}

.detail-item-full {
  grid-column: 1 / -1;
}

.detail-label,
.detail-value {
  padding: 12px;
  line-height: 1.6;
}

.detail-label {
  color: #606266;
  white-space: nowrap;
  background: #f8f8f9;
}

.detail-value {
  color: #1f2d3d;
  overflow-wrap: anywhere;
}

.detail-item-full .detail-value {
  white-space: pre-wrap;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@media (max-width: 768px) {
  .detail-heading,
  .heading-actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .detail-row {
    grid-template-columns: 1fr;
  }
}
</style>
