# Code Review: Initial Commit (3225a5f)

**项目**: 工程项目成本管理系统 (基于 RuoYi-Vue 3.9.2)
**提交**: `chore: initial commit`
**日期**: 2026-04-29
**审查维度**: 稳定性 · 可用性 · 可扩展性 · 需求符合度

---

## 总体评价

项目基于若依框架构建，分层架构（Controller → Service → Mapper）清晰，前端 Vue 组件化结构规范。PRD 文档定义完整，业务流程闭环明确。当前代码实现了项目立项、客户管理和成本科目配置的基本 CRUD。

但四个审查维度均存在需要关注的问题，下文按维度逐一展开。

---

## 一、稳定性

### 1.1 [Critical] 工作流状态机无前置状态校验

**文件**: `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/service/project/impl/ProjInfoServiceImpl.java:92-115`

`submitForApproval`、`approve`、`reject` 三个方法**直接覆盖状态字段**，未校验当前状态是否允许转入目标状态：

```java
// submitForApproval — 任何状态的项目都可以提交审批
public int submitForApproval(Long projId, String username) {
    ProjInfo info = projInfoMapper.selectProjInfoById(projId);
    info.setStatus("1");  // 无前置状态检查
    return projInfoMapper.updateProjInfo(info);
}
```

**可复现的异常场景**：

| 操作 | 当前状态 | 调用方法 | 结果 |
|------|---------|---------|------|
| 已立项项目再次提交审批 | `2`（已立项） | `submitForApproval` | 状态变为 `1`（审批中）— 数据错误 |
| 草稿项目直接审批 | `0`（草稿） | `approve` | 跳过审批流程直接变为 `2`（已立项） |
| 已驳回项目直接审批 | `3`（已驳回） | `approve` | 绕过重新提交，直接生效 |

**建议**: 在每个状态变更方法开头添加状态机白名单校验，对非法转换抛出 `ServiceException`。

```java
// 只允许 草稿(0) 或 已驳回(3) 状态的项目提交审批
if (!List.of("0", "3").contains(info.getStatus())) {
    throw new ServiceException("当前项目状态不允许提交审批");
}
```

---

### 1.2 [Critical] 项目编号生成存在并发碰撞风险

**文件**: `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/service/project/impl/ProjInfoServiceImpl.java:124-128`

```java
private String generateProjNo() {
    String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
    long suffix = System.currentTimeMillis() % 1000;  // 0~999
    return "PRJ-" + date + "-" + String.format("%03d", suffix);
}
```

**问题**:
- `System.currentTimeMillis() % 1000` 在同一毫秒内的并发请求生成**相同编号**
- 数据库 `proj_info` 表有 `uk_proj_no` 唯一约束，并发碰撞会直接导致 `DataIntegrityViolationException`，用户看到的是 500 错误

**矛盾点**: SQL 中已建好 `proj_no_sequence` 序列表（用于自增流水号），但代码完全未使用，反而用时间戳取模。

**建议**: 使用 `proj_no_sequence` 表的自增 ID 作为流水号后缀，或使用 Redis `INCR`。

---

### 1.3 [High] `saveDraft` 事务内先删后插的恢复问题

**文件**: `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/service/project/impl/ProjInfoServiceImpl.java:62-63`

```java
@Transactional
public int saveDraft(ProjInfoFormVO form, String username) {
    // ... 插入或更新项目主表 ...
    wbsNodeMapper.deleteByProjId(projId);      // 逻辑删除旧节点
    allocationMapper.deleteByProjId(projId);    // 逻辑删除旧分配
    // 然后逐个插入新节点和分配...
}
```

**分析**:
- Mapper XML 中的 `deleteByProjId` 是 `update ... set del_flag='1'`（逻辑删除），不是物理删除
- 如果插入新节点中途失败（如违反约束），事务回滚后 `del_flag='1'` 的旧数据**不会被恢复**（因为 `@Transactional` 仅回滚事务内的 SQL，逻辑删除的 UPDATE 已经提交了一部分）
- 实际上由于方法标注了 `@Transactional`，所有操作在同一个事务中，回滚时会一并撤销 — **但如果事务隔离级别配置不当或有嵌套事务，仍存在风险**
- 更关键的是：每次保存草稿产生大量 `del_flag='1'` 的废弃数据

**建议**: 
- 改为先插入新数据，成功后再清理旧数据
- 或改为物理删除（非逻辑删除），因为这是"替换"语义而非"删除"语义

