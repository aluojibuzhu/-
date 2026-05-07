# 前端规范设计文档

| 版本号 | 日期 | 作者 | 说明 |
|--------|------|------|------|
| V1.0 | 2026-04-29 | 前端架构 | 基于若依框架的B端成本管理系统前端规范 |

---

## 设计决策汇总

| 维度 | 决定 |
|------|------|
| 覆盖范围 | 视觉主题 + 组件使用 + 页面布局 + 编码风格 |
| 视觉风格 | 现代轻量风（明亮白色底色、大留白、圆角卡片） |
| 主色调 | 科技蓝 #1890ff（沿用若依默认） |
| 侧边栏 | 浅色（`sideTheme: 'theme-light'`） |
| 表格密度 | 舒适密度（默认行高 48px，斑马纹辅助） |

---

## 1. 视觉主题规范

### 1.1 配色体系

#### 主色

```scss
$color-primary:   #1890ff;  // 主色 — 按钮、链接、选中态
$color-primary-l: #40a9ff;  // 主色浅 — hover 态
$color-primary-d: #096dd9;  // 主色深 — active 态
```

#### 功能色

```scss
$color-success: #52c41a;  // 成功/通过/已入账
$color-warning: #faad14;  // 警告/审批中/黄色预警
$color-danger:  #ff4d4f;  // 危险/驳回/红色预警
$color-info:    #909399;  // 信息/草稿/中性提示
$color-orange:  #fa8c16;  // 橙色预警专用
```

#### 中性色（文字层级）

```scss
$text-primary:   #1f2d3d;  // 标题、正文
$text-regular:   #606266;  // 常规文字
$text-secondary: #8c98a8;  // 辅助说明
$text-placeholder: #c0c4cc; // 占位符
```

#### 背景色

```scss
$bg-page:        #f5f7fa;  // 页面底色
$bg-card:        #ffffff;  // 卡片/面板底色
$bg-table-header: #f8f8f9; // 表格表头
$bg-stripe:      #fafbfc;  // 表格斑马纹
$bg-sidebar:     #ffffff;  // 浅色侧边栏
```

### 1.2 字体排版

| 层级 | 字号 | 行高 | 字重 | 用途 |
|------|------|------|------|------|
| H2 页面标题 | 22px | 1.4 | 600 | 每个页面的主标题 |
| H3 区块标题 | 16px | 1.4 | 600 | 卡片内分区标题 |
| 正文 | 14px | 1.6 | 400 | 表格内容、表单标签 |
| 辅助文字 | 13px | 1.5 | 400 | 描述说明、时间戳 |
| 小字 | 12px | 1.5 | 400 | 标签、角标、计数 |

字体栈：`-apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif`

### 1.3 间距系统

基于 4px 步进：

| Token | 值 | 用途 |
|-------|-----|------|
| xs | 4px | 图标与文字间距 |
| sm | 8px | 表单内边距、标签间距 |
| md | 12px | 卡片内边距（紧凑） |
| base | 16px | 标准卡片内边距、表格单元格 |
| lg | 20px | 页面区块间距 |
| xl | 24px | 页面标题下间距 |
| 2xl | 32px | 大区块分隔 |

### 1.4 圆角与阴影

| 元素 | 圆角 | 说明 |
|------|------|------|
| 卡片/面板 | 6px | `.filter-panel`, `.table-panel` |
| 按钮 | 4px | Element UI 默认 |
| 输入框 | 4px | Element UI 默认 |
| 标签/徽标 | 3px | `el-tag` |
| 对话框 | 8px | `el-dialog` |

阴影仅用于浮层（对话框、下拉菜单），卡片不使用阴影，用 1px 边框（`#e6ebf2`）区分。

### 1.5 图标

- 侧边栏菜单：使用 Element UI 内置图标（`el-icon-*`）
- 操作按钮：必须带图标（`el-button` 的 `icon` 属性）
- 状态指示：使用 `el-tag` 配合颜色，不依赖纯图标

### 1.6 侧边栏配置

修改 `src/settings.js`：

```js
sideTheme: 'theme-light',  // 浅色侧边栏
```

