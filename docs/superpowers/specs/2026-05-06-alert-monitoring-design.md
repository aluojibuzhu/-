# 监控预警模块设计文档

| 版本号 | 日期 | 作者 | 说明 |
|--------|------|------|------|
| V1.0 | 2026-05-06 | 产品规划团队 | 基于旧版PRD,重新设计模块4监控预警 |

> **关联文档**: [PRD_MVP.md](../../../PRD_MVP.md) · [前端规范设计文档](./2026-04-29-frontend-specification-design.md)

---

## 1. 模块定位

监控预警是 MVP 闭环的第四阶段。项目管理(1)→成本填报(2)→审批入账(3)→**监控预警(4)**→成本看板(5)。

核心目标：成本入账后实时感知预算异常，通过可视化看板发现问题、预警中心处理问题、规则配置定义问题边界。

### 1.1 旧设计的问题

| 问题 | 说明 |
|------|------|
| 预警维度单一 | 仅支持执行率百分比一条规则 |
| 可视化缺失 | 无仪表盘、无图表，纯列表展示 |
| 规则写死 | 80/90/100 硬编码，不可配置 |
| 无处理闭环 | 触发→通知就结束，缺少确认/跟进/关闭 |

### 1.2 重构方案

从 4 个子模块重构为 **3 个独立功能页面**：

```
旧: 4.1预算监控 / 4.2三级预警 / 4.3预警通知 / 4.4预警处理
新: 4.1预警看板 / 4.2预警中心 / 4.3预警规则配置
```

---

## 2. 视觉设计决策

本次 brainstorming 过程中确定的关键设计决策：

| 决策项 | 决定 | 理由 |
|--------|------|------|
| 基础风格 | 延续项目现有「现代轻量」风（白色底色、科技蓝#1890ff、浅色侧边栏） | 与现有页面风格统一 |
| 卡片容器 | **全局减少方块感**，弱化边框和阴影，用分隔线替代 card 包裹 | 用户偏好扁平风格 |
| 看板信息密度 | 项目经理视角，数据可下钻，可滚动 | 需要详细数据，非管理层一屏总览 |
| 下钻交互 | 弹窗/抽屉，不离开看板页面 | 保持看板上下文 |
| 预警列表排序 | 红色→橙色→黄色，同级别按触发时间倒序 | 紧急优先 |

---

## 3. 功能树

```
4. 监控预警
├── 4.1 预警看板（可视化仪表盘）
│   ├── 4.1.1 预警概览统计（待处理/今日新增/本月累计/已处理）
│   ├── 4.1.2 项目健康度总览（正常/黄色/橙色/红色，四色卡片可点击下钻）
│   ├── 4.1.3 预算消耗趋势图（双线折线图：预算总额 vs 实际成本累计，近12个月）
│   ├── 4.1.4 成本科目消耗分布（分组柱状图 + 消耗占比圆环图）
│   └── 4.1.5 项目预警Top10（横向条形图 + 同步表格，可点击下钻）
│
├── 4.2 预警中心
│   ├── 4.2.1 预警列表（按级别/类型/状态/时间筛选，红色→橙色→黄色默认排序）
│   ├── 4.2.2 预警详情抽屉（右侧滑出，信息网格 + 预算对比 + 处理历史时间线）
│   └── 4.2.3 处理操作（确认/忽略/跟进/关闭 + 处理日志记录）
│
└── 4.3 预警规则配置
    ├── 4.3.1 规则列表（表格 + 行内启用开关）
    ├── 4.3.2 5种规则类型（执行率/单笔大额/余额不足/项目延期/静默无入账）
    ├── 4.3.3 灵活阈值（每规则独立设定触发条件和预警级别）
    ├── 4.3.4 作用范围（全局/指定项目/指定科目 可多选）
    └── 4.3.5 通知配置（通知角色多选 + 静默期设置 + 启用/停用）
```

---

## 4. 页面设计

### 4.1 预警看板 — 布局结构

```
┌─ 页面标题 + 操作栏（项目筛选下拉 / 刷新 / 导出）──────────────┐
├─ 预警概览统计（4个数字卡片，点击可下钻到预警列表）─────────────┤
├─ 项目健康度总览（4个四色卡片：正常/黄/橙/红，点击弹出分类列表）─┤
├─ 图表区上行 ─────────────────────────────────────────────┤
│  预算消耗趋势（折线图，12月）  │  科目消耗（柱状图+圆环图）   │
├─ 图表区下行 ─────────────────────────────────────────────┤
│  项目预警Top10（横向条形图 + 同步表格，点击弹出项目详情卡）   │
└──────────────────────────────────────────────────────────┘
```

