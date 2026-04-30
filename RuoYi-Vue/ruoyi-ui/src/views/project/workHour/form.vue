<template>
  <div class="app-container work-hour-form-page">
    <div class="page-heading">
      <div>
        <h2>{{ form.workHour.whId ? '编辑工时填报' : '新建工时填报' }}</h2>
        <p>关联项目、WBS节点与人工费科目，自动计算本次工时成本</p>
      </div>
      <div class="heading-actions">
        <el-button icon="el-icon-back" @click="$router.push('/cost/workHour')">返回列表</el-button>
        <el-button type="primary" icon="el-icon-document-checked" :loading="saving" @click="save">保存草稿</el-button>
      </div>
    </div>

    <div class="form-body">
      <div class="form-group">
        <h3 class="form-header">工时信息</h3>
        <el-form ref="form" :model="form.workHour" :rules="rules" size="small" label-width="100px">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="工时编号">
                <el-input v-model="form.workHour.whNo" disabled placeholder="保存后自动生成" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="所属项目" prop="projId">
                <el-select v-model="form.workHour.projId" filterable clearable class="field-full" placeholder="请选择已立项项目" @change="handleProjectChange">
                  <el-option v-for="item in projects" :key="item.projId" :label="item.projName" :value="item.projId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="WBS节点" prop="nodeId">
                <el-select v-model="form.workHour.nodeId" filterable clearable class="field-full" placeholder="请选择WBS节点" @change="handleNodeChange">
                  <el-option v-for="item in nodes" :key="item.nodeId" :label="item.nodeName" :value="item.nodeId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="成本科目" prop="categoryId">
                <el-select v-model="form.workHour.categoryId" filterable clearable class="field-full" placeholder="请选择人工费类科目" @change="handleCategoryChange">
                  <el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="工作类型" prop="workType">
                <el-select v-model="form.workHour.workType" clearable class="field-full" placeholder="请选择工作类型" @change="markDirty">
                  <el-option v-for="dict in dict.type.wh_work_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="填报日期" prop="workDate">
                <el-date-picker v-model="form.workHour.workDate" value-format="yyyy-MM-dd" type="date" class="field-full" placeholder="选择日期" @change="markDirty" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="工时数" prop="workHours">
                <el-input-number v-model="form.workHour.workHours" :min="0.5" :step="0.5" :precision="1" controls-position="right" class="field-full" @change="recalculate" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="工时单价">
                <el-input :value="formatMoney(form.workHour.unitPrice)" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :lg="8">
              <el-form-item label="工时成本">
                <el-input :value="formatMoney(workCost)" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="工作内容" prop="workDesc">
                <el-input v-model="form.workHour.workDesc" clearable type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入本次工作内容" @input="markDirty" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <div class="form-group">
        <h3 class="form-header">附件上传</h3>
        <file-upload v-model="attachmentPaths" :file-type="['png','jpg','jpeg','pdf']" :file-size="10" :limit="5" @input="markDirty" />
      </div>
    </div>

    <div class="action-bar">
      <div class="budget-total">工时成本：<strong>{{ formatMoney(workCost) }}</strong></div>
      <div>
        <el-button type="primary" icon="el-icon-document-checked" :loading="saving" @click="save">保存草稿</el-button>
        <el-button type="success" icon="el-icon-upload2" :disabled="!form.workHour.whId" @click="submit">提交审批</el-button>
        <el-button icon="el-icon-close" @click="$router.push('/cost/workHour')">取消</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { listCostCategories } from '@/api/project/costCategory'
import { listProjInfos } from '@/api/project/projInfo'
import { getWorkHour, listWorkHourWbsNodes, saveWorkHourDraft, submitWorkHour } from '@/api/project/workHour'
import { formatMoney } from '@/utils/project'

