# 监控预警模块 — 前端设计方案

| 版本号 | 日期 | 作者 | 说明 |
|--------|------|------|------|
| V1.0 | 2026-05-06 | 产品规划团队 | 模块4前端完整设计，含组件树/状态管理/图表配置/交互规范 |

> **关联文档**: [前端规范设计文档](../specs/2026-04-29-frontend-specification-design.md) · [监控预警设计规格](../specs/2026-05-06-alert-monitoring-design.md) · [监控预警实现计划](../plans/2026-05-06-alert-monitoring-implementation-plan.md)

---

## 1. 设计总则

### 1.1 风格决策

本次 brainstorming 确定的关键前端设计原则：

| 原则 | 规范 |
|------|------|
| 基础风格 | 延续项目「现代轻量」：白色底色、科技蓝#1890ff、浅色侧边栏 |
| 容器策略 | **全局减少方块感**：列表/表格区域不额外包裹 el-card，用 border-bottom 分隔行 |
| 筛选区 | 无背景容器，表单项直接排列于页面顶部 |
| 弹窗/抽屉 | 保留圆角(8px)和边框(1px solid #e6ebf2)，不保留阴影 |
| 看板数据密度 | 项目经理视角，可滚动，各区块纵向排列 |
| 下钻交互 | 弹窗（el-dialog）或抽屉（el-drawer），不离开看板 |

### 1.2 与已有规范的差异

本模块有较高的数据可视化需求，需要在基础规范之上补充：
- ECharts 图表组件的改造和动态数据绑定
- 看板页面的特殊布局（非标准列表/表单/详情模板）
- el-drawer 的使用规则（现有项目未使用）

---

## 2. 页面组件树

### 2.1 预警看板 `views/project/alertDashboard/index.vue`

```
index.vue
├── 页面标题区 (.page-heading)
│   ├── h2: 预警看板
│   ├── el-select: 项目筛选（全部项目/单个项目）
│   ├── el-button: 刷新
│   └── el-button: 导出
│
├── 预警概览统计 (.stat-row)
│   └── x4 统计卡片（内联组件，v-for panels）
│       ├── el-icon
│       ├── count-to: 数字滚动
│       └── .label + .sub
│
├── 项目健康度总览 (.health-row)
│   └── x4 分类卡片（内联，v-for levels）
│       ├── .dot: 颜色圆点
│       ├── .count: 项目数
│       └── .desc: 分类描述
│   └── 点击 → el-dialog(.health-dialog)
│       ├── 分类标题 + 统计摘要
│       └── el-table: 该项目列表
│           └── 操作列: 查看详情 → 跳转项目详情页
│
├── 图表区上行 (.chart-row)
│   ├── 左: 预算消耗趋势 (.chart-panel)
│   │   └── <line-chart :chart-data="budgetTrendData" height="320px" />
│   │       点击数据点 → el-dialog: 当月明细
│   └── 右: 科目消耗分布 (.chart-panel)
│       ├── <bar-chart :chart-data="categoryBarData" height="280px" />
│       └── <pie-chart :chart-data="categoryPieData" height="280px" />
│           点击 → el-dialog: 该科目预警列表
│
├── 图表区下行 (.chart-row)
│   └── 项目预警Top10 (.chart-panel)
│       ├── 左: 横向条形图(ECharts bar type='horizontal')
│       └── 右: el-table 同步数据
│           点击 → el-dialog: 项目摘要卡片
│
└── 下钻弹窗组件（内联，v-if 控制显隐）
```

### 2.2 预警中心 `views/project/alertRecord/index.vue`

```
index.vue
├── 页面标题区 (.page-heading)
│   └── h2: 预警中心
│
├── 筛选区（无 card 包裹，直接 div.filter-bar）
│   └── el-form :inline
│       ├── el-input: 项目名称
│       ├── el-select: 预警级别（全部/黄/橙/红）
│       ├── el-select: 预警类型（全部/项目级/节点级/科目级）
│       ├── el-select: 处理状态（全部/待处理/已确认/...）
│       ├── el-date-picker type="daterange": 触发时间
│       └── .filter-actions: [搜索] [重置]
│
├── 工具栏 (.toolbar)
│   ├── span: 共 N 条预警记录
│   └── el-button type="text": 导出
│
├── 表格区（无 card 包裹）
│   └── el-table border stripe
│       ├── 编号(link, #1890ff)
│       ├── 项目(strong)
│       ├── 级别(el-tag: warning/danger/自定义橙色)
│       ├── 类型(文本转换)
│       ├── 触发条件
│       ├── 执行率(内联进度条 + 百分比)
│       ├── 触发时间(el-table-column formatter)
│       ├── 状态(el-tag)
│       └── 操作(button text, 动态显隐)
│
├── 分页
│   └── <pagination>
│
└── 详情抽屉
    └── el-drawer (right, 560px)
        ├── header: 预警编号 + 级别tag
        ├── 信息网格(.info-grid, CSS Grid 2列)
        ├── 预算对比(progress bar + 数字)
        ├── 处理时间线(.timeline, CSS实现)
        └── 底部: 操作按钮（按状态动态显示）
```

### 2.3 预警规则 `views/project/alertRule/index.vue`

```
index.vue
├── 页面标题区
│   ├── h2: 预警规则配置
│   └── el-button type="primary": 新增规则
│
├── 工具栏
│   └── span: 共 N 条规则 · 启用 N 条
│
├── 表格区
│   └── el-table
│       ├── 名称 / 类型(tag) / 触发条件 / 级别(tag) / 范围 / 通知
│       ├── 启用: el-switch @change="toggleRule"
│       └── 操作: [编辑] [删除(el-popconfirm)]
│
└── 编辑弹窗
    └── el-dialog (620px)
        └── el-form :model="form" label-width="100px"
            ├── 规则名称: el-input
            ├── 规则类型: el-select → 条件格式联动
            ├── 触发条件: 动态组件
            │   ├── EXEC_RATE → el-select(>=/>/= ) + el-input-number(%)
            │   ├── SINGLE_AMOUNT → el-select(>=) + el-input-number(元)
            │   ├── BALANCE_RATE → el-select(<=) + el-input-number(%)
            │   ├── OVERDUE → el-select(>=) + el-input-number(天)
            │   └── INACTIVE → el-select(>=) + el-input-number(月)
            ├── 预警级别: el-radio-group（带颜色icon）
            ├── 作用范围: el-radio(全局/项目/科目) + el-select(multiple, v-if)
            ├── 通知角色: el-checkbox-group(项目经理/成本会计/管理员)
            ├── 静默期: el-input-number(min=0,max=720) + "小时"
            ├── 规则说明: el-input textarea
            └── 启用: el-switch
```

---

## 3. 状态管理

### 3.1 看板页数据流

```
created()
  ├── loadSummary()    → summary: { pendingCount, todayNew, monthNew, closedTotal }
  ├── loadHealth()      → health: { normal, yellow, orange, red }
  ├── loadBudgetTrend() → budgetTrend: { months[], budgetSeries[], actualSeries[] }
  ├── loadCategoryCmp() → categoryCompare: { categories[], budget[], actual[] }
  └── loadTop10()       → top10: [{ projId, projName, totalBudget, actualCost, execRate, alertLevel }]

watch: selectedProjId → 所有图表数据重新加载
```

### 3.2 预警中心数据流

```
created()
  └── load() → list: { rows, total }
      queryParams: { pageNum, pageSize, projName, alertLevel, alertType, status, dateRange }

handleConfirm(id) → PUT /alert/record/confirm/{id} → load() 刷新
handleIgnore(id)   → PUT /alert/record/ignore/{id}   → load()
handleFollow(id)   → PUT /alert/record/follow/{id}   → load()
handleClose(id)    → PUT /alert/record/close/{id}    → load()
openDetail(id)     → GET /alert/record/{id} + GET /alert/record/log/{id}
                    → drawerVisible = true
```

### 3.3 规则配置数据流

```
created()
  └── loadRules() → ruleList
      queryParams: { pageNum, pageSize }

toggleRule(row) → PUT /alert/rule/toggle/{ruleId} → body: { enabled }
handleAdd()     → dialogVisible=true, form={}
handleEdit(row) → GET /alert/rule/{ruleId} → form = row
handleSave()    → form.ruleId ? PUT : POST → dialogVisible=false → loadRules()
handleDelete(id)→ DELETE /alert/rule/{id} → loadRules()
```

---

## 4. ECharts 图表配置

### 4.1 预算消耗趋势（LineChart）

```javascript
option = {
  tooltip: { trigger: 'axis' },
  legend: { data: ['预算总额', '实际成本'], bottom: 0 },
  grid: { left: 60, right: 20, top: 20, bottom: 30 },
  xAxis: { type: 'category', data: chartData.months, axisLine: { lineStyle: { color: '#e6ebf2' } } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
  series: [
    {
      name: '预算总额', type: 'line', smooth: true,
      data: chartData.budgetSeries,
      lineStyle: { color: '#1890ff', width: 2 },
      itemStyle: { color: '#1890ff' }
    },
    {
      name: '实际成本', type: 'line', smooth: true,
      data: chartData.actualSeries,
      lineStyle: { color: '#ff4d4f', width: 2 },
      itemStyle: { color: '#ff4d4f' },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [{ offset: 0, color: 'rgba(255,77,79,0.08)' }, { offset: 1, color: 'rgba(255,77,79,0)' }] } }
    }
  ]
}
```

### 4.2 成本科目消耗柱状图（BarChart）

```javascript
option = {
  tooltip: { trigger: 'axis' },
  legend: { data: ['预算', '实际'], bottom: 0 },
  grid: { left: 50, right: 10, top: 10, bottom: 30 },
  xAxis: { type: 'category', data: chartData.categories, axisLabel: { rotate: 20, fontSize: 11 } },
  yAxis: { type: 'value' },
  series: [
    { name: '预算', type: 'bar', data: chartData.budget, itemStyle: { color: '#91d5ff' }, barGap: '10%' },
    { name: '实际', type: 'bar', data: chartData.actual, itemStyle: { color: '#ff7875' } }
  ]
}
```

### 4.3 消耗占比圆环图（PieChart）

```javascript
option = {
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', right: 10, top: 'center', itemWidth: 8, itemHeight: 8 },
  series: [{
    type: 'pie', radius: ['50%', '72%'], center: ['38%', '52%'],
    label: { show: false },
    data: chartData.pieData,  // [{name, value}]
    itemStyle: { borderColor: '#fff', borderWidth: 2 }
  }],
  graphic: [{
    type: 'text', left: '28%', top: '44%',
    style: { text: '实际成本', textAlign: 'center', fill: '#8c98a8', fontSize: 12 }
  }, {
    type: 'text', left: '28%', top: '50%',
    style: { text: '¥' + totalText, textAlign: 'center', fill: '#1f2d3d', fontSize: 16, fontWeight: 600 }
  }]
}
```

### 4.4 项目预警Top10 横向条形图

```javascript
option = {
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 120, right: 40, top: 10, bottom: 10 },
  xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
  yAxis: { type: 'category', data: chartData.projects, inverse: true,
    axisLabel: { fontSize: 11 } },
  series: [{
    type: 'bar', data: chartData.rates,
    itemStyle: { borderRadius: [0, 3, 3, 0] },
    // 每条颜色根据执行率动态设置
    data.map(v => ({
      value: v,
      itemStyle: { color: v >= 100 ? '#ff4d4f' : v >= 90 ? '#fa8c16' : '#faad14' }
    }))
  }]
}
```

---

## 5. 交互规范

### 5.1 看板下钻

| 触发区域 | 交互 | 弹窗内容 | 参数 |
|----------|------|----------|------|
| 待处理预警卡片 | click → el-dialog | 待处理预警列表 | status='0' |
| 健康度"橙色预警"卡片 | click → el-dialog | 该分类项目列表 | execRate 90-100% |
| 趋势图数据点 | click → el-dialog | 该月成本明细 | month |
| 科目柱体/扇区 | click → el-dialog | 该科目预警列表 | categoryId |
| Top10 行 | click → el-dialog | 项目摘要卡片 | projId |

### 5.2 预警中心操作

| 当前状态 | 可用操作 |
|----------|----------|
| 待处理(0) | 确认 / 忽略 / 跟进 |
| 已确认(1) | 跟进 / 关闭 |
| 已忽略(2) | 跟进 |
| 跟进中(3) | 关闭 |
| 已关闭(4) | 无操作 |

**操作确认**：忽略/关闭操作需 `$prompt` 输入原因，必填校验。

### 5.3 规则表单联动

```
ruleType 切换 → 触发条件输入格式变化：
  EXEC_RATE     → 比较符[≥/>/=] + 数字[0-100] + "%"
  SINGLE_AMOUNT → 比较符[≥] + 数字[0-99999999] + "元"
  BALANCE_RATE  → 比较符[≤] + 数字[0-100] + "%"
  OVERDUE       → 比较符[≥] + 数字[1-999] + "天"
  INACTIVE      → 比较符[≥] + 数字[1-36] + "月"

scopeType 切换 → 指定项目/科目时显示对应的 el-select multiple
```

---

## 6. CSS 规范补充

### 6.1 新增全局样式建议

```scss
// 看板用
.stat-row    { display: flex; gap: 12px; margin-bottom: 12px }
.health-row   { display: flex; gap: 12px; margin-bottom: 12px }
.chart-row    { display: flex; gap: 12px; margin-bottom: 12px }
.chart-panel  { flex: 1; background: #fff; border: 1px solid #e6ebf2; border-radius: 6px; padding: 16px }
.filter-bar   { display: flex; gap: 10px; align-items: center; flex-wrap: wrap;
                padding-bottom: 14px; border-bottom: 1px solid #e6ebf2; margin-bottom: 14px }

// 看板卡片（轻量版）
.stat-card    { flex: 1; background: #fff; border: 1px solid #e6ebf2; border-radius: 6px;
                padding: 16px; cursor: pointer; transition: box-shadow 0.15s }
.stat-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.08) }
.health-card  { flex: 1; border-radius: 6px; padding: 16px; cursor: pointer; border: 1px solid }
.health-card .h-count { font-size: 32px; font-weight: 700 }

// 时间线（预警详情用，避免el-timeline）
.timeline     { position: relative; padding-left: 24px }
.timeline::before { content: ''; position: absolute; left: 8px; top: 4px; bottom: 4px;
                    width: 2px; background: #e6ebf2 }
.tl-item      { position: relative; padding: 8px 0 8px 16px }
.tl-item::before { content: ''; position: absolute; left: -20px; top: 14px;
                   width: 10px; height: 10px; border-radius: 50%; background: #fff;
                   border: 2px solid #1890ff }

// 信息网格
.info-grid    { display: grid; grid-template-columns: 1fr 1fr; gap: 12px }
.info-row     { padding: 8px 0; border-bottom: 1px solid #f5f5f5 }
```

### 6.2 预警级别颜色变量

```scss
// element-variables.scss 中新增
$color-alert-yellow: #faad14;
$color-alert-orange: #fa8c16;
$color-alert-red:    #ff4d4f;
$color-alert-green:  #52c41a;
```

---

## 7. 与现有图表组件的差异

| 现有组件 | 当前状态 | 改造后 |
|----------|----------|--------|
| `LineChart.vue` | 内联 mock 数据 | 通过 props.chartData 接收动态数据 |
| `BarChart.vue` | 内联 mock 数据 | 通过 props.chartData 接收，支持双系列 |
| `PieChart.vue` | 内联 mock 数据 | 通过 props.chartData + props.centerText 配置 |
| `PanelGroup.vue` | 硬编码4个卡片 | 通过 props.panels 数组动态渲染 |
| `RaddarChart.vue` | 内联 mock 数据 | 可选使用 |

改造方式：在 Chart 组件中新增 `chartData` prop + watch；若无数据传入则使用原有默认值（不影响原有 dashboard 页）。

---

*本文档为监控预警模块的前端设计规范，实施时与 [前端规范设计文档](../specs/2026-04-29-frontend-specification-design.md) 配合使用。*
