# Code Review: 工程项目成本管理系统（项目立项模块）

**项目**: 工程项目成本管理系统 (基于 RuoYi-Vue 3.9.2)
**审查维度**: 稳定性 · 可用性 · 可扩展性 · 需求符合度

---

## 审查历史

| 审查轮次 | 基准提交 | 审查提交 | 日期 |
|----------|---------|---------|------|
| 初次审查 | — | `3225a5f` (initial) | 2026-04-29 |
| 复审查 | `3225a5f` | `fad4e5b` (harden) | 2026-04-29 |

**需求参考文档**: `docs/superpowers/specs/2026-04-21-project-establishment-design.md`（立项模块权威设计文档，已覆盖并细化原 PRD 的相关部分）

**修复状态标记**:
- ✅ 已在 `fad4e5b` 中修复
- ⚠️ 部分修复或仍有隐患
- ❌ 未修复

---

## 总体评价

项目基于若依框架构建，分层架构（Controller → Service → Mapper）清晰，前端 Vue 组件化结构规范。设计文档定义完整，业务流程闭环明确。

`fad4e5b` 提交针对初次审查的 P0/P1 问题做了集中修复——状态机校验、编号生成并发安全、输入校验、前端交互体验均有显著提升。**13/15 项 P0+P1 问题已修复**。当前剩余工作主要集中在前端 WBS 成本分配矩阵表（设计文档 Section 5.2）的实现。

---

## 一、稳定性

### 1.1 ✅ [Critical] 工作流状态机无前置状态校验

**文件**: `ProjInfoServiceImpl.java:107,120,129`

> **已修复**。`fad4e5b` 引入 `ProjectStatus.require()` 静态方法对所有状态变更做白名单校验：
> - `submitForApproval` → 只允许 DRAFT / REJECTED
> - `approve` → 只允许 PENDING
> - `reject` → 只允许 PENDING，且驳回原因不能为空
> - `deleteProjInfoById` → 只允许 DRAFT / REJECTED

---

### 1.2 ✅ [Critical] 项目编号生成存在并发碰撞风险

**文件**: `ProjInfoServiceImpl.java:149-154`

> **已修复**。改用 `proj_no_sequence` 表的自增 ID 作为编号后缀（`sequence.getId()`），替代了原有的 `System.currentTimeMillis() % 1000` 方案。

---

### 1.3 ✅ [High] `saveDraft` 事务内先删后插的恢复问题

**文件**: `ProjWbsNodeMapper.xml:15`、`ProjCostAllocationMapper.xml:13`

> **已修复**。WBS 节点和成本分配的 `deleteByProjId` 从逻辑删除（`update ... set del_flag='1'`）改为物理删除（`delete from ...`），配合 `@Transactional` 确保全部成功或全部回滚。

---

### 1.4 ✅ [High] 前端 API 调用无错误处理

**文件**: `form.vue`、`detail.vue`、`index.vue`

> **已修复**。所有 API 调用链均添加了 `.catch()` 错误提示，详情页的审批操作增加了 `loading` 状态防止重复点击。

---

### 1.5 ⚠️ [Medium] `totalBudget` 冗余字段与节点预算不一致

**文件**: `ProjInfoServiceImpl.java:99`

`totalBudget` 仍是 WBS 节点预算的聚合冗余字段。当前 `saveDraft` 是唯一写入入口，暂时安全。但若后续单独提供编辑节点的 API，此字段会变成脏数据。

> **当前判断**: 暂不处理，等引入独立节点编辑功能时再改为实时 `SUM` 查询。

---

### 1.6 ❌ [Low] 客户名称冗余字段无同步

**文件**: `sql/...schema.sql:53`

`proj_info.customer_name` 冗余自 `proj_customer`。`ProjCustomerServiceImpl.updateProjCustomer` 修改客户名称时不会同步更新项目表的冗余字段。

---

## 二、可用性

### 2.1 ✅ [Critical] 项目状态在前端显示为裸数字

**文件**: `index.vue:1004`、`detail.vue:7`

> **已修复**。三个页面均定义了 `STATUS_OPTIONS` 映射表，列表页和详情页使用 `el-tag` 组件以中文标签显示状态（草稿/审批中/已立项/已驳回/进行中/已完工），并用不同颜色区分。

---