export default {
  name: 'WorkHourForm',
  dicts: ['wh_work_type'],
  data() {
    const validateHalfHour = (rule, value, callback) => {
      if (!value || Number(value) <= 0) return callback(new Error('工时数必须大于0'))
      if ((Number(value) * 10) % 5 !== 0) return callback(new Error('工时数必须为0.5的整数倍'))
      callback()
    }
    return {
      saving: false,
      dirty: false,
      projects: [],
      nodes: [],
      categories: [],
      attachmentPaths: '',
      form: { workHour: { workHours: 0.5, unitPrice: 0, workCost: 0, status: '0' }, attachments: [] },
      rules: {
        projId: [{ required: true, message: '请选择所属项目', trigger: 'change' }],
        nodeId: [{ required: true, message: '请选择WBS节点', trigger: 'change' }],
        categoryId: [{ required: true, message: '请选择成本科目', trigger: 'change' }],
        workType: [{ required: true, message: '请选择工作类型', trigger: 'change' }],
        workDate: [{ required: true, message: '请选择填报日期', trigger: 'change' }],
        workHours: [{ required: true, validator: validateHalfHour, trigger: 'change' }],
        workDesc: [{ required: true, message: '请输入工作内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    workCost() {
      return Number(this.form.workHour.workHours || 0) * Number(this.form.workHour.unitPrice || 0)
    }
  },
  created() {
    Promise.all([this.loadProjects(), this.loadCategories()]).then(() => {
      if (this.$route.query.id) {
        this.loadForm(this.$route.query.id)
      }
    })
  },
  beforeRouteLeave(to, from, next) {
    if (!this.dirty) {
      next()
      return
    }
    this.$confirm('当前页面有未保存内容，确认离开吗？', '提示', { type: 'warning' }).then(() => next()).catch(() => next(false))
  },
  methods: {
    loadProjects() {
      return Promise.all([
        listProjInfos({ pageNum: 1, pageSize: 100, status: '2' }),
        listProjInfos({ pageNum: 1, pageSize: 100, status: '4' })
      ]).then(([approved, running]) => {
        this.projects = [].concat(approved.rows || [], running.rows || [])
      }).catch(() => this.$message.error('项目列表加载失败'))
    },
    loadCategories() {
      return listCostCategories({ status: '0' }).then(res => {
        const rows = res.data || []
        const labor = rows.find(item => item.parentId === 0 && item.categoryName === '人工费')
        this.categories = labor ? rows.filter(item => item.parentId === labor.categoryId) : []
      }).catch(() => this.$message.error('人工费科目加载失败'))
    },
    loadForm(id) {
      return getWorkHour(id).then(res => {
        const data = res.data || {}
        this.form = { workHour: data.workHour || {}, attachments: data.attachments || [] }
        this.attachmentPaths = this.form.attachments.map(item => item.filePath).join(',')
        if (this.form.workHour.projId) {
          this.loadNodes(this.form.workHour.projId)
        }
        this.dirty = false
      }).catch(() => this.$message.error('工时详情加载失败'))
    },
    loadNodes(projId) {
      return listWorkHourWbsNodes(projId).then(res => {
        this.nodes = res.data || []
      }).catch(() => this.$message.error('WBS节点加载失败'))
    },
    handleProjectChange(projId) {
      this.form.workHour.nodeId = undefined
      this.form.workHour.nodeName = ''
      this.nodes = []
      if (projId) {
        const project = this.projects.find(item => item.projId === projId)
        this.form.workHour.projName = project ? project.projName : ''
        this.loadNodes(projId)
      }
      this.markDirty()
    },
    handleNodeChange(nodeId) {
      const node = this.nodes.find(item => item.nodeId === nodeId)
      this.form.workHour.nodeName = node ? node.nodeName : ''
      this.markDirty()
    },
    handleCategoryChange(categoryId) {
      const category = this.categories.find(item => item.categoryId === categoryId)
      this.form.workHour.categoryName = category ? category.categoryName : ''
      this.form.workHour.unitPrice = category ? Number(category.unitPrice || 0) : 0
      this.recalculate()
    },
    recalculate() {
      this.form.workHour.workCost = this.workCost
      this.markDirty()
    },
    buildPayload() {
      const paths = this.attachmentPaths ? this.attachmentPaths.split(',').filter(Boolean) : []
      return {
        workHour: Object.assign({}, this.form.workHour, { workCost: this.workCost }),
        attachments: paths.map(path => ({ fileName: path, originalName: path.split('/').pop(), filePath: path }))
      }
    },
    persistDraft() {
      return new Promise((resolve, reject) => {
        this.$refs.form.validate(valid => {
          if (!valid) {
            reject(new Error('invalid'))
            return
          }
          this.saving = true
          saveWorkHourDraft(this.buildPayload()).then(res => {
            this.form.workHour = res.data || this.form.workHour
            this.dirty = false
            resolve(this.form.workHour)
          }).catch(reject).finally(() => {
            this.saving = false
          })
        })
      })
    },
    save() {
      this.persistDraft().then(() => {
        this.$message.success('保存成功')
      }).catch(err => {
        if (err.message !== 'invalid') this.$message.error('保存失败')
      })
    },
    submit() {
      const doSubmit = () => submitWorkHour(this.form.workHour.whId).then(() => {
        this.dirty = false
        this.$message.success('已提交审批')
        this.$router.push('/cost/workHour')
      }).catch(() => this.$message.error('提交审批失败'))
      if (!this.dirty) {
        doSubmit()
        return
      }
      this.persistDraft().then(() => doSubmit()).catch(err => {
        if (err.message !== 'invalid') this.$message.error('提交前保存失败')
      })
    },
    markDirty() {
      this.dirty = true
    },
    formatMoney(value) {
      return formatMoney(value)
    }
  }
}
</script>

<style scoped>
.work-hour-form-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
  padding-bottom: 86px;
}

.page-heading,
.form-group,
.action-bar {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  margin-bottom: 16px;
}

.page-heading h2 {
  margin: 0 0 6px;
  color: #1f2d3d;
  font-size: 22px;
  font-weight: 600;
}

.page-heading p {
  margin: 0;
  color: #8c98a8;
  font-size: 13px;
}

.heading-actions {
  display: flex;
  gap: 8px;
}

.form-group {
  padding: 18px 20px 16px;
  margin-bottom: 16px;
}

.form-header {
  position: relative;
  margin: 0 0 18px;
  padding-bottom: 10px;
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 600;
}

.form-header::after {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 36px;
  height: 3px;
  content: '';
  background: #1890ff;
  border-radius: 2px;
}

.field-full {
  width: 100%;
}

.action-bar {
  position: fixed;
  right: 20px;
  bottom: 18px;
  left: 220px;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 56px;
  padding: 10px 18px;
}

.budget-total {
  color: #606266;
  font-size: 14px;
}

.budget-total strong {
  color: #1f2d3d;
  font-size: 18px;
}

@media (max-width: 992px) {
  .action-bar {
    left: 20px;
  }
}

@media (max-width: 768px) {
  .page-heading,
  .heading-actions,
  .action-bar {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }
}
</style>
