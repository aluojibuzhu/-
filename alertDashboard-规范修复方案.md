# 预警看板 — 前端规范（最终版）

> 文件: `RuoYi-Vue/ruoyi-ui/src/views/project/alertDashboard/index.vue`
> 参考实现: `alertDashboard-preview.html`
> 日期: 2026-05-08

---

## 一、设计决策

| 决策 | 结论 |
|------|------|
| 整体风格 | 开放式无边无界 — 卡片不使用边框、圆角、阴影，内容靠留白和极淡分隔线区分 |
| 页面标题 | 透明背景，纯留白分隔，不加白底卡片包裹 |
| 统计卡片 | 透明背景，数字直接浮在灰色底色上，紧凑纵向间距 |
| 图表区 | `grid` 两列（~62:38），两张图等高，底部信息区用固定高度强制对齐 |
| 图表底部 | 标题 + 辅助信息（日期选择器/配色/图例）排在同一行，字体小不抢眼 |
| 项目列表 | 白底 + 淡边框，与上方无界图表形成层次对比 |
| 图表颜色 | 主色 `#1890ff` 以 `rgba(24,144,255,.78)` 半透明呈现，明亮但不刺眼 |
| 折线面积 | 羽化模糊 + 渐变消失 |
| 交互 | 点击项目行高亮选中，折线图和饼图同步切换至该项目的独立数据 |

---

## 二、CSS 设计 Token

```css
:root {
  --bg-page:        #f5f7fa;   /* 页面底色 */
  --bg-card:        #ffffff;   /* 仅表格区使用 */
  --text-primary:   #1f2d3d;   /* 标题、正文 */
  --text-regular:   #606266;   /* 常规文字 */
  --text-secondary: #8c98a8;   /* 辅助说明 */
  --color-primary:  #1890ff;   /* 主色 */
  --color-danger:   #dc2626;   /* 红色预警 */
  --color-warning:  #f97316;   /* 橙色预警 */
  --color-yellow:   #ca8a04;   /* 黄色预警 */
  --color-success:  #16a34a;   /* 绿色正常 */
  --border-subtle:  #e6ebf2;   /* 仅表格/筛选面板使用 */
  --chart-color:    rgba(24,144,255,.78);  /* 图表主色 */
}
```

---

## 三、布局结构

```
┌──────────────────────────────────────────────┐
│  .container  max-width:1440px padding:28px 40px │
│                                              │
│  .page-heading    标题 + 描述 + 刷新按钮       │  ← 透明无框
│                                              │
│  .summary-row     grid 6列 × 1行             │  ← 透明无框
│    .summary-card    label / value / trend      │
│                                              │
│  .chart-row       grid 两列 (1.55fr .95fr)   │
│    .detail-card    flex column min-h:414px    │  ← 透明，底部分隔线
│      .card-body    flex:1 图表 SVG            │
│      .chart-footer min-h:66px                │  ← 固定高度强制对齐
│        .chart-footer-title  12px 居中         │
│        .chart-footer-meta   h:34px flex居中   │
│                                              │
│  .table-card      透明底 + 可选淡边框          │  ← 仅此处有框
│    .table-title-wrap                         │
│    .health-table   border-collapse           │
└──────────────────────────────────────────────┘
```

---

## 四、关键 CSS 规则

### 4.1 页面标题 — 完全开放

```css
.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 0;
  margin-bottom: 28px;
  background: transparent;
  border: 0;
  border-radius: 0;
}
.page-heading h2 { font-size: 22px; font-weight: 600; }
.page-heading p  { font-size: 13px; color: var(--text-secondary); }
```

> **注意**: 看板页的 `.page-heading` 不遵循列表/表单页的"白底+边框+圆角"规范。看板需要更开放的视觉语言。

### 4.2 统计卡片 — 透明数字行

```css
.summary-row {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.summary-card {
  min-height: auto;
  padding: 0;
  background: transparent;
  border: 0;
  border-radius: 0;
}
.summary-card .label { font-size: 13px; color: var(--text-secondary); margin-bottom: 2px; }
.summary-card .value { font-size: 26px; font-weight: 700; }
```

