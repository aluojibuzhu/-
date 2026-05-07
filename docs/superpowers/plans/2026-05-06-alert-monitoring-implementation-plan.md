# 监控预警模块实现计划

| 版本号 | 日期 | 作者 | 说明 |
|--------|------|------|------|
| V1.0 | 2026-05-06 | 产品规划团队 | 基于设计规格，制定模块4实现计划 |

> **关联文档**: [PRD_MVP.md](../../../PRD_MVP.md) · [监控预警设计规格](../specs/2026-05-06-alert-monitoring-design.md) · [前端规范设计文档](../specs/2026-04-29-frontend-specification-design.md)

---

## 1. 模块定位

实现 MVP 闭环第四阶段——监控预警。成本入账后实时感知预算异常，通过可视化看板发现问题、预警中心处理问题、规则配置定义问题。

---

## 2. 文件结构总览

```
后端 (Java)
ruoyi-system/src/main/java/com/ruoyi/system/
├── domain/project/
│   ├── ProjAlertRule.java              # 预警规则实体
│   ├── ProjAlertRecord.java           # 预警记录实体
│   └── ProjAlertLog.java              # 预警处理日志实体
├── mapper/project/
│   ├── ProjAlertRuleMapper.java       # 规则Mapper
│   ├── ProjAlertRecordMapper.java     # 记录Mapper（含看板统计SQL）
│   └── ProjAlertLogMapper.java        # 日志Mapper
├── resources/mapper/project/
│   ├── ProjAlertRuleMapper.xml
│   ├── ProjAlertRecordMapper.xml
│   └── ProjAlertLogMapper.xml
├── service/project/
│   ├── IProjAlertRuleService.java + impl
│   ├── IProjAlertRecordService.java + impl
│   ├── IProjAlertLogService.java + impl
│   └── IAlertTriggerService.java + impl   ← 核心引擎
ruoyi-admin/src/main/java/com/ruoyi/web/controller/project/
├── AlertDashboardController.java      # 看板API
├── AlertRecordController.java         # 预警中心API
└── AlertRuleController.java           # 规则配置API

后端 (Quartz)
ruoyi-quartz/src/main/java/com/ruoyi/quartz/task/
└── AlertDailyScanJob.java             # 定时巡检

前端 (Vue)
ruoyi-ui/src/
├── api/project/
│   ├── alertDashboard.js              # 看板5个API
│   ├── alertRecord.js                 # 预警中心7个API
│   └── alertRule.js                   # 规则6个API
├── views/project/
│   ├── alertDashboard/index.vue       # 4.1 看板
│   ├── alertRecord/index.vue          # 4.2 中心（含详情抽屉）
│   └── alertRule/index.vue            # 4.3 规则（含编辑弹窗）
└── utils/project.js                   # 新增 alertLevel/alertStatus/ruleType 工具函数

数据库
sql/
└── 2026-05-06_alert_monitoring_schema.sql  # 建表+预置数据+菜单+权限
```

---

## 3. 实施步骤

### Phase 1：数据库（基础）

**产出**: `sql/2026-05-06_alert_monitoring_schema.sql`

1. 创建 `proj_alert_rule` 表（预警规则配置）
2. 创建 `proj_alert_record` 表（预警记录，含数据快照字段）
3. 创建 `proj_alert_log` 表（处理日志，含操作类型枚举）
4. 创建 `proj_alert_no_sequence` 表（编号序列）
5. 插入预置5条默认规则（执行率红/橙/黄 + 单笔大额 + 预算余额）
6. 插入菜单数据（M目录1个 + C页面3个 + F按钮7个）
7. 插入字典数据（预警级别/处理状态/规则类型/预警类型 可选）

### Phase 2：后端 Entity + Mapper（数据访问层）

1. `ProjAlertRule.java` — 实体，extends BaseEntity，字段对应 proj_alert_rule
2. `ProjAlertRecord.java` — 实体，extends BaseEntity，含快照字段和状态字段
3. `ProjAlertLog.java` — 实体，含 action 枚举字段
4. `ProjAlertRuleMapper.java` + XML — CRUD + 按启用/类型查询
5. `ProjAlertRecordMapper.java` + XML — CRUD + 看板统计SQL（summary/health/budgetTrend/categoryCompare/top10）+ 联合列表查询 + 按ID更新current_exec_rate
6. `ProjAlertLogMapper.java` + XML — 按 alert_id 查询 + 批量插入