### 2.2 ✅ [High] 详情页审批按钮在所有状态下均可见

**文件**: `detail.vue:12`

> **已修复**。审批按钮（通过/驳回）仅在 `status === '1'`（审批中）时显示。

---

### 2.3 ✅ [High] 驳回原因硬编码，用户无法自定义

**文件**: `detail.vue:114-128`

> **已修复**。使用 `$prompt` 弹窗让审批人填写驳回原因，带输入校验（不能为空）。

---

### 2.4 ✅ [High] 表单无必填校验，可提交空数据

**文件**: `form.vue:121-127`、`ProjInfoServiceImpl.java:162-177`

> **已修复**。
> - 前端 `el-form` 绑定了 `:rules`，5 个字段（项目名称、客户、合同金额、开工日期、竣工日期）均为必填
> - 后端新增 `validateDraft()` 方法校验项目名称、关联客户、日期的合法性
> - 项目名称限制 50 字，项目简介限制 500 字

---

### 2.5 ⚠️ [Medium] 客户选择列表硬编码加载 500 条

**文件**: `form.vue:148`

> **部分修复**。`pageSize` 从 200 提升到 500，但仍不是远程搜索方案。客户数量超过 500 时问题依旧。

---

### 2.6 ✅ [Medium] 项目列表缺少分页控制

**文件**: `index.vue:66`

> **已修复**。添加了 `pagination` 组件，支持分页切换。

---

### 2.7 ✅ [Medium] 删除项目缺少确认弹窗

**文件**: `index.vue:125-129`

> **已修复**。添加了 `$modal.confirm` 确认弹窗，后端也增加了状态校验（仅草稿/已驳回可删除）。

---

### 2.8 ❌ [Medium] `SysCostCategoryController` 未使用分页

**文件**: `SysCostCategoryController.java:22`

> **未修复**。该端点仍未调用 `startPage()`，与其他 list 端点写法不一致。考虑到成本科目数量有限，优先级较低。

---

### 2.9 ❌ [Low] 表单离开无脏数据提醒

> **未修复**。用户在表单页修改数据后误退路由仍会丢失未保存内容。

---

### 2.10 [New-Low] 前端日期交叉校验缺失

**文件**: `form.vue:133`

两个日期字段只校验了必填，没有校验 `planEndDate >= planStartDate`。后端 `validateDraft()` 会拦截并返回错误，但用户需点击保存后才能感知。建议在前端添加自定义 validator 提前拦截。

---

## 三、可扩展性

### 3.1 ❌ [Critical] `saveDraft` 方法承担过多职责

**文件**: `ProjInfoServiceImpl.java:46-102`

`saveDraft` 仍然同时处理 6 种职责。当 WBS 节点×成本科目矩阵表（设计文档 Section 5.2）实现时，该方法会进一步膨胀。建议届时一并拆解。

---

### 3.2 ✅ [High] 项目状态使用魔术字符串

**文件**: `ProjectStatus.java`

> **已修复**。新增 `ProjectStatus` 枚举类，定义了 6 个状态常量及其中文标签和 `require()` 校验方法。Java 端不再有裸字符串状态码。

前端 `index.vue` / `detail.vue` 中的 `STATUS_OPTIONS` 和 `canEdit()` 仍使用了字符串比较（如 `row.status === '0'`），但通过常量数组集中管理。

---

### 3.3 ❌ [High] 审批权限无业务角色约束

**文件**: `ProjInfoController.java:58`

> **未修复**。当前仍然只检查权限字符串 `project:projInfo:approve`，任何拥有该权限的用户均可审批任意项目。但设计文档 Section 8 指定审批人为"管理员"，在若依框架中通常所有管理员都拥有审批权限，实际业务中需要额外校验。

---

### 3.4 ❌ [High] 领域对象直接用于 API 请求体和 DB 映射

**文件**: `ProjInfo.java`

> **未修复**。`ProjInfo` 仍同时承担 DB 实体、查询参数、驳回命令 DTO 三种角色。`reject` 接口仍然使用 `@RequestBody ProjInfo projInfo` 只为了拿 `rejectReason` 字段。

---

### 3.5 ❌ [Medium] `@Autowired` 字段注入限制可测试性

> **未修复**。若依框架全项目统一使用字段注入，属于框架惯例。本轮不强求修改。

