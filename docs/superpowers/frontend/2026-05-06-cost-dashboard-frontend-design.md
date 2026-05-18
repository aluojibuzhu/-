# 成本看板模块 — 前端设计方案

| 版本号 | 日期 | 作者 | 说明 |
|--------|------|------|------|
| V1.0 | 2026-05-06 | 产品规划团队 | 模块5前端完整设计 |

> **关联文档**: [前端规范设计文档](../specs/2026-04-29-frontend-specification-design.md) · [成本看板设计规格](../specs/2026-05-06-cost-dashboard-design.md)

---

## 1. 设计总则

延续模块4的扁平风格：减少 card 容器，用分隔线和留白区分区块。总览页指标卡片保留细边框和文字，报表页采用左右分栏布局。

---

## 2. 组件树

### 2.1 成本总览 `views/project/costDashboard/index.vue`

```
index.vue
├── 页面标题区
│   ├── h2: 成本总览
│   ├── p: 实时项目资金状态
│   └── el-button icon="refresh": 刷新
│
├── 6指标卡片行 (.indicator-row, el-row :gutter="12")
│   └── x6 el-col :span="4"
│       └── .indicator
│           ├── .ind-label (icon + 文字)
│           ├── .ind-value (count-to 数字动画)
│           └── .ind-sub (辅助说明，本月入账显示环比箭头)
│
├── 状态切换 (.tab-bar)
│   └── x3 .tab-item (进行中/已完工/全部，active 蓝色下划线)
│
├── 搜索+计数 (.toolbar)
│   ├── el-input placeholder="搜索项目名称或编号" clearable
│   └── span: 共 N 个项目
│
├── 项目表格 (el-table border stripe)
│   ├── 编号 (link color)
│   ├── 项目名称 (strong, show-overflow-tooltip)
│   ├── 合同金额 (formatMoney)
│   ├── 预算总额 (formatMoney)
│   ├── 已执行 (formatMoney)
│   ├── 预算余额 (formatMoney, 余额少时红色)
│   ├── 利润空间 (formatMoney, +绿色/−红色)
│   ├── 本月入账 (formatMoney)
│   └── 执行率 (progress-bar内联 + 百分比)
│   └── @row-click → router push 项目详情
│
└── 分页 (pagination)
```

### 2.2 专项报表（4个独立页面）

每个报表页统一模板：**筛选在上方，表格在下方，底部导出按钮**。

```
模板结构（4个页面共用）
├── 页面标题区
│   ├── h2: 报表名称（如"项目资金汇总表"）
│   └── p: 报表侧重点描述
│
├── 筛选区（上方，.filter-bar）
│   └── el-form :inline
│       ├── 项目: el-select multiple（默认全部）
│       ├── [时间范围 / 科目 / 单据类型 / 审批人 / 节点]（按报表类型）
│       ├── el-button: 查询
│       └── el-button: 重置
│
├── 表格区（下方，无card包裹）
│   └── el-table border stripe（固定列）
│
├── 分页
│   └── pagination
│
└── 底部
    └── el-button: 导出Excel
```

**4个报表的差异**：

| 报表 | 筛选字段 | 固定表格列 |
|------|---------|-----------|
| 项目资金汇总表 | 项目(多选)/时间范围 | 编号/名称/合同/预算/已执行/余额/利润空间/执行率 |
| 科目成本明细表 | 项目(多选)/科目(多选)/时间范围 | 项目/一级科目/二级科目/预算/已执行/余额/执行率 |
| 入账流水明细表 | 项目(多选)/单据类型/时间范围/审批人 | 编号/项目/类型/科目/金额/日期/审批人 |
| 节点预算执行表 | 项目(多选)/节点(多选)/时间范围 | 项目/节点编号/名称/预算/已执行/余额/执行率/完成日期 |

---

## 3. 数据流

### 3.1 成本总览

```
created()
  ├── loadSummary(status)  → summary: { totalBudget, actualCost, balance,
  │                                     contractAmount, profitMargin, monthlyCost, momChange }
  └── loadList(status, keyword, page) → list: { rows, total }

watch: activeTab → loadSummary + loadList(重置page)
watch: keyword → loadList(debounce 300ms)
```

### 3.2 专项报表（4个页面共用模式）

```
form: { projIds[], [specificFilters], dateRange }

项目资金汇总: GET /cost/report/projectSummary → list
科目成本明细: GET /cost/report/categoryDetail → list
入账流水明细: GET /cost/report/postingFlow    → list
节点预算执行: GET /cost/report/nodeExecution   → list

handleExport() → POST /cost/report/export/{type} → blob download
```

---

## 4. 关键组件和数据

| 场景 | 实现 |
|------|------|
| 数字动画 | count-to 组件，:start-val="0" :end-val="value" :duration="800" |
| 环比箭头 | momChange > 0 → 绿色↑, < 0 → 红色↓, = 0 → 灰色→ |
| 利润空间颜色 | profitMargin >= 0 → #52c41a, < 0 → #ff4d4f |
| 余额低警告 | balance < totalBudget * 0.1 → 金额红色 |
| 执行率进度条 | 内联 CSS width=execRate%, 颜色按阈值 |
| tab切换 | 自定义 tab-bar，不用 el-tabs（扁平化） |
| 报表预览 | filter变化 → 手动点击预览才刷新，不自动触发 |
| 导出下载 | responseType: 'blob'，生成临时 a 标签点击下载 |

---

## 5. CSS 补充

```scss
.indicator-row { display: flex; gap: 12px; margin-bottom: 16px }
.indicator { flex: 1; background: #fff; border: 1px solid #e6ebf2;
             border-radius: 6px; padding: 14px 16px }
.tab-bar { display: flex; border-bottom: 2px solid #e6ebf2; margin-bottom: 12px }
.tab-item { padding: 10px 20px; cursor: pointer; border: none; background: none;
            color: #8c98a8; font-size: 14px; transition: all 0.2s }
.tab-item.active { color: #1890ff; border-bottom: 2px solid #1890ff; font-weight: 500 }

// 环比变化
.mom-up   { color: #52c41a; font-size: 11px }
.mom-down { color: #ff4d4f; font-size: 11px }
```

---

*本文档为成本看板模块的前端设计规范。*
