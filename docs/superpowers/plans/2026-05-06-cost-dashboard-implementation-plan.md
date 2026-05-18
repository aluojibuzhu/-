# 成本看板模块实现计划

| 版本号 | 日期 | 作者 | 说明 |
|--------|------|------|------|
| V1.0 | 2026-05-06 | 产品规划团队 | 基于设计规格，制定模块5实现计划 |

> **关联文档**: [PRD_MVP.md](../../../PRD_MVP.md) · [成本看板设计规格](../specs/2026-05-06-cost-dashboard-design.md)

---

## 1. 模块定位

实现 MVP 闭环第五阶段——成本看板。无需新建数据库表，数据全部从现有表（proj_info, cost_posting_record 等）聚合。核心工作在后端聚合查询和前端展示+导出。

---

## 2. 文件结构

```
后端
ruoyi-admin/src/main/java/com/ruoyi/web/controller/project/
├── CostDashboardController.java     # 总览API (summary + list)
└── CostReportController.java        # 报表API (preview + export)

ruoyi-system/src/main/java/com/ruoyi/system/service/project/
├── ICostDashboardService.java + impl  # 指标计算 + 列表聚合
└── ICostReportService.java + impl     # 报表查询 + Excel生成

前端
ruoyi-ui/src/
├── api/project/
│   ├── costDashboard.js
│   └── costReport.js
├── views/project/
│   ├── costDashboard/index.vue        # 5.1 成本总览
│   └── costReport/                    # 5.2 4个专项报表
│       ├── projectSummary.vue         # 项目资金汇总表
│       ├── categoryDetail.vue         # 科目成本明细表
│       ├── postingFlow.vue            # 入账流水明细表
│       └── nodeExecution.vue          # 节点预算执行表
```

---

## 3. 实施步骤

### Phase 1：后端 Service

1. `ICostDashboardService` + impl
   - `summary(status)` — 6指标聚合SQL：按 status 过滤项目范围，SUM total_budget/actual_cost/contract_amount，计算余额/利润空间/本月入账/环比
   - `list(status, keyword, pageNum, pageSize)` — 项目资金分页列表，JOIN cost_posting_record 取本月入账

2. `ICostReportService` + impl
   - `preview(filterParams)` — 按时间范围+筛选维度+分组方式构建动态SQL查询，返回前100行
   - `export(filterParams, response)` — 同 preview 查全量数据，生成 .xlsx 并写入 response

### Phase 2：后端 Controller

1. `CostDashboardController` (`/cost/dashboard`)
   - `GET /summary?status=` — 权限 `cost:dashboard:view`
   - `GET /list?status=&keyword=&pageNum=&pageSize=` — 权限 `cost:dashboard:view`

2. `CostReportController` (`/cost/report`)
   - `POST /preview` — 权限 `cost:report:export`
   - `POST /export` — 权限 `cost:report:export`（返回文件流）

### Phase 3：菜单和权限

```sql
INSERT INTO sys_menu VALUES ('成本看板', 0, 5, 'cost', NULL, 1, 0, 'M', '0', '0', '', 'dashboard', 'admin', NOW(), '成本看板目录');
-- 5.1 成本总览
INSERT INTO sys_menu VALUES ('成本总览', @cost_root, 1, 'overview', 'project/costDashboard/index', 1, 0, 'C', '0', '0', 'cost:dashboard:view', 'list', 'admin', NOW(), '成本总览');
-- 5.2 报表导出
INSERT INTO sys_menu VALUES ('报表导出', @cost_root, 2, 'report', 'project/costReport/index', 1, 0, 'C', '0', '0', 'cost:report:export', 'download', 'admin', NOW(), '报表导出');
-- 权限按钮
INSERT INTO sys_menu VALUES ('报表预览', @report_menu, 1, '#', 'F', '0', '0', 'cost:report:preview', 'admin', NOW());
INSERT INTO sys_menu VALUES ('报表导出', @report_menu, 2, '#', 'F', '0', '0', 'cost:report:export', 'admin', NOW());
```

### Phase 4：前端 API 层

1. `costDashboard.js`
   - `getSummary(status)` — GET `/cost/dashboard/summary?status=`
   - `listProjects(params)` — GET `/cost/dashboard/list`

2. `costReport.js`
   - `preview(filterData)` — POST `/cost/report/preview`
   - `exportExcel(filterData)` — POST `/cost/report/export` (blob)

### Phase 5：前端页面 — 5.1 成本总览

**文件**: `views/project/costDashboard/index.vue`

关键实现：
- 6个指标卡片：el-row :gutter="12" + el-col :span="4"，el-count-to 数字动画
- 状态切换tab：el-tabs 或自定义tab-bar，切换时重新加载 summary + list
- 搜索：el-input + @keyup.enter 触发搜索
- 表格：el-table border stripe，金额列 formatMoney，利润空间绿色正/红色负
- 执行率列：内联进度条 progress-bar
- 行点击：@row-click → `$router.push('/project/projInfo/detail/' + row.projId)`

### Phase 6：前端页面 — 5.2 专项报表（4个独立页面）

每个报表页遵循统一模式：**筛选在上方，表格在下方，底部导出按钮**。

1. `views/project/costReport/projectSummary.vue` — 项目资金汇总表
   - 筛选：项目(多选) + 时间范围
   - 表格：项目编号/名称/合同金额/预算/已执行/余额/利润空间/执行率

2. `views/project/costReport/categoryDetail.vue` — 科目成本明细表
   - 筛选：项目(多选) + 科目(多选) + 时间范围
   - 表格：项目/一级科目/二级科目/预算/已执行/余额/执行率

3. `views/project/costReport/postingFlow.vue` — 入账流水明细表
   - 筛选：项目(多选) + 单据类型 + 时间范围 + 审批人
   - 表格：编号/项目/类型/科目/金额/日期/审批人

4. `views/project/costReport/nodeExecution.vue` — 节点预算执行表
   - 筛选：项目(多选) + 节点(多选) + 时间范围
   - 表格：项目/节点编号/名称/预算/已执行/余额/执行率/完成日期

---

## 4. 关键技术点

| 要点 | 实现 |
|------|------|
| 本月入账 | SQL: WHERE DATE_FORMAT(create_time,'%Y-%m') = DATE_FORMAT(NOW(),'%Y-%m') |
| 环比计算 | (本月入账 − 上月入账) / 上月入账 × 100% |
| 利润空间 | contract_amount − total_budget（正数绿色，负数红色） |
| Excel导出 | 使用 Apache POI 在 Service 层构建 .xlsx 文件 |
| 动态SQL | 报表查询用 MyBatis `<choose>/<where>/<foreach>` 动态构建 |
| 数据权限 | 非管理员角色自动加上 proj_id IN (用户关联项目) 过滤 |

---

## 5. 验证清单

1. 总览页加载 → 6指标数字正确 → 本月入账环比显示正确
2. 状态tab切换 → 指标和列表联动刷新
3. 搜索项目名称 → 列表过滤正确
4. 点击项目行 → 跳转项目详情页
5. 报表页选本月 → 预览显示本月入账明细
6. 报表页选分组维度 → 预览表格按维度交叉透视
7. 报表页点击导出 → 下载 .xlsx 文件，内容与预览一致
8. 项目经理登录 → 总览和报表仅显示本项目数据

---

*本计划为成本看板模块的完整技术实现方案。*