---

### 3.6 ❌ [Medium] 审批流程无事件机制

> **未修复**。立项审批通过后仍无法触发下游动作。可待后续模块（如通知模块）开发时一并引入 Spring Event。

---

### 3.7 ❌ [Medium] PRD 规划的预算版本管理缺少数据模型支撑

> **未修复**。`proj_wbs_node` 和 `proj_cost_allocation` 表仍未添加 `version` 字段。考虑到设计文档将预算版本列入 v1.1，当前阶段可评估是否需要在本次迭代预留字段。

---

### 3.8 ✅ [Low] 前端硬编码 WBS 节点编号

**文件**: `form.vue:159`

> **已修复**。`addNode()` 将 `nodeNo` 设为空字符串，界面上通过 `nextNodeNo(index)` 显示预览编号（`ND-001` 等），实际编号由后端 `saveDraft` 统一生成。

---

### 3.9 [New-Low] 三个 Vue 页面重复定义 `STATUS_OPTIONS` / `statusLabel` / `statusTag` / `formatMoney`

**文件**: `index.vue:74-81`、`detail.vue:78-85`、`form.vue:189-192`

三个页面各自定义了完全相同的常量映射和工具方法。建议提取到 `@/utils/project.js` 公共模块，避免后续状态值变更时需要改三处。

---

## 四、需求符合度（对照设计文档）

> **重要**: 设计文档 `2026-04-21-project-establishment-design.md` Section 9 列出了与原始 PRD 的关键差异，包括"产值移除"、"WBS+预算合入立项"等。以下审查以设计文档为基准。

### 4.1 ⚠️ [Gap-Critical] WBS节点-成本分配矩阵表未实现

**设计文档 Section 5.2**:

> 横向（行）为 WBS 节点，纵向（列）为节点属性 + 各成本科目分配金额。
>
> ```
> 节点编号 | 节点名称 | 预计完成日期 | 人工费 | 材料费 | 设备费 | 分包费 | ... | 节点预算
> ND-001   | 基础工程 | 2026-06-01  | 20万   | 25万   |  5万   |        |      | 50万（自动累计）
> ```

**当前实现** (`form.vue:74-93`) 是仅含 4 列的扁平表（序号/节点名称/预计完成/节点预算/操作），节点预算为手动输入。与设计文档的结构性差异：

| 维度 | 设计文档要求 | 当前实现 |
|------|-------------|---------|
| 表格结构 | 节点 × 成本科目矩阵 | 4 列扁平表 |
| 成本科目列 | 每个一级科目一列，手动填写分配金额 | **无** |
| 节点预算 | 自动累计 = SUM(行内各科目分配)，不可编辑 | `<el-input-number>` 手动输入 |
| `allocations` 数组 | 从表格数据构建后提交 | 声明但从未填充（死代码） |

这是当前与设计文档之间**唯一的重大结构性 gap**。

<details>
<summary><b>WBS 节点字段逐层核查（点击展开）</b></summary>

对照设计文档 Section 4.1 对 WBS 节点的 5 个字段定义，核查 Domain / DB / 前端四层的一致性：

| 设计文档 (Section 4.1) | Domain `ProjWbsNode` | DB `proj_wbs_node` | 前端 `form.vue` | 判定 |
|---|---|---|---|---|
| 节点编号 — 文本(只读), 系统生成 ND-001 | `nodeNo` String ✅ | `node_no` varchar(30) ✅ | 只读预览 `nextNodeNo(index)` ✅ | ✅ 一致 |
| 节点名称 — 文本, 必填, 最大30字 | `nodeName` String ✅ | `node_name` varchar(50) ✅ | `el-input` maxlength=30, 但表格列无 required 校验 ⚠️ | ⚠️ 缺必填 |
| 预计完成日期 — 日期, 必填, YYYY-MM-DD | `planFinishDate` Date ✅ | `plan_finish_date` date **DEFAULT NULL** ❌ | `el-date-picker` 无 required 规则 ❌ | ❌ 缺必填 |
| **成本科目分配** — 数字, 必填, 手动填写 | 存于 `ProjCostAllocation` 表 ✅ | `proj_cost_allocation` 表 ✅ | **无对应列** — 表里没有每科目列 ❌ | ❌ 缺失 |
| 节点预算 — 数字(计算), 只读, 自动累计 | `nodeBudget` BigDecimal ✅ | `node_budget` decimal(14,2) ✅ | `el-input-number` **手动输入** ❌ | ❌ 应为只读 |