**看板统计SQL要点**：
- `summary`: 按 status='0' 计 pendingCount，按 create_time 计 todayNew/monthNew，按 status='4' 计 closedTotal
- `health`: 按 exec_rate 范围 GROUP BY 四档（<80/80-90/90-100/>=100）
- `budgetTrend`: 按月 SUM actual_cost，并 JOIN proj_info 获取月预算
- `categoryCompare`: 按 category_id GROUP BY SUM budget vs SUM actual
- `top10`: 按 exec_rate DESC LIMIT 10，JOIN proj_info

### Phase 3：后端 Service（业务逻辑层）

1. `IProjAlertRuleService` + `ProjAlertRuleServiceImpl` — 规则CRUD + 启用/停用切换
2. `IProjAlertRecordService` + `ProjAlertRecordServiceImpl` — 记录CRUD + 状态流转（confirm/ignore/follow/close）+ 看板数据聚合
3. `IProjAlertLogService` + `ProjAlertLogServiceImpl` — 日志写入（TRIGGER/NOTIFY/CONFIRM/IGNORE/FOLLOW/CLOSE）
4. `IAlertTriggerService` + `AlertTriggerServiceImpl` — **核心引擎**：
   - `checkOnPosting(projId, postingRecord)` — 入账后实时检查
     - 查询所有 enabled=1 的规则
     - 按 scope_type 过滤（全局/指定项目/指定科目）
     - 按 rule_type 执行对应计算逻辑（EXEC_RATE/SINGLE_AMOUNT/BALANCE_RATE）
     - 检查静默期：同项目同规则在 notify_silence_hours 内是否已有预警
     - 若触发 → 创建 ProjAlertRecord（含数据快照）→ 写 ProjAlertLog(TRIGGER) → 写 ProjAlertLog(NOTIFY) → 调用 ISysNoticeService.insertNotice()
   - `checkAllProjects()` — 定时巡检
     - 遍历所有 status='4'(进行中)项目
     - 应用 OVERDUE / INACTIVE 规则（入账事件无法触发）
     - 其余逻辑同 checkOnPosting

**触发引擎注入点**: `CostApprovalServiceImpl.postApproved()` 方法末尾调用 `alertTriggerService.checkOnPosting(...)`

### Phase 4：后端 Controller（REST API层）

1. `AlertDashboardController` — `/alert/dashboard`
   - `GET /summary` — 权限 `alert:dashboard:view`
   - `GET /health?projId=` — 权限 `alert:dashboard:view`
   - `GET /budgetTrend?projId=` — 权限 `alert:dashboard:view`
   - `GET /categoryCompare?projId=` — 权限 `alert:dashboard:view`
   - `GET /top10?projId=` — 权限 `alert:dashboard:view`

2. `AlertRecordController` — `/alert/record`
   - `GET /list` — 权限 `alert:record:list`（默认排序：alert_level ASC, create_time DESC）
   - `GET /{alertId}` — 权限 `alert:record:query`
   - `PUT /confirm/{alertId}` — 权限 `alert:record:handle`
   - `PUT /ignore/{alertId}` — 权限 `alert:record:handle`
   - `PUT /follow/{alertId}` — 权限 `alert:record:handle`
   - `PUT /close/{alertId}` — 权限 `alert:record:handle`
   - `GET /log/{alertId}` — 权限 `alert:record:query`

3. `AlertRuleController` — `/alert/rule`
   - `GET /list` — 权限 `alert:rule:list`
   - `GET /{ruleId}` — 权限 `alert:rule:query`
   - `POST` — 权限 `alert:rule:add`
   - `PUT` — 权限 `alert:rule:edit`
   - `PUT /toggle/{ruleId}` — 权限 `alert:rule:edit`
   - `DELETE /{ruleIds}` — 权限 `alert:rule:remove`