---

### 1.4 [High] 前端 API 调用无错误处理

**文件**: `RuoYi-Vue/ruoyi-ui/src/views/project/projInfo/form.vue:38`、`detail.vue:18-19`、`index.vue:26`

所有 Vue 组件中的异步调用都没有 `.catch()` 错误处理：

```js
// form.vue — 保存失败时无用户提示
saveDraft(this.form).then(() => this.$message.success('保存成功'))
// 缺少 .catch(err => this.$message.error('保存失败: ' + err))

// detail.vue — 审批失败时无提示
approve(this.form.projInfo.projId).then(() => this.$message.success('已通过'))
// 缺少 .catch(...)

// index.vue — 提交失败时无提示
submitForApproval(row.projId).then(() => { this.$message.success('已提交'); this.load() })
// 缺少 .catch(...)
```

**影响**: 后端返回 500 错误时用户完全不知道操作是否成功。

---

### 1.5 [Medium] `totalBudget` 冗余字段与节点预算不一致

**文件**: `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/service/project/impl/ProjInfoServiceImpl.java:87`

```java
info.setTotalBudget(total);        // 累加节点预算写入项目表
projInfoMapper.updateProjInfo(info);
```

`proj_info.total_budget` 是 WBS 节点预算的聚合冗余字段。当前**只有 `saveDraft` 一个入口更新它**。未来如果通过其他接口（如单独编辑节点的 API）修改节点预算，`total_budget` 会变成脏数据。

**建议**: 要么删除冗余字段改为实时 `SUM` 查询，要么在 ProjWbsNodeMapper 的每次写操作后触发 `total_budget` 重算。

---

### 1.6 [Low] 客户名称冗余字段无同步

**文件**: `RuoYi-Vue/sql/2026-04-21_project_establishment_schema.sql:38`

```sql
`customer_name` varchar(100) DEFAULT NULL COMMENT '客户名称（冗余）'
```

`proj_info.customer_name` 冗余自 `proj_customer` 表。在 `saveDraft` 中前端手动将选中的客户名写入 `form.projInfo.customerName`，但 `ProjCustomerServiceImpl.updateProjCustomer` 修改客户名称时不会更新 `proj_info` 中已关联的记录。

---

## 二、可用性

### 2.1 [Critical] 项目状态在前端显示为裸数字

**文件**: `RuoYi-Vue/ruoyi-ui/src/views/project/projInfo/index.vue:9`、`detail.vue:4`

```html
<!-- 列表页 — 直接显示数据库值 -->
<el-table-column prop="status" label="状态" />

<!-- 详情页 — 直接拼接数据库值 -->
<p>状态：{{ form.projInfo.status }}</p>
```

用户看到的是 `0`、`1`、`2`、`3`，而非"草稿"、"审批中"、"已立项"、"已驳回"。

**建议**: 使用字典映射或过滤器将状态码转为中文文本。

---

### 2.2 [High] 详情页审批按钮在所有状态下均可见

**文件**: `RuoYi-Vue/ruoyi-ui/src/views/project/projInfo/detail.vue:7-8`

```html
<el-button type="success" @click="approveIt">通过</el-button>
<el-button type="danger" @click="rejectIt">驳回</el-button>
```

无论项目处于什么状态（草稿、已立项、已驳回），审批按钮始终显示。用户点击后，如果后端加了状态校验（见 1.1），会收到一个 500 错误；如果后端没加校验，会产生数据错误。

**建议**: 仅在 `status === '1'`（审批中）时显示审批按钮，与列表页的处理方式保持一致（列表页提交按钮已正确判断了 `v-if="scope.row.status==='0'"`）。

---

### 2.3 [High] 驳回原因硬编码，用户无法自定义

**文件**: `RuoYi-Vue/ruoyi-ui/src/views/project/projInfo/detail.vue:19`

```js
rejectIt() {
    reject(this.form.projInfo.projId, { rejectReason: '需补充信息' })
        .then(() => this.$message.success('已驳回'))
}
```

驳回原因固定为"需补充信息"，审批人无法填写具体原因。被驳回的项目经理无法知道哪里需要修改。

**建议**: 弹出输入框让用户填写驳回原因。

---

### 2.4 [High] 表单无必填校验，可提交空数据

**文件**: `RuoYi-Vue/ruoyi-ui/src/views/project/projInfo/form.vue:4-11`