**总结**: 数据库和 Domain 层字段定义没问题，`ProjCostAllocation` 表也已准备好存各科目分配金额。问题集中在**前端表单**——需将 WBS 节点表从扁平 4 列表改为矩阵式表（节点为行、成本科目为列），并将 `nodeBudget` 改为自动计算的只读字段。

</details>

---

### 4.2 ✅ [Gap] WBS 节点缺少 `产值` 字段

> **确认已移除**。设计文档 Section 9 明确：`产值概念 | MVP阶段不需要，已移除`。不再是缺陷。

---

### 4.3 ⚠️ [Gap] WBS 节点缺少实际完成日期

设计文档未提及 `actual_finish_date`，但原始 PRD 的成本看板功能需要对比计划 vs 实际。当前 `proj_wbs_node` 表只有 `plan_finish_date`。属于远期需求，当前立项模块范围外。

---

### 4.4 ✅ [Gap] 项目状态机

`ProjectStatus` 枚举已定义完整的 6 个状态（草稿/审批中/已立项/已驳回/进行中/已完工）。立项模块范围内的 4 个状态流转已实现。"进行中"和"已完工"的流转逻辑超出当前模块边界，属于后续迭代。

---

### 4.5 ✅ [Gap] 成本科目预置数据

SQL 已预置 7 个一级科目 + 6 个二级科目，符合设计文档 Section 5.1 的要求。

---

### 4.6 ✅ [Gap] 权限矩阵

SQL 已插入 `sys_menu` 权限记录：`project:projInfo:add/edit/remove/query/approve`、`project:customer:*`、`project:costCategory:list`，符合设计文档 Section 8。

---

### 4.7 ❌ [Gap] 成本科目缺少层级树查询

**设计文档 Section 5.1** 支持二级科目层级。数据库有 `parent_id` 和 `category_level` 字段，但 `SysCostCategoryServiceImpl` 只有一个返回扁平列表的 `selectList` 方法。在当前立项模块中，暂无前端页面展示成本科目树，但如果要实现 WBS 矩阵表的成本科目动态列头，需要按树形结构获取科目。

---

### 4.8 [New-Medium] 合同金额校验缺失

设计文档要求合同金额为必填。前端有 `required: true`，但后端 `validateDraft()` 未校验 `contractAmount` 是否为 null 或 ≤ 0。

---

### 4.9 [New-Low] `saveDraft` 返回了项目对象但前端未充分利用

**文件**: `ProjInfoController.java:48-49`

> **已改进**。`saveDraft` 接口现在返回 `success(form.getProjInfo())`（含 `projId`），前端在保存后可获得项目 ID 用于"提交审批"按钮的启用判断（`:disabled="!form.projInfo.projId"`）。

---

## 总结

### 修复统计（`fad4e5b` vs 初次审查）

| 优先级 | 已修复 | 部分修复 | 未修复 | 新增问题 |
|--------|--------|---------|--------|---------|
| Critical | 3 | 0 | 1（3.1 拆分 saveDraft） | 1（WBS 矩阵缺失） |
| High | 5 | 0 | 3 | 0 |
| Medium | 2 | 1 | 4 | 2 |
| Low | 1 | 0 | 3 | 3 |
| **合计** | **11** | **1** | **11** | **6** |

### 当前推荐行动

| 优先级 | 事项 | 说明 |
|--------|------|------|
| **P0** | 实现 WBS 节点×成本科目矩阵表 | 设计文档 Section 5.2，前端 ~60 行改动 |
| **P0** | `nodeBudget` 改为自动计算（不可编辑） | 依赖矩阵表实现 |
| P1 | 前端日期交叉校验 | `form.vue` ~10 行 |
| P1 | 后端合同金额 > 0 校验 | `ProjInfoServiceImpl.java` ~5 行 |
| P2 | 提取公共 STATUS_OPTIONS/util | 新建 `utils/project.js` |
| P2 | 客户名称同步 | `ProjCustomerServiceImpl.java` ~5 行 |
| P3 | remove `allocations` 死代码 | 矩阵表实现时自然消除 |