配合全局样式覆盖，确保浅色侧边栏与内容区视觉统一。

---

## 2. 组件使用规范

### 2.1 Element UI 组件选型

| 组件 | 用途 | 定制说明 |
|------|------|----------|
| `el-table` | 所有数据列表 | `border stripe`，表头背景 `#f8f8f9`，所有列统一 `align="center"` |
| `el-form` | 所有表单 | `label-width="100px"`，`size="small"` |
| `el-input` | 文本输入 | `clearable`，超 50 字用 `type="textarea"` |
| `el-select` | 下拉选择 | `clearable filterable`（选项 > 20 时） |
| `el-date-picker` | 日期选择 | `value-format="yyyy-MM-dd"` |
| `el-input-number` | 金额/数字（表单区域） | `:min="0" :precision="2" controls-position="right"` 金额专用 |
| `el-input` (type=number) | 金额/数字（表格内嵌） | 表格单元格空间有限，不使用 `el-input-number`，改用 `<el-input>` 配合数字格式化，避免步进按钮挤占空间 |
| `el-tag` | 状态标签 | 统一 `size="small"` |
| `el-button` | 操作按钮 | 表格内用 `type="text"`，主要操作用 `type="primary"` |
| `el-pagination` | 分页 | 封装为 `<pagination>` 组件统一使用 |
| `el-dialog` | 弹窗 | `width` 根据内容设定，不超 720px |
| `el-card` | 信息卡片 | `shadow="never"` |
| `el-tooltip` | 文本溢出 | 表格列 `show-overflow-tooltip` |
| `el-popconfirm` | 删除确认 | 替代 `$modal.confirm` 用于行内操作 |
| `el-empty` | 空状态 | 列表无数据时统一使用 |

### 2.2 禁用组件

以下 Element UI 组件不在项目中使用：

- `el-timeline` — 业务不需要时间轴
- `el-calendar` — 使用日期选择器替代
- `el-carousel` — B 端系统不需要轮播

### 2.3 业务组件命名

自定义业务组件放在 `src/views/project/components/`，以 `Proj` 前缀命名：

```
components/
├── ProjStatusTag.vue      // 项目状态标签（封装 el-tag + 颜色映射）
├── ProjMoneyDisplay.vue   // 金额展示（千分位 + ¥ 前缀 + 颜色标识正负）
├── ProjWarnBadge.vue      // 预警等级徽标（黄/橙/红 + 脉冲动画）
├── ProjNodeSelect.vue     // WBS 节点下拉选择（带层级）
├── ProjCostCategorySelect.vue  // 成本科目下拉选择
```

### 2.4 通用组件规范

- **Pagination**：项目已有 `<pagination>` 组件，所有列表页统一使用
- **RightToolbar**：项目已有的右侧工具栏，列表页统一使用
- **DictTag**：项目已有的字典标签，用于字典数据展示
- **ImageUpload / FileUpload**：项目已有组件，文件上传统一使用

---

## 3. 页面布局规范

### 3.1 列表页模板

列表页固定结构：**页面标题区 → 筛选区 → 工具栏区 → 表格区 → 分页区**

```html
<div class="app-container xxx-list-page">
  <!-- 1. 页面标题 -->
  <div class="page-heading">
    <div>
      <h2>页面名称</h2>
      <p>页面描述（可选）</p>
    </div>
    <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建</el-button>
  </div>

  <!-- 2. 筛选区 -->
  <div class="filter-panel">
    <el-form :model="queryParams" :inline="true" size="small">
      <!-- 筛选项 -->
      <el-form-item class="filter-actions">
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
  </div>

  <!-- 3. 工具栏 + 表格 -->
  <div class="table-panel">
    <div class="table-toolbar">
      <div>
        <span class="toolbar-title">列表名称</span>
        <span class="toolbar-count">共 {{ total }} 条</span>
      </div>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <!-- 表格列 -->
    </el-table>

    <!-- 4. 分页 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="load" />
  </div>
</div>
```

**CSS 类名规范**：
- `.page-heading` — 标题区（flex，两端对齐）
- `.filter-panel` — 筛选区（白底、圆角 6px、边框 `#e6ebf2`）
- `.table-panel` — 表格区（同上）
- `.table-toolbar` — 工具栏（flex，含标题和计数）
- `.filter-actions` — 筛选区按钮组