```html
<el-form :model="form.projInfo" label-width="110px">
  <!-- 没有 :rules -->
  <el-form-item label="项目名称">
    <el-input v-model="form.projInfo.projName" />
    <!-- 没有 required 标记 -->
  </el-form-item>
```

前后端均无输入校验：
- **前端**: `el-form` 未绑定 `:rules`，`el-form-item` 未标注 `required`
- **后端**: Domain 类无 JSR-303 注解（`@NotBlank`、`@NotNull`），Controller 参数无 `@Valid`

用户可以不填项目名称、不选客户直接保存，产生脏数据。

---

### 2.5 [Medium] 客户选择列表硬编码加载 200 条

**文件**: `RuoYi-Vue/ruoyi-ui/src/views/project/projInfo/form.vue:33`

```js
loadCustomers() {
    listCustomers({ pageNum: 1, pageSize: 200 })
        .then(r => this.customers = r.rows || [])
}
```

客户数量超过 200 时，后面的客户不可见且不可搜索。应使用分页加载或远程搜索的下拉组件。

---

### 2.6 [Medium] 项目列表缺少分页控制

**文件**: `RuoYi-Vue/ruoyi-ui/src/views/project/projInfo/index.vue:4`

```html
<el-table :data="list">  <!-- 未使用 el-pagination -->
```

虽然后端已通过 `startPage()` 支持分页，前端列表页没有分页组件，数据量增大后用户无法翻页。

---

### 2.7 [Medium] 删除项目后客户列表页无确认弹窗

**文件**: `RuoYi-Vue/ruoyi-ui/src/views/project/projInfo/index.vue`

列表页没有任何删除按钮，但后端 Controller 定义了 `DELETE /{projId}` 接口。前端应该提供带确认弹窗的删除操作（仅限草稿状态）。

---

### 2.8 [Medium] `SysCostCategoryController` 未使用分页

**文件**: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/project/SysCostCategoryController.java:22`

```java
@GetMapping("/list")
public AjaxResult list() {
    return success(categoryService.selectSysCostCategoryList(new SysCostCategory()));
}
```

与项目中其他 list 端点不同，此处未调用 `startPage()`。成本科目作为系统配置数据量一般不大，但写法不一致，且如果后续扩展到二级科目，数量可能增长。建议统一分页。

---

### 2.9 [Low] 表单离开无脏数据提醒

用户在项目表单页填写了内容但未保存时，如果误点浏览器后退或在侧边栏切换路由，填写的所有数据都会丢失，且无任何提示。

---

## 三、可扩展性

### 3.1 [Critical] `saveDraft` 方法承担过多职责

**文件**: `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/service/project/impl/ProjInfoServiceImpl.java:42-90`

当前 `saveDraft` 方法同时处理：
1. 项目主表的新增/更新判断
2. 项目编号生成
3. WBS 节点的全量删除重建
4. 节点编号生成
5. 成本分配的全量删除重建
6. 总预算累加计算

所有逻辑耦合在一个方法中，未来任何一处的业务规则变化（如：分配需要按科目总额校验、节点需要关联负责人）都必须修改这个方法。

**建议**: 按职责拆分为：
- `ProjInfoService.saveDraft()` — 仅处理项目主表
- `WbsNodeService.replaceNodes()` — 处理节点替换逻辑
- `AllocationService.replaceAllocations()` — 处理分配替换逻辑
- `BudgetService.recalculateTotal()` — 预算重算

---

### 3.2 [High] 项目状态使用魔术字符串

**文件**: 遍布 `ProjInfoServiceImpl.java`、前端 `index.vue`、`detail.vue`

状态值 `"0"`、`"1"`、`"2"`、`"3"` 以硬编码字符串形式散落在 Java 和 JavaScript 代码中：

```java
// Java
info.setStatus("0");  // 草稿
info.setStatus("1");  // 审批中

// Vue
v-if="scope.row.status==='0'"  // 草稿
```

**后果**:
- 新增状态（如 PRD 中的"进行中"、"已完工"）需要全局搜索替换
- 没有编译期检查，`"1"` 和 `1` 在前后端交互时容易混淆
- 状态流转规则散落各处，无法集中维护

**建议**: 定义 `ProjectStatus` 枚举类，包含状态值和合法的流转目标。

```java
public enum ProjectStatus {
    DRAFT("0", "草稿", Set.of("1")),           // 只能提交审批
    PENDING("1", "审批中", Set.of("2", "3")),   // 可通过或驳回
    APPROVED("2", "已立项", Set.of("4")),       // 可开始施工
    REJECTED("3", "已驳回", Set.of("0", "1")),  // 可修改或重新提交
    IN_PROGRESS("4", "进行中", Set.of("5")),
    COMPLETED("5", "已完工", Set.of());
    // ...
}
```

---

### 3.3 [High] 审批权限无业务角色约束

**文件**: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/project/ProjInfoController.java:58`

