<template>
  <div class="app-container cost-category-page">
    <div class="page-heading">
      <div>
        <h2>成本科目</h2>
        <p>维护企业级成本科目，支持一级与二级科目配置</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd()" v-hasPermi="['project:costCategory:add']">新增科目</el-button>
    </div>

    <div class="filter-panel">
      <el-form ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="72px">
        <el-form-item label="科目名称" prop="categoryName">
          <el-input v-model="queryParams.categoryName" clearable placeholder="请输入科目名称" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" clearable placeholder="全部状态" class="filter-status">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
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
        <div>
          <span class="toolbar-title">科目列表</span>
          <span class="toolbar-count">共 {{ flatTotal }} 条</span>
        </div>
        <el-button type="info" plain icon="el-icon-sort" size="mini" @click="toggleExpandAll">展开/折叠</el-button>
      </div>

      <el-table
        ref="categoryTable"
        v-loading="loading"
        :data="categoryList"
        row-key="categoryId"
        border
        stripe
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="categoryName" label="科目名称" min-width="260" header-align="center" align="center" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="category-name-cell">{{ scope.row.categoryName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.categoryLevel === 1 ? 'primary' : 'success'">{{ scope.row.categoryLevel === 1 ? '一级' : '二级' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="100" align="center" />
        <el-table-column label="工时单价" width="130" align="center">
          <template slot-scope="scope">{{ formatMoney(scope.row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" align="center" />
        <el-table-column label="操作" width="276" align="center" fixed="right">
          <template slot-scope="scope">
            <div class="action-cell">
              <el-button v-if="scope.row.categoryLevel === 1" type="text" icon="el-icon-plus" @click="handleAdd(scope.row)" v-hasPermi="['project:costCategory:add']">新增下级</el-button>
              <el-button type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['project:costCategory:edit']">编辑</el-button>
              <el-button type="text" icon="el-icon-delete" class="danger-action" @click="handleDelete(scope.row)" v-hasPermi="['project:costCategory:remove']">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" size="small" label-width="100px">
        <el-form-item label="上级科目" prop="parentId">
          <treeselect v-model="form.parentId" :options="categoryOptions" :normalizer="normalizer" placeholder="请选择上级科目" />
        </el-form-item>
        <el-form-item label="科目名称" prop="categoryName">
          <el-input v-model="form.categoryName" maxlength="50" show-word-limit placeholder="请输入科目名称" />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="工时单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" controls-position="right" class="field-full" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { addCostCategory, delCostCategory, getCostCategory, listCostCategories, updateCostCategory } from '@/api/project/costCategory'
import { formatMoney } from '@/utils/project'

export default {
  name: 'CostCategoryIndex',
  components: { Treeselect },
  data() {
    return {
      loading: false,
      submitting: false,
      open: false,
      title: '',
      isExpandAll: true,
      categoryList: [],
      sourceList: [],
      queryParams: { categoryName: undefined, status: undefined },
      form: {},
      rules: {
        parentId: [{ required: true, message: '请选择上级科目', trigger: 'change' }],
        categoryName: [{ required: true, message: '科目名称不能为空', trigger: 'blur' }],
        orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    flatTotal() {
      return this.sourceList.length
    },
    categoryOptions() {
      const roots = this.sourceList.filter(item => Number(item.parentId || 0) === 0 && item.categoryId !== this.form.categoryId)
      return [{ categoryId: 0, categoryName: '一级科目', children: this.handleTree(roots, 'categoryId', 'parentId') }]
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.loading = true
      listCostCategories(this.queryParams).then(res => {
        this.sourceList = res.data || []
        this.categoryList = this.handleTree(this.sourceList, 'categoryId', 'parentId')
      }).catch(() => {
        this.$message.error('成本科目列表加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    normalizer(node) {
      return { id: node.categoryId, label: node.categoryName, children: node.children }
    },
    reset() {
      this.form = { categoryId: undefined, parentId: 0, categoryName: '', categoryLevel: 1, orderNum: 0, unitPrice: 0, status: '0' }
      this.resetForm('form')
    },
    handleQuery() {
      this.load()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    toggleExpandAll() {
      this.isExpandAll = !this.isExpandAll
      this.$nextTick(() => {
        this.toggleRows(this.categoryList, this.isExpandAll)
      })
    },
    toggleRows(rows, expanded) {
      rows.forEach(row => {
        this.$refs.categoryTable.toggleRowExpansion(row, expanded)
        if (row.children && row.children.length) {
          this.toggleRows(row.children, expanded)
        }
      })
    },
    handleAdd(row) {
      this.reset()
      if (row && row.categoryId) {
        this.form.parentId = row.categoryId
      }
      this.title = '新增成本科目'
      this.open = true
    },
    handleUpdate(row) {
      this.reset()
      getCostCategory(row.categoryId).then(res => {
        this.form = res.data || {}
        this.title = '编辑成本科目'
        this.open = true
      }).catch(() => this.$message.error('成本科目详情加载失败'))
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        const request = this.form.categoryId ? updateCostCategory(this.form) : addCostCategory(this.form)
        request.then(() => {
          this.$message.success('保存成功')
          this.open = false
          this.load()
        }).catch(() => {
          this.$message.error('保存失败')
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除成本科目 "' + row.categoryName + '" 吗？').then(() => delCostCategory(row.categoryId)).then(() => {
        this.$message.success('删除成功')
        this.load()
      }).catch(() => {})
    },
    formatMoney(value) {
      return formatMoney(value)
    }
  }
}
</script>

<style scoped>
.cost-category-page {
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.page-heading,
.filter-panel,
.table-panel {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  margin-bottom: 14px;
}

.page-heading h2 {
  margin: 0;
  color: #1f2d3d;
  font-size: 22px;
  font-weight: 600;
}

.page-heading p {
  margin: 8px 0 0;
  color: #8c98a8;
}

.filter-panel {
  padding: 16px 18px 0;
  margin-bottom: 14px;
}

.filter-status {
  width: 140px;
}

.field-full {
  width: 100%;
}

.table-panel {
  padding: 16px 18px 18px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.toolbar-title {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 600;
}

.toolbar-count {
  margin-left: 10px;
  color: #8c98a8;
  font-size: 13px;
}

.category-name-cell {
  display: inline-block;
  max-width: 100%;
  line-height: 1.6;
  vertical-align: middle;
}

.action-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  white-space: nowrap;
}

.action-cell /deep/ .el-button + .el-button {
  margin-left: 0;
}

.danger-action {
  color: #ff4d4f;
}

@media (max-width: 768px) {
  .page-heading,
  .table-toolbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }
}
</style>