### 3.2 表单页模板

```html
<div class="app-container form-page">
  <div class="page-heading">
    <div>
      <h2>{{ isEdit ? '编辑' : '新建' }}XXX</h2>
    </div>
    <div>
      <el-button @click="$router.back()">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="saveDraft">保存草稿</el-button>
    </div>
  </div>

  <div class="form-body">
    <!-- 分组一 -->
    <div class="form-group">
      <h3 class="form-header">基本信息</h3>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <!-- 表单字段 -->
      </el-form>
    </div>
    <!-- 分组二 -->
    <div class="form-group">
      <h3 class="form-header">子表信息</h3>
      <!-- 内嵌表格 -->
    </div>
  </div>
</div>
```

**表单布局规则**：
- 单字段（如名称、编号）：单列
- 并列字段（如开始/结束日期）：双列（`el-row` + `el-col :span="12"`）
- 分组区块用 `.form-header`（蓝色下划线标题）分隔
- 提交/保存按钮固定在页面顶部右侧，随滚动或置底可讨论

### 3.3 详情页模板

采用**信息卡片**布局，分组展示：

```
项目详情
├── 基本信息卡片
│   ├── detail-item: 项目编号 | 项目名称
│   ├── detail-item: 客户名称 | 合同金额
│   └── detail-item: 开工日期 | 竣工日期
├── 节点信息卡片
│   └── 内嵌 el-table（节点列表）
├── 成本汇总卡片
│   └── 数字统计
└── 操作区
    └── 审批按钮（仅审批中可见）
```

**CSS 类名**：使用项目已有的 `.detail-card`、`.detail-card-title`、`.detail-row`、`.detail-item`、`.detail-label`、`.detail-value`。

### 3.4 看板页模板

```
看板页
├── 顶部统计卡片行（4 个指标卡片横排）
├── 左侧：图表区（柱状图/折线图）
├── 右侧：预警列表
└── 底部：项目总览表格
```

统计卡片使用 `el-row :gutter="16"` + `el-col :span="6"` 四列布局。每个卡片包含：图标、数值、标签、同比变化（可选）。

---

## 4. 编码风格规范

### 4.1 Vue 单文件组件结构

严格遵循以下顺序：

```vue
<template>
  <!-- 模板 -->
</template>

<script>
// 导入顺序：第三方库 → 项目 API → 项目组件 → 项目工具
import { apiMethod } from '@/api/xxx'
import Pagination from '@/components/Pagination'
import { formatMoney } from '@/utils/project'

export default {
  name: 'ComponentName',     // 必须
  components: { Pagination },
  props: {},                 // 若有
  data() { return {} },
  computed: {},
  watch: {},                 // 若有
  created() {},
  mounted() {},              // 若有
  methods: {}
}
</script>

<style scoped>
/* 样式 */
</style>
```

### 4.2 命名约定

| 类别 | 规则 | 示例 |
|------|------|------|
| 组件名 | PascalCase，Proj 前缀（业务组件） | `ProjStatusTag.vue` |
| 页面文件 | kebab-case | `index.vue`, `detail.vue`, `form.vue` |
| API 函数 | camelCase，动词在前 | `listProjInfos`, `getProjInfo`, `delProjInfo` |
| CSS class | kebab-case，语义化 | `.page-heading`, `.filter-panel` |
| data 变量 | camelCase | `queryParams`, `submitLoading` |
| 常量 | UPPER_SNAKE_CASE | `STATUS_OPTIONS` |
| Event | kebab-case | `@pagination`, `@update:visible` |

### 4.3 API 调用规范

```js
// 标准模式：loading → 调用 → 成功处理 → 错误提示 → finally
methods: {
  load() {
    this.loading = true
    apiMethod(params).then(res => {
      this.list = res.rows || []
      this.total = res.total || 0
    }).catch(() => {
      this.$message.error('加载失败')
    }).finally(() => {
      this.loading = false
    })
  }
}
```