```java
@PreAuthorize("@ss.hasPermi('project:projInfo:approve')")
@PutMapping("/approve/{projId}")
public AjaxResult approve(@PathVariable Long projId) {
    return toAjax(projInfoService.approve(projId, getUsername()));
}
```

当前只检查了权限字符串，未校验操作人与项目的关系。PRD 规定"企业主审批立项"，但代码中任何拥有 `project:projInfo:approve` 权限的用户都可以审批任意项目 — 包括**项目经理审批自己创建的项目**。

**建议**: 在 `approve` 方法中增加业务校验 — 审批人不能是申请人。

---

### 3.4 [High] 领域对象直接用于 API 请求体和 DB 映射

**文件**: `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/project/ProjInfo.java`

`ProjInfo` 类同时扮演了三种角色：
1. MyBatis 实体映射（`resultMap` 指向它）
2. Controller 查询参数绑定（`list(ProjInfo projInfo)`）
3. 驳回请求体参数（`reject` 方法中 `@RequestBody ProjInfo projInfo` 只为拿 `rejectReason`）

**后果**: 修改数据库字段会影响 API 接口，修改 API 结构会影响 DB 映射。这是典型的贫血模型过度复用。

**建议**: 至少分离出：
- `ProjInfo` — 数据库实体
- `ProjInfoQuery` — 查询参数 DTO
- `ProjRejectCommand` — 驳回命令 DTO

---

### 3.5 [Medium] `@Autowired` 字段注入限制可测试性

**文件**: 所有 Controller 和 Service 类

全部使用字段注入：
```java
@Autowired
private IProjInfoService projInfoService;
```

**影响**: 
- 无法在单元测试中不启动 Spring 容器直接 mock 依赖
- 无法将依赖声明为 `final`，编译期无法发现未注入的依赖
- 无法发现循环依赖

**建议**: 统一改为构造函数注入（Spring 4.3+ 自动识别）。

---

### 3.6 [Medium] 审批流程无事件机制

当项目审批通过时，除了修改状态字段，没有任何扩展点让后续模块（预算、通知）感知此事件。未来添加"审批通过后自动初始化预算"或"发送通知"功能时，需要修改 `approve` 方法。

**建议**: 使用 Spring Event 机制，在 `approve` 成功后发布 `ProjectApprovedEvent`，让其他模块通过 `@EventListener` 订阅。

---

### 3.7 [Medium] PRD 规划的预算版本管理缺少数据模型支撑

PRD 明确要求在 v1.1 支持"预算调整申请"，但当前数据库设计中 `proj_wbs_node` 和 `proj_cost_allocation` 表没有 `version` 字段，每次 `saveDraft` 都是直接替换。这意味着未来实现预算版本管理需要大量重构。

**建议**: 在当前阶段就给这两张表添加 `version` 字段（即使暂时不使用），避免后期数据迁移。

---

### 3.8 [Low] 前端硬编码 WBS 节点编号

**文件**: `RuoYi-Vue/ruoyi-ui/src/views/project/projInfo/form.vue:34`

```js
addNode() {
    this.form.wbsNodes.push({
        nodeNo: 'ND-' + String(this.form.wbsNodes.length + 1).padStart(3, '0'),
        ...
    })
}
```

前端生成了节点编号，但后端 `saveDraft` 会再次用 `generateNodeNo(i+1)` 覆盖。前端生成的值被完全忽略。逻辑重复且不一致 — 如果未来编号规则变化，需要改两处。

---

## 四、需求符合度

### 4.1 [Critical] WBS 节点缺少 `产值` 字段

**PRD 原文** (3.3.1 WBS进度节点设置):

> | 字段 | 类型 | 必填 | 说明 |
> |------|------|------|------|
> | 节点产值 | 数字 | 是 | 单位：元 |

**实际情况**: `ProjWbsNode` 类、`proj_wbs_node` 表、Mapper XML 中均**没有** `产值` 字段。PRD 还规定了"节点产值总和 = 项目总产值"，这个校验规则也无法实现。