**看板交互规则**：
- 所有统计卡片和图表区域均可点击，弹出下钻弹窗
- 弹窗内展示对应分类的项目列表，含预算/实际/执行率
- 弹窗内可进一步跳转到项目详情页或预警中心
- 页面支持项目筛选下拉（全部项目/单个项目），切换后所有图表联动刷新

### 4.2 预警中心 — 布局结构

```
┌─ 页面标题 ──────────────────────────────────────────────┐
├─ 筛选栏（无 card 包裹，直接贴边）────────────────────────┤
│  项目名称 / 预警级别 / 预警类型 / 处理状态 / 触发时间范围  │
│  [搜索] [重置]                                          │
├─ 工具栏（共 N 条 · [导出]）─────────────────────────────┤
├─ 预警表格 ─────────────────────────────────────────────│
│  编号 | 项目 | 级别(tag) | 类型 | 触发条件 | 执行率(进度条)│
│  触发时间 | 状态(tag) | [确认][忽略][跟进][关闭]         │
├─ 分页 ─────────────────────────────────────────────────┤
└────────────────────────────────────────────────────────┘

点击预警编号 → 右侧抽屉滑出
  ┌─ 预警详情 ────────────────────┐
  │ 基本信息网格（2列）            │
  │ 预算执行对比                  │
  │ 处理历史时间线                │
  │ [确认] [忽略] [跟进] [关闭]   │
  └──────────────────────────────┘
```

**预警状态流转**：
```
待处理(0) ──→ 已确认(1) ──→ 跟进中(3) ──→ 已关闭(4)
   │            │
   └──→ 已忽略(2) ──→ (同项目同规则不再触发，直到下次检查)
```

**生命周期日志**：每次操作（触发/通知/确认/忽略/跟进/关闭）写入 `proj_alert_log`

### 4.3 预警规则配置 — 布局结构

```
┌─ 页面标题 + [新增规则] ─────────────────────────────────┐
├─ 工具栏（共 N 条 · 启用 N 条）──────────────────────────┤
├─ 规则表格 ─────────────────────────────────────────────│
│  # | 名称 | 类型(tag) | 触发条件 | 级别(tag) | 范围 |   │
│  通知 | 启用(toggle) | [编辑][删除]                     │
└────────────────────────────────────────────────────────┘

点击新增/编辑 → 弹窗
  ┌─ 新增/编辑预警规则 ──────────┐
  │ 规则名称                     │
  │ 规则类型（下拉，5种）         │
  │ 触发条件（动态格式：         │
  │   执行率 ≥ [90] %            │
  │   单笔金额 ≥ [50000] 元      │
  │   余额 ≤ [10] %              │
  │   超竣工日 ≥ [30] 天          │
  │   连续 ≥ [3] 月无入账）       │
  │ 预警级别（单选：黄/橙/红）    │
  │ 作用范围（全局/指定项目/科目） │
  │ 通知角色（多选：项目经理/     │
  │   成本会计/管理员）           │
  │ 静默期（小时，0=不限制）      │
  │ 规则说明                     │
  │ 启用开关                     │
  │         [取消] [保存]        │
  └──────────────────────────────┘
```

---

## 5. 数据库设计

### 5.1 预警规则表 `proj_alert_rule`

| 字段 | 类型 | 说明 |
|------|------|------|
| `rule_id` | bigint PK | 规则ID |
| `rule_name` | varchar(100) | 规则名称 |
| `rule_type` | varchar(20) | 规则类型: EXEC_RATE / SINGLE_AMOUNT / BALANCE_RATE / OVERDUE / INACTIVE |
| `trigger_condition` | varchar(50) | 触发条件表达式，如 ">=80", ">=50000", "<=10" |
| `alert_level` | char(1) | 预警级别: 1黄色/2橙色/3红色 |
| `scope_type` | char(1) | 作用范围: 0全局/1指定项目/2指定科目 |
| `scope_proj_ids` | varchar(2000) | 指定项目ID列表(JSON) |
| `scope_category_ids` | varchar(2000) | 指定科目ID列表(JSON) |
| `notify_roles` | varchar(200) | 通知角色(JSON) |
| `notify_silence_hours` | int(4) | 静默期(小时)，0=不限制 |
| `enabled` | char(1) | 启用: 0停用/1启用 |
| 审计字段 | — | `order_num` / `remark` / `create_by` / `create_time` / `update_by` / `update_time` |