规则：
- 列表类 API：统一 `{ rows, total }` 响应格式
- 提交类 API：成功后 `$message.success()` + 刷新列表
- 删除类 API：必须用 `$modal.confirm()` 或 `el-popconfirm` 确认
- 所有 API 调用**必须带 `.catch()`**，不能吞错误不处理

### 4.4 目录结构规范

```
src/
├── api/project/          // 项目相关 API（每个 Domain 一个文件）
│   ├── projInfo.js
│   ├── projBudget.js
│   └── projCost.js
├── views/project/
│   ├── projInfo/         // 每个功能模块一个目录
│   │   ├── index.vue     //  列表页
│   │   ├── form.vue      //  表单页
│   │   └── detail.vue    //  详情页
│   ├── budget/
│   └── components/       // 模块内共享的业务组件
├── components/           // 全局通用组件
├── utils/                // 工具函数
│   ├── project.js        //   项目相关工具（STATUS_OPTIONS, formatMoney 等）
│   └── auth.js           //   权限相关
└── store/modules/        // Vuex 模块
```

### 4.5 状态映射集中管理

所有状态值 → 中文映射集中定义在 `src/utils/project.js`：

```js
// src/utils/project.js
export const STATUS_OPTIONS = [
  { value: '0', label: '草稿' },
  { value: '1', label: '审批中' },
  { value: '2', label: '已立项' },
  { value: '3', label: '已驳回' },
  { value: '4', label: '进行中' },
  { value: '5', label: '已完工' }
]

export function statusLabel(status) {
  const item = STATUS_OPTIONS.find(i => i.value === status)
  return item ? item.label : status
}

export function statusTagType(status) {
  return ({ '0': 'info', '1': 'warning', '2': 'success', '3': 'danger', '4': '', '5': 'success' })[status] || 'info'
}

export function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
```

**禁止**在每个 Vue 文件中重复定义相同的 STATUS_OPTIONS / formatMoney。

### 4.6 CSS 书写规范

- **必须使用 `<style scoped>`**，避免样式污染
- 覆盖 Element UI 样式时使用 `>>>` 或 `/deep/`
- 页面级样式写在该 `.vue` 文件中
- 全局通用样式写在 `src/assets/styles/ruoyi.scss`
- 特定业务通用样式可新增 `src/assets/styles/project.scss`
- 颜色值统一使用 SCSS 变量（在 `element-variables.scss` 中定义）
- 不写行内样式（`:style` 动态值除外）

### 4.7 交互规范

| 场景 | 实现方式 |
|------|----------|
| 加载中 | `v-loading` 指令 + `loading` 状态变量 |
| 空数据 | `el-empty description="暂无数据"` |
| 提交中 | 按钮 `:loading="submitting"`，防重复点击 |
| 删除确认 | `$modal.confirm()` 或 `el-popconfirm` |
| 操作成功 | `this.$message.success('XXX成功')` |
| 操作失败 | `.catch()` 内 `this.$message.error('XXX失败')` |
| 离开未保存 | 表单页 `beforeRouteLeave` 守卫检测脏数据 |
| 审批驳回原因 | `$prompt` 弹窗，必填校验 |

---

## 5. 实施路径

| 阶段 | 内容 | 优先级 |
|------|------|--------|
| 第一阶段 | 侧边栏改浅色、配色变量定义、公共工具 `utils/project.js` | P0 |
| 第二阶段 | 列表/表单/详情页模板统一、状态映射迁移 | P1 |
| 第三阶段 | 业务组件提取（ProjStatusTag 等）、看板布局 | P2 |
| 第四阶段 | CSS 变量化、历史页面规范化 | P3 |

---

## 6. 与现有代码的关系

- 本规范建立时参考了 `claude-review.md` 中识别的问题（STATUS_OPTIONS 重复定义、formatMoney 重复等），给出了解决方案
- `settings.js` 中 `sideTheme` 从 `'theme-dark'` 改为 `'theme-light'`
- 现有的 `.detail-card`、`.page-heading`、`.filter-panel` 等 CSS 类已应用了部分规范，本规范将它们正式标准化
- 新页面必须遵循本规范，已有页面在后续迭代中逐步对齐