---

### 4.2 [Critical] WBS 节点缺少实际完成日期

PRD 中成本看板的"执行对比"功能需要对比计划 vs 实际，但 `proj_wbs_node` 表只有 `plan_finish_date`（计划完成日期），没有 `actual_finish_date`。

---

### 4.3 [High] 项目状态机缺少"进行中"和"已完工"两个状态

**PRD 定义的状态**: 草稿 → 审批中 → 已立项 → 进行中 → 已完工

**代码实现**: 0 → 1 → 2 → 3（无 4、5 的流转逻辑）

SQL 建表语句中定义了 `4进行中 5已完工`，但 `ProjInfoServiceImpl` 中没有任何方法将项目状态从"已立项"推进到"进行中"或"已完工"。

---

### 4.4 [High] 成本科目缺少层级树查询功能

**PRD 描述**:

> 成本科目支持两级（一级和二级），如 人工费 → 技工/普工，材料费 → 钢材/水泥

数据库 `sys_cost_category` 表有 `parent_id` 和 `category_level` 字段支持层级结构，但 `SysCostCategoryServiceImpl` 只有一个 `selectList` 方法，返回扁平列表，前端也没有按树形结构展示。用户无法创建和管理二级科目。

---

### 4.5 [High] PRD 核心模块完成度

| PRD 模块 | 功能点 | 后端 | 前端 | 完成度 |
|----------|--------|------|------|--------|
| 项目管理 | 项目立项（CRUD+审批） | ✅ | ✅ | 60% — 缺少搜索、分页、删除UI |
| 项目管理 | 客户管理 | ✅ | ✅（仅弹窗创建） | 40% — 无客户列表管理页、无编辑/删除 |
| 预算管理 | WBS节点设置 | ✅ | ✅（仅基本添加） | 30% — 在立项表单内，无独立预算编制页 |
| 预算管理 | 成本科目配置 | ✅ | ❌ 无前端页面 | 10% — 仅有后端接口 |
| 预算管理 | 预算审批 | ❌ | ❌ | 0% |
| 成本管理 | 工时填报 | ❌ | ❌ | 0% |
| 成本管理 | 报销申请 | ❌ | ❌ | 0% |
| 成本管理 | 成本审批入账 | ❌ | ❌ | 0% |
| 监控预警 | 三级预警 | ❌ | ❌ | 0% |
| 成本看板 | 项目总览/执行对比/盈亏分析 | ❌ | ❌ | 0% |

**整体完成度约 15%**。如果这是 MVP 首次提交，当前实现仅覆盖了项目立项这一个业务环节。

---

### 4.6 [Medium] 项目列表缺少按状态筛选

**PRD 原文** (3.2.2 项目列表):

> 展示所有项目，支持按状态筛选

后端 `ProjInfoMapper.xml` 的 `selectProjInfoList` 已支持 `status` 参数筛选，但前端列表页没有提供状态下拉筛选器。

---

### 4.7 [Medium] 工时精度规则未在数据模型中体现

**PRD 原文** (5.2):

> 工时精度：0.5的倍数

工时模块尚未实现，但如果后续建表时应确保 `hour` 字段使用支持 0.5 精度的类型（如 `decimal(6,1)`），而非整数类型。

---

## 总结

### 各维度问题数量

| 维度 | Critical | High | Medium | Low |
|------|----------|------|--------|-----|
| 稳定性 | 2 | 2 | 1 | 1 |
| 可用性 | 1 | 3 | 3 | 1 |
| 可扩展性 | 1 | 3 | 2 | 1 |
| 需求符合度 | 2 | 3 | 1 | 1 |

### 按优先级排序的建议路线

| 阶段 | 内容 | 涉及文件数 |
|------|------|-----------|
| **P0**（本周） | 状态机校验 + 编号生成改用序列表 + 前端错误处理 | ~5 |
| **P0**（本周） | 状态文本映射 + 审批按钮条件显示 + 驳回原因输入框 | ~3 |
| **P1**（下次迭代） | 补充 WBS 产值字段 + 项目状态扩展至 6 状态 | ~6 |
| **P1**（下次迭代） | 拆解 saveDraft + 引入状态枚举 + DTO 分离 | ~8 |
| **P2**（架构改进） | 构造函数注入 + Event 机制 + 预算版本字段预留 | ~15 |
| **P2**（功能补全） | 预算审批 + 成本填报 + 预警 + 看板 | 新模块 |