预置 5 条默认规则（执行率红/橙/黄 + 大额 + 余额）。

### 5.2 预警记录表 `proj_alert_record`

| 字段 | 类型 | 说明 |
|------|------|------|
| `alert_id` | bigint PK | 预警ID |
| `alert_no` | varchar(30) | 预警编号: AL-YYYYMMDD-NNN |
| `rule_id` | bigint FK | 关联规则ID |
| `rule_name` | varchar(100) | 规则名称快照 |
| `alert_level` | char(1) | 预警级别: 1/2/3 |
| `alert_type` | varchar(20) | 预警类型: PROJECT/NODE/CATEGORY |
| `proj_id` / `proj_no` / `proj_name` | — | 关联项目 |
| `node_id` / `node_name` | — | 关联节点(可空) |
| `category_id` / `category_name` | — | 关联科目(可空) |
| `trigger_condition` | varchar(100) | 触发条件描述 |
| `trigger_exec_rate` | decimal(5,2) | 触发时执行率(%) |
| `current_exec_rate` | decimal(5,2) | 当前执行率(%)，实时更新 |
| `total_budget_snapshot` / `actual_cost_snapshot` | decimal(14,2) | 触发时数据快照 |
| `status` | char(1) | 处理状态: 0待处理/1已确认/2已忽略/3跟进中/4已关闭 |
| `handle_by` / `handle_time` / `handle_desc` | — | 处理信息 |
| `close_by` / `close_time` / `close_reason` | — | 关闭信息 |
| 审计字段 | — | `del_flag` / `create_by` / `create_time` / `update_by` / `update_time` |

### 5.3 预警处理日志表 `proj_alert_log`

| 字段 | 类型 | 说明 |
|------|------|------|
| `log_id` | bigint PK | 日志ID |
| `alert_id` | bigint FK | 关联预警ID |
| `action` | varchar(20) | TRIGGER/NOTIFY/CONFIRM/IGNORE/FOLLOW/CLOSE |
| `content` | varchar(500) | 操作描述 |
| `operator` | varchar(64) | 操作人 |
| `operate_time` | datetime | 操作时间 |

### 5.4 编号序列表 `proj_alert_no_sequence`

单列 `id` bigint AUTO_INCREMENT，生成预警编号。

---

## 6. 预警规则引擎

### 6.1 五种规则类型

| 类型 | 编码 | 触发条件格式 | 计算逻辑 |
|------|------|-------------|----------|
| 预算执行率 | EXEC_RATE | `>=X` | `(actualCost / totalBudget) * 100 >= X` |
| 单笔入账金额 | SINGLE_AMOUNT | `>=X` (元) | `postingRecord.amount >= X` |
| 预算余额比例 | BALANCE_RATE | `<=X` | `((totalBudget-actualCost)/totalBudget) * 100 <= X` |
| 项目延期 | OVERDUE | `>=X` (天) | `now() - plan_end_date >= X AND status != 5` |
| 静默无入账 | INACTIVE | `>=X` (月) | `MAX(posting_time) < now() - X months` |

### 6.2 触发时机

**时机A — 入账后实时触发（同步）**：
在 `CostApprovalServiceImpl` 入账完成后注入调用 `AlertTriggerService.checkOnPosting(projId, postingRecord)`，匹配所有启用规则，生成预警+写日志+发通知。

**时机B — 定时巡检（异步）**：
Quartz 定时任务，每日凌晨2点执行 `AlertDailyScanJob`，遍历所有进行中项目，应用 OVERDUE/INACTIVE 规则（这两种规则无法通过入账事件触发）。

### 6.3 静默期机制

若 `notify_silence_hours > 0`，查询同项目同规则在静默期内是否已有预警记录，存在则跳过。

### 6.4 通知集成

复用若依 `sys_notice` 表 + `HeaderNotice` 铃铛组件。通过 `notify_roles` 配置查找对应用户，调用 `ISysNoticeService.insertNotice()`。

---

## 7. 后端设计

### 7.1 文件清单