### Phase 5：定时任务

1. 创建 `AlertDailyScanJob.java`（放在 ruoyi-quartz task 包）
2. 在 `sys_job` 表注册：`cron: 0 0 2 * * ?`, `invoke_target: alertDailyScanJob.execute()`

### Phase 6：前端 API 层

1. `alertDashboard.js` — 5个 GET 函数（summary/health/budgetTrend/categoryCompare/top10），支持 projId 参数
2. `alertRecord.js` — 7个函数（list/get/confirm/ignore/follow/close/log）
3. `alertRule.js` — 6个函数（list/get/add/edit/toggle/remove）

### Phase 7：前端工具函数

更新 `src/utils/project.js`，新增：
- `ALERT_LEVEL_OPTIONS` / `alertLevelLabel(level)` / `alertLevelTagType(level)` / `alertLevelColor(level)`
- `ALERT_STATUS_OPTIONS` / `alertStatusLabel(status)` / `alertStatusTagType(status)`
- `ALERT_TYPE_OPTIONS` / `alertTypeLabel(type)`
- `RULE_TYPE_OPTIONS` / `ruleTypeLabel(type)`

### Phase 8：前端页面 — 4.3 预警规则配置（先做简单的）

**文件**: `views/project/alertRule/index.vue`

遵循扁平风格（减少 card 容器）：
- 页面标题 + 新增按钮
- 表格（无外层 card）：规则名称/类型(tag)/触发条件/级别(tag)/范围/通知/启用(toggle)/操作
- 新增/编辑弹窗（el-dialog）：规则名称/类型(select动态切换条件格式)/触发条件/级别(radio)/作用范围(radio+多选)/通知角色(checkbox)/静默期(number)/规则说明(textarea)
- 行内启用/停用 toggle 开关

### Phase 9：前端页面 — 4.2 预警中心

**文件**: `views/project/alertRecord/index.vue`

遵循扁平风格：
- 页面标题
- 筛选栏（无 card 包裹）：项目名称/预警级别(select)/预警类型(select)/处理状态(select)/触发时间范围(date-range)
- 工具栏：计数 + 导出
- 表格：编号/项目/级别(tag彩色)/类型/触发条件/执行率(progress-bar)/触发时间/状态(tag)/操作
- 默认排序：红色→橙色→黄色（alert_level ASC），同级别 create_time DESC
- 操作按钮根据当前状态动态显示：待处理→确认/忽略/跟进；已确认→跟进/关闭；跟进中→关闭
- 详情抽屉（el-drawer right, width=560px）：
  - 信息网格（2列）：类型/规则/预算/实际/执行率/时间
  - 处理历史时间线（纯CSS实现，避用el-timeline）
  - 底部操作按钮

### Phase 10：前端页面 — 4.1 预警看板（可视化核心）

**文件**: `views/project/alertDashboard/index.vue`

页面布局结构（从上到下，可滚动）：

*顶部*：页面标题 + 项目筛选下拉 + 刷新/导出按钮

*区块1 — 预警概览统计*：
- 4个数字卡片横排，el-row :gutter="12"，el-col :span="6"
- 复用改造 PanelGroup 数字滚动动画(count-to)
- 每个卡片可点击，点击弹出预警列表弹窗（筛选为对应状态）

*区块2 — 项目健康度总览*：
- 4个四色卡片横排（正常绿/黄/橙/红）
- 背景用透明色 + 细边框，大字+描述
- 点击弹出 el-dialog：该颜色分类下的项目列表表格

*区块3 — 图表区上行*：
- 左(60%)：预算消耗趋势折线图
  - 改造 LineChart.vue：props 接收 chartData = { months, budgetSeries, actualSeries }
  - 双线（蓝=预算/红=实际），浅色面积填充预算余额区域
  - 点击数据点弹出当月明细
- 右(40%)：成本科目消耗分布
  - 左半：分组柱状图（改造 BarChart.vue，stacked: false, 蓝/红双系列）
  - 右半：消耗占比圆环图（ECharts pie, radius: ['45%','70%']，中心文字显示总额）
  - 点击柱体/扇区弹出该科目的预警列表