### 4.3 图表区 — Grid 等高 + 底部固定对齐

```css
.chart-row {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(320px, .95fr);
  gap: 16px;
  margin-bottom: 16px;
}
.detail-card {
  min-height: 414px;
  display: flex;
  flex-direction: column;
  background: transparent;   /* 无边 */
  border: 0;
  border-radius: 0;
  padding: 0;
}
.card-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
}
```

**底部信息区固定高度**保证两个图的标题对齐:

```css
.chart-footer        { min-height: 66px; flex-shrink: 0; text-align: center; }
.chart-footer-title  { min-height: 22px; font-size: 12px; color: var(--text-secondary); }
.chart-footer-meta   { height: 34px; display: flex; align-items: center; justify-content: center; gap: 14px; }
```

### 4.4 折线图 SVG 样式

```css
.line-chart-container { width: 100%; height: 100%; display: flex; align-items: center; }
.line-chart-svg       { width: 100%; display: block; overflow: visible; }
```

面积渐变 + 羽化（在 SVG `<defs>` 中）:

```xml
<linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
  <stop offset="0%" stop-color="#1890ff" stop-opacity="0.13"/>
  <stop offset="100%" stop-color="#1890ff" stop-opacity="0"/>
</linearGradient>
```

### 4.5 项目表格 — 唯一有边框的区块

```css
.health-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.health-table th {
  background: #f8fafc;
  border-bottom: 1px solid var(--border-subtle);
  font-weight: 500;
  font-size: 12px;
  color: var(--text-secondary);
}
.health-table td {
  padding: 12px;
  border-bottom: 1px solid #f1f4f8;
  cursor: pointer;
}
/* 选中态 */
.health-table tbody tr.selected {
  background: #eef6ff;
  outline: 1px solid rgba(24,144,255,.35);
  outline-offset: -1px;
}
```

### 4.6 图表颜色切换

```css
.color-palette { display: inline-flex; align-items: center; gap: 7px; }
.color-dot {
  width: 16px; height: 16px;
  border-radius: 50%;
  background: var(--c);
  cursor: pointer;
  border: 2px solid transparent;
}
.color-dot.active { border-color: #fff; box-shadow: 0 0 0 2px var(--text-primary); }
```

---

## 五、JS 数据流

图表由数据驱动渲染，不与具体数据耦合:

```js
// 默认状态（全部项目汇总）
const defaultState = {
  name: '全部项目',
  trend: [40, 56, 50, 88, 70, 103, 118, 94, 77, 100, 108, 96, 90],
  pie: [135, 85, 66, 46, 38],
  total: '386万'
};

// 项目行携带独立数据
<tr data-name="某项目"
    data-trend="60,65,70,..."
    data-pie="90,55,30,..."
    data-pietotal="215万">

// 点击选中 → 重新渲染图表
function selectProject(row) { ... }
```

三个渲染函数:
- `renderTrend(values)` — 根据数值数组动态计算 SVG polyline 坐标和 Y 轴刻度
- `renderPie(values)` — 根据数值数组动态生成环形图 segment + 图例
- `applyDashboardState(state)` — 统一入口，更新标题+折线+饼图

---

## 六、响应式

| 断点 | 行为 |
|------|------|
| ≤ 1180px | 统计卡从 6 列缩为 3 列，图表区从双列变为单列 |
| ≤ 768px | 容器内边距缩至 16px，标题区换行，统计卡 2 列，表格横向滚动 |

---

## 七、与列表/表单页规范的差异

看板页作为特殊页面类型，部分规则与通用规范不同：

| 规则 | 通用规范 | 看板页 |
|------|---------|--------|
| `.page-heading` 背景 | 白色 + 边框 + 圆角 | 透明、无框 |
| 卡片样式 | 白色背景 + 边框/阴影 | 透明、无框（表格区除外） |
| 区块标题装饰 | `::after` 蓝色下划线 | 无装饰 |
| 区块间距 | `14px` | `28px` ~ `32px`（更宽松） |
| 配色 | 全色值 `#1890ff` | `rgba(24,144,255,.78)` 半透明 |
