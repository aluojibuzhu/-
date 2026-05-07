# Code Review: 工程项目成本管理系统

**项目**: 工程项目成本管理系统 (基于 RuoYi-Vue 3.9.2)
**审查维度**: 稳定性 · 可用性 · 可扩展性 · 需求符合度

---

## 审查历史

| 审查轮次 | 基准提交 | 审查提交 | 日期 |
|----------|---------|---------|------|
| 1-7 轮 | — | `3225a5f` → `8058cf9` | 2026-04-29 ~ 2026-05-06 |
| **八轮** | `8058cf9` | `3eba480` (harden-approval) | **2026-05-07** |

**本轮参考**: `测试报告-2026-05-07.md`（共发现 22 个 Bug，覆盖 8 个业务模块）

---

## 总体评价

本轮是测试驱动的修复轮次。测试报告通过白盒代码审查+业务逻辑推演，发现 22 个 Bug（P0×2, P1×3, P2×7, P3×10）。`3eba480` 修复了其中 **6 个**，包括最关键的 **P0 并发竞态条件**。

核心修复是引入乐观锁机制——`updateXxxIfStatus(entity, expectedStatus)` SQL 在 WHERE 中增加 `AND status = #{expectedStatus}` 条件，配合 `ensureUpdated(rows)` 在 updateCount=0 时抛出并发冲突异常。同时补齐了入账前置校验（`ensureCostUpdated`）、billType 分支健壮性、以及 3 处前端按钮防重复提交保护。

**整体判断：提交质量高，不需要紧急修改。** 但仍有 3 个 P1 和 3 个 P2 问题建议在上线前处理。

---

## 一、本轮修复验证

### 1.1 ✅ B-10 [P0] 并发审批竞态条件

**问题**: 两个事务同时对同一单据审批，都通过 `require(PENDING)` 校验（MVCC 快照读），导致状态跳跃或重复入账。

**修复**: 三层防护——
1. SQL 层面：`updateWorkHourIfStatus(workHour, PENDING)` → `WHERE wh_id=#{whId} AND status=#{expectedStatus}`，利用行锁
2. 应用层面：`ensureUpdated(rows)` → `rows==0` 抛出"单据状态已变化，请刷新后重试"
3. DB 层面：`uk_cost_posting_bill(bill_type, bill_id)` 唯一约束兜底

```java
// 修复前
workHourMapper.updateWorkHour(workHour);  // 无状态条件

// 修复后
ensureUpdated(workHourMapper.updateWorkHourIfStatus(workHour, WorkHourStatus.PENDING.code()));
```

### 1.2 ✅ B-12 [P2] 入账时未校验成本分配记录是否存在

**问题**: `increaseActualCost` 更新的 WHERE 条件匹配不到记录时，affected rows=0，数据静默丢失。

**修复**: 新增 `ensureCostUpdated(rows, target)` → 0 行时抛出 `"WBS节点预算记录不存在或已删除，无法入账"`。

### 1.3 ✅ B-13 [P2] billType 分支不够健壮

**问题**: `reject()` 中非 REIMBURSEMENT 类型会走到兜底 `getReimbursementForType`，异常信息模糊。

**修复**: 
- 所有方法改为显式 if-else 链（`WORK_HOUR` / `REIMBURSEMENT` / else）
- 新增 `throwUnsupportedBillType(billType)` → `"不支持的单据类型: " + billType`

### 1.4 ✅ B-15/B-16 [P2] rejectIt 缺少提交前 loading

**修复**: `this.submitting = true` 移到 `$prompt` 调用之前，入口处增加 `if (this.submitting) return` 守卫。

### 1.5 ✅ B-17 [P3] costApproval 缺少 submitting 保护

**修复**: 新增 `submitting` 状态变量，3 个操作按钮全部绑定 `:loading="submitting"` + `:disabled="submitting"`，方法入口双重检查。

---

## 二、修复覆盖统计

| 优先级 | 总数 | 已修复 | 仍开放 |
|--------|------|--------|--------|
| P0 | 2 | **2** (B-10, B-23) | 0 |
| P1 | 3 | 0 | **3** |
| P2 | 7 | **4** (B-12/B-13/B-15/B-16) | **3** |
| P3 | 10 | **1** (B-17) | **9** |
| **合计** | **22** | **7** | **15** |

---

## 三、仍需关注的问题

### P1 — 上线前建议修复

| ID | 问题 | 影响 |
|----|------|------|
| B-01 | `saveDraft` 新建场景冗余第二次 `updateProjInfo` | 性能浪费，每次保存多一次 UPDATE |
| B-08 | `expenseType`/`workType` 无字典值白名单校验 | 任意字符串可入库，数据脏污风险 |
| B-11 | `proj_info` 缺少 `approve_by`/`approve_time` 字段 | 审计追溯缺失，审批人不可查 |

### P2 — 尽快修复

| ID | 问题 |
|----|------|
| B-02 | WBS 节点 `planFinishDate` 未校验是否在项目周期内 |
| B-05 | 工时填报表 `categoryLevel` 未显式校验 |
| B-21 | WBS/分配表物理删除，历史数据不可追溯 |

---

## 四、提交质量评估与判断

### 是否需要修改：**不需要紧急修改**

本轮修复精准命中了测试报告中最高优的 6 个问题，乐观锁机制实现规范（SQL 条件 + 应用层检查 + DB 唯一约束三层防护），前端防护也补齐到位。

3 个 P1 问题（B-01/B-08/B-11）和 3 个 P2 问题（B-02/B-05/B-21）建议在下次迭代中处理。