```
ruoyi-system/src/main/java/com/ruoyi/system/
├── domain/project/
│   ├── ProjAlertRule.java
│   ├── ProjAlertRecord.java
│   └── ProjAlertLog.java
├── mapper/project/
│   ├── ProjAlertRuleMapper.java + XML
│   ├── ProjAlertRecordMapper.java + XML
│   └── ProjAlertLogMapper.java + XML
└── service/project/
    ├── IProjAlertRuleService.java + impl
    ├── IProjAlertRecordService.java + impl
    ├── IProjAlertLogService.java + impl
    └── IAlertTriggerService.java + impl  ← 核心引擎

ruoyi-admin/src/main/java/com/ruoyi/web/controller/project/
├── AlertDashboardController.java   # 看板API
├── AlertRecordController.java      # 预警中心API
└── AlertRuleController.java        # 规则配置API

ruoyi-quartz/.../task/
└── AlertDailyScanJob.java          # 定时巡检
```

### 7.2 关键 API

**AlertDashboardController** (`/alert/dashboard`):
- `GET /summary` — 预警概览统计
- `GET /health` — 健康度四分类
- `GET /budgetTrend?projId=` — 预算消耗趋势数据
- `GET /categoryCompare?projId=` — 科目消耗分布数据
- `GET /top10?projId=` — 项目预警Top10

**AlertRecordController** (`/alert/record`):
- `GET /list` — 预警列表（分页，默认按级别+时间排序）
- `GET /{alertId}` — 预警详情
- `PUT /confirm/{alertId}` — 确认
- `PUT /ignore/{alertId}` — 忽略
- `PUT /follow/{alertId}` — 跟进
- `PUT /close/{alertId}` — 关闭
- `GET /log/{alertId}` — 处理日志

**AlertRuleController** (`/alert/rule`):
- `GET /list` — 规则列表
- `GET /{ruleId}` — 规则详情
- `POST` — 新增规则
- `PUT` — 编辑规则
- `PUT /toggle/{ruleId}` — 启停开关
- `DELETE /{ruleIds}` — 删除

### 7.3 触发引擎注入点

`CostApprovalServiceImpl.postApproved()` 方法末尾注入：
```java
alertTriggerService.checkOnPosting(postingRecord.getProjId(), postingRecord);
```

---

## 8. 前端设计

### 8.1 文件清单

```
ruoyi-ui/src/
├── api/project/
│   ├── alertDashboard.js
│   ├── alertRecord.js
│   └── alertRule.js
├── views/project/
│   ├── alertDashboard/index.vue    # 4.1 看板
│   ├── alertRecord/index.vue       # 4.2 中心（含详情抽屉）
│   └── alertRule/index.vue         # 4.3 规则（含编辑弹窗）
└── utils/project.js                # 新增 alertLevel/alertStatus/ruleType 工具函数
```

### 8.2 图表复用

复用并改造现有 ECharts 组件（`views/dashboard/` 下的 LineChart/BarChart/PieChart），改为 props 驱动动态数据。新增 PanelGroup 统计卡片和横向条形图。

### 8.3 页面风格

遵循前端规范，减少方块容器：列表/表格区域不额外包裹 card，直接用分隔线区分；筛选栏无背景容器；弹窗/抽屉保留圆角和边框。

---

## 9. 菜单与权限

### 9.1 菜单

| 菜单名 | 类型 | 路径 | 权限标识 | 可见 |
|--------|------|------|----------|------|
| 监控预警 | M | `monitor` | — | 侧边栏目录 |
| 预警看板 | C | `dashboard` | `alert:dashboard:view` | 可见 |
| 预警中心 | C | `record` | `alert:record:list` | 可见 |
| 预警规则 | C | `rule` | `alert:rule:list` | 可见 |

### 9.2 权限矩阵

| 操作 | 项目经理 | 成本会计 | 项目成员 | 管理员 |
|------|----------|----------|----------|--------|
| 看板 | 本项目 | 本项目 | — | 全部 |
| 预警列表/处理 | 本项目 | 本项目 | — | 全部 |
| 规则配置 | — | — | — | ✓ |

---

## 10. 与现有模块集成

- **3.2 成本审批** → 入账后实时触发预警检查
- **1.2 项目列表** → 新增"预警状态"列（取该项目最高激活预警级别）
- **1.3 项目详情** → 底部新增"预警记录"卡片（该项目最近5条）
- **通知** → 复用 `sys_notice` + `HeaderNotice` 铃铛
- **定时** → Quartz `sys_job` 注册 `AlertDailyScanJob`

---

*本文档为监控预警模块的完整设计规格说明。*