*区块4 — 图表区下行*：
- 项目预警Top10
  - 左侧：横向条形图（ECharts bar，xAxis/yAxis互换，按执行率降序，红/橙/黄颜色）
  - 右侧：同步表格（项目/预算/实际/执行率进度条/级别标签）
  - 点击行或条形弹出项目摘要卡片

*图表数据联动*：顶部项目下拉切换 → 所有图表重新加载（传入 projId 参数）

### Phase 11：集成与联调

1. `CostApprovalServiceImpl.postApproved()` 方法末尾注入 `alertTriggerService.checkOnPosting(projId, postingRecord)`
2. 项目列表页 `projInfo/index.vue` 新增"预警状态"列：通过 SQL JOIN proj_alert_record 或 Service 增强，取项目最高激活预警级别
3. 项目详情页 `projInfo/detail.vue` 底部新增"预警记录"卡片：该项目最近5条预警列表
4. 端到端测试：入账 → 检查执行率 → 触发预警 → 通知铃铛 → 看板展示 → 预警中心处理 → 关闭

---

## 4. 图表组件改造方案

现有 `views/dashboard/` 下的图表组件为演示数据硬编码，需改造为 props 驱动：

| 组件 | 改造内容 |
|------|----------|
| `LineChart.vue` | 新增 `chartData` prop，watch 响应式更新，系列从写死改为动态生成 |
| `BarChart.vue` | 新增 `chartData` prop，支持双系列（预算+实际），支持堆叠/分组切换 |
| `PieChart.vue` | 新增 `chartData` prop，支持中心文字配置 |
| `PanelGroup.vue` | 新增 `panels` prop 数组，每项含{label, value, color, icon}，支持点击事件 |
| `RaddarChart.vue` | 可选用于节点维度展示 |

改造后原 dashboard 演示页不受影响（chartData 默认值即原数据）。

---

## 5. 关键设计要点

| 要点 | 实现 |
|------|------|
| 看板数据权限 | 看板API根据角色过滤：项目经理仅返回本项目数据，管理员返回全部 |
| 静默期检查 | `checkOnPosting` 中查询同 rule_id + proj_id + create_time > now()-N小时的记录数 |
| 预警编号 | `AL-YYYYMMDD-NNN`，通过序列表生成，与项目编号模式一致 |
| 数据快照 | 触发时保存 total_budget/actual_cost/exec_rate 到 record 表，current_exec_rate 持续更新 |
| 通知生成 | 调用 `ISysNoticeService.insertNotice()`，title 格式 `【级别】项目名 执行率X%` |
| 执行率实时更新 | 每次入账后检查时，更新关联的所有"待处理"状态预警记录的 current_exec_rate |
| 列表页预警列 | 通过 Service 增强或 SQL JOIN，取项目激活预警数 |
| 全局扁平风格 | 列表/表格无需外层 el-card；筛选栏直接贴边；分隔用 border-bottom；详情用 el-drawer |

---

## 6. 验证清单

1. 入账一笔超过项目预算80%的金额 → 触发黄色预警 → 通知铃铛有未读红点
2. 入账 → 执行率超过90% → 触发橙色预警 → 多个角色收到通知
3. 入账 → 执行率超过100% → 触发红色预警
4. 同一项目同规则24小时内重复入账 → 静默期不重复触发
5. 看板页面正常加载 → 健康度卡片数字正确 → 图表数据正确
6. 看板点击橙色健康卡片 → 弹窗展示3个橙色项目 → 点击项目跳转详情
7. 预警中心列表默认红色排最前 → 筛选/分页正常
8. 预警中心点击确认→状态变更→处理历史新增一条日志
9. 预警中心忽略→跟进→关闭 完整流转
10. 规则配置新增/编辑/启停正常
11. 定时巡检凌晨2点执行 → 延期项目触发预警
12. 项目列表页显示预警状态列
13. 项目详情页显示最新预警记录

---

*本计划为监控预警模块的完整技术实现方案，具体编码时遵循若依框架分层约定和前端规范。*
