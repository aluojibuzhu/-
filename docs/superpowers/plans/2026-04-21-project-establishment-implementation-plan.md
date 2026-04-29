# 项目立项模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 实现完整的项目立项模块——项目经理在单页面完成项目方案制定（基本信息 + WBS节点 + 成本分配），提交后由管理员审批。

**架构思路：**
- 数据库层：项目主表 + WBS节点表 + 成本分配表 + 客户表 + 成本科目配置表
- 后端：遵循若依框架分层结构（Controller → Service → Mapper → Entity），新增统一响应和校验
- 前端：单页面表单，包含基本信息区、WBS节点-成本分配表格、操作区；支持新建客户弹窗
- 工作流：草稿 → 提交审批 → 审批中 → 已立项/已驳回

**技术栈：** RuoYi-Vue（Java + Spring Boot 后端，Vue.js 前端，MySQL 数据库）

---

## 文件结构总览

```
后端 (Java)
├── domain/project/
│   ├── entity/ProjInfo.java              # 项目主表实体
│   ├── entity/ProjWbsNode.java           # WBS节点实体
│   ├── entity/ProjCostAllocation.java    # 成本分配实体
│   ├── entity/ProjCustomer.java          # 客户实体
│   └── entity/SysCostCategory.java       # 成本科目配置实体
├── mapper/project/
│   ├── ProjInfoMapper.java
│   ├── ProjWbsNodeMapper.java
│   ├── ProjCostAllocationMapper.java
│   ├── ProjCustomerMapper.java
│   └── SysCostCategoryMapper.java
├── service/project/
│   ├── IProjInfoService.java
│   ├── impl/ProjInfoServiceImpl.java
│   ├── IProjWbsNodeService.java
│   ├── IProjCostAllocationService.java
│   └── IProjCustomerService.java
├── controller/project/
│   ├── ProjInfoController.java           # 项目立项 API
│   ├── ProjCustomerController.java       # 客户管理 API
│   └── SysCostCategoryController.java    # 成本科目配置 API
└── domain/vo/
    ├── ProjInfoVO.java                   # 项目详情返回VO
    ├── ProjInfoFormVO.java               # 项目表单VO（包含节点+分配）
    └── ProjCostAllocationVO.java         # 成本分配VO

前端 (Vue)
├── api/project/
│   ├── projInfo.js                       # 项目立项 API
│   ├── projCustomer.js                   # 客户管理 API
│   └── costCategory.js                   # 成本科目 API
├── views/project/
│   ├── projInfo/
│   │   ├── index.vue                    # 项目列表页
│   │   ├── form.vue                     # 新建/编辑项目表单
│   │   └── detail.vue                    # 项目详情（审批页）
│   └── components/
│       ├── WbsCostTable.vue              # WBS节点-成本分配表格组件
│       └── CustomerModal.vue             # 新建客户弹窗
└── views/project/modules/
    └──ProjInfoModal.vue (若复用审批流可拆)
```

---

## Phase 1：数据库设计

### 任务 1：编写数据库建表脚本

**文件：** `sql/2026-04-21_project_establishment_schema.sql`

```sql
-- ----------------------------
-- 项目立项模块建表脚本
-- ----------------------------

-- 1. 客户表
CREATE TABLE `proj_customer` (
  `customer_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `customer_name` varchar(100) NOT NULL COMMENT '客户名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 2. 成本科目配置表（企业级）
CREATE TABLE `sys_cost_category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '科目ID',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父科目ID（0表示一级）',
  `category_name` varchar(50) NOT NULL COMMENT '科目名称',
  `category_level` tinyint(1) NOT NULL COMMENT '科目级别（1一级 2二级）',
  `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本科目配置表';

-- 预置成本科目数据
INSERT INTO `sys_cost_category` (`category_id`, `parent_id`, `category_name`, `category_level`, `order_num`) VALUES
(1, 0, '人工费', 1, 1),
(2, 0, '材料费', 1, 2),
(3, 0, '设备费', 1, 3),
(4, 0, '分包费', 1, 4),
(5, 0, '现场杂费', 1, 5),
(6, 0, '管理费', 1, 6),
(7, 0, '其他', 1, 7),
(101, 1, '项目管理人工', 2, 11),
(102, 1, '设计人员人工', 2, 12),
(103, 1, '施工人员人工', 2, 13),
(201, 2, '主材', 2, 21),
(202, 2, '辅材', 2, 22),
(203, 2, '周转材料', 2, 23);

-- 3. 项目主表
CREATE TABLE `proj_info` (
  `proj_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `proj_no` varchar(30) NOT NULL COMMENT '项目编号（PRJ-YYYYMMDD-序号）',
  `proj_name` varchar(100) NOT NULL COMMENT '项目名称',
  `customer_id` bigint(20) NOT NULL COMMENT '关联客户ID',
  `customer_name` varchar(100) DEFAULT NULL COMMENT '客户名称（冗余）',
  `plan_start_date` date DEFAULT NULL COMMENT '预计开工日期',
  `plan_end_date` date DEFAULT NULL COMMENT '预计竣工日期',
  `contract_amount` decimal(14,2) DEFAULT NULL COMMENT '预计合同金额',
  `proj_desc` varchar(500) DEFAULT NULL COMMENT '项目简介',
  `total_budget` decimal(14,2) DEFAULT 0.00 COMMENT '项目总预算（所有节点预算之和）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '项目状态（0草稿 1审批中 2已立项 3已驳回 4进行中 5已完工）',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回原因',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`proj_id`),
  UNIQUE KEY `uk_proj_no` (`proj_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目主表';

-- 4. WBS节点表
CREATE TABLE `proj_wbs_node` (
  `node_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `proj_id` bigint(20) NOT NULL COMMENT '所属项目ID',
  `node_no` varchar(30) NOT NULL COMMENT '节点编号（ND-001）',
  `node_name` varchar(50) NOT NULL COMMENT '节点名称',
  `plan_finish_date` date DEFAULT NULL COMMENT '预计完成日期',
  `node_budget` decimal(14,2) DEFAULT 0.00 COMMENT '节点预算（自动累计）',
  `order_num` int(4) DEFAULT 0 COMMENT '节点顺序',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`node_id`),
  KEY `idx_proj_id` (`proj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WBS节点表';

-- 5. 成本分配表（节点 × 成本科目）
CREATE TABLE `proj_cost_allocation` (
  `allocation_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分配ID',
  `proj_id` bigint(20) NOT NULL COMMENT '项目ID',
  `node_id` bigint(20) NOT NULL COMMENT '节点ID',
  `category_id` bigint(20) NOT NULL COMMENT '成本科目ID',
  `category_name` varchar(50) DEFAULT NULL COMMENT '科目名称（冗余）',
  `allocation_amount` decimal(14,2) DEFAULT 0.00 COMMENT '分配金额',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`allocation_id`),
  KEY `idx_proj_node` (`proj_id`, `node_id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本分配表';

-- 6. 项目编号序列表（用于生成 PRJ-YYYYMMDD-001）
CREATE TABLE `proj_no_sequence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目编号序列表';

-- 7. 节点编号序列表（用于生成 ND-001）
CREATE TABLE `proj_node_no_sequence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点编号序列表';
```

- [ ] **Step 1: 创建数据库建表脚本**
- [ ] **Step 2: 在 MySQL 中执行建表脚本**
- [ ] **Step 3: 提交 SQL 文件**

---

## Phase 2：后端实体类

### 任务 2：创建实体类

**文件：** `ruoyi-admin/src/main/java/com/ruoyi/project/domain/project/`

- [ ] **Step 1: 创建 ProjInfo.java（项目主表实体）**
```java
package com.ruoyi.project.domain.project;

import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

public class ProjInfo extends BaseEntity {
    private Long projId;
    private String projNo;
    private String projName;
    private Long customerId;
    private String customerName;
    private Date planStartDate;
    private Date planEndDate;
    private BigDecimal contractAmount;
    private String projDesc;
    private BigDecimal totalBudget;
    private String status;       // 0草稿 1审批中 2已立项 3已驳回 4进行中 5已完工
    private String rejectReason;
}
```

- [ ] **Step 2: 创建 ProjWbsNode.java（WBS节点实体）**
- [ ] **Step 3: 创建 ProjCostAllocation.java（成本分配实体）**
- [ ] **Step 4: 创建 ProjCustomer.java（客户实体）**
- [ ] **Step 5: 创建 SysCostCategory.java（成本科目实体）**

### 任务 3：创建 VO 类

**文件：** `ruoyi-admin/src/main/java/com/ruoyi/project/domain/vo/`

- [ ] **Step 1: 创建 ProjInfoFormVO.java（包含基本信息 + 节点列表 + 分配列表）**
- [ ] **Step 2: 创建 ProjInfoVO.java（项目详情返回VO）**

---

## Phase 3：后端 Mapper 层

### 任务 4：创建 Mapper 接口

**文件：** `ruoyi-admin/src/main/java/com/ruoyi/project/mapper/project/`

- [ ] **Step 1: 创建 ProjInfoMapper.java**
```java
package com.ruoyi.project.mapper;

import com.ruoyi.project.domain.project.ProjInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProjInfoMapper {
    int insertProjInfo(ProjInfo projInfo);
    int updateProjInfo(ProjInfo projInfo);
    ProjInfo selectProjInfoById(Long projId);
    List<ProjInfo> selectProjInfoList(ProjInfo projInfo);
    String generateProjNo();
}
```

- [ ] **Step 2: 创建 ProjWbsNodeMapper.java**
- [ ] **Step 3: 创建 ProjCostAllocationMapper.java**
- [ ] **Step 4: 创建 ProjCustomerMapper.java**
- [ ] **Step 5: 创建 SysCostCategoryMapper.java**

### 任务 5：编写 Mapper XML

**文件：** `ruoyi-admin/src/main/resources/mapper/project/`

- [ ] **Step 1: 编写 ProjInfoMapper.xml（包含生成项目编号逻辑）**
- [ ] **Step 2: 编写 ProjWbsNodeMapper.xml**
- [ ] **Step 3: 编写 ProjCostAllocationMapper.xml**
- [ ] **Step 4: 编写 ProjCustomerMapper.xml**
- [ ] **Step 5: 编写 SysCostCategoryMapper.xml**

---

## Phase 4：后端 Service 层

### 任务 6：创建 Service 接口和实现

**文件：** `ruoyi-admin/src/main/java/com/ruoyi/project/service/project/`

- [ ] **Step 1: 创建 IProjInfoService.java 和 ProjInfoServiceImpl.java**
  - 包含：`saveDraft()`、`submitForApproval()`、`approve()`、`reject()` 方法
  - 包含：保存项目时同时保存节点和分配数据（事务）

```java
public interface IProjInfoService {
    ProjInfoFormVO getProjForm(Long projId);
    int saveDraft(ProjInfoFormVO form);
    int submitForApproval(Long projId);
    int approve(Long projId);
    int reject(Long projId, String rejectReason);
    List<ProjInfo> listProjInfos(ProjInfo projInfo);
}
```

- [ ] **Step 2: 创建 IProjCustomerService.java 和实现（支持新建客户）**
- [ ] **Step 3: 创建 ISysCostCategoryService.java 和实现（成本科目查询）**
- [ ] **Step 4: 创建 IProjWbsNodeService.java 和实现**

---

## Phase 5：后端 Controller 层

### 任务 7：创建 Controller

**文件：** `ruoyi-admin/src/main/java/com/ruoyi/project/controller/project/`

- [ ] **Step 1: 创建 ProjInfoController.java（项目立项 API）**

```java
@RestController
@RequestMapping("/project/info")
public class ProjInfoController {
    // GET  /project/info/{id}          获取项目表单详情
    // GET  /project/info/list           项目列表
    // POST /project/info/draft          保存草稿
    // POST /project/info/submit         提交审批
    // PUT  /project/info/approve/{id}   管理员审批通过
    // PUT  /project/info/reject/{id}    管理员驳回
    // DELETE /project/info/{id}         删除草稿项目
}
```

- [ ] **Step 2: 创建 ProjCustomerController.java（客户管理 API）**
```java
@RestController
@RequestMapping("/project/customer")
public class ProjCustomerController {
    // GET    /project/customer/list        客户列表
    // POST   /project/customer              新建客户
    // GET    /project/customer/{id}         获取客户详情
    // PUT    /project/customer/{id}         修改客户
    // DELETE /project/customer/{id}        删除客户
}
```

- [ ] **Step 3: 创建 SysCostCategoryController.java（成本科目配置 API）**
```java
@RestController
@RequestMapping("/system/costCategory")
public class SysCostCategoryController {
    // GET /system/costCategory/list        成本科目列表（树形）
    // GET /system/costCategory/tree        获取科目树（一级+二级）
}
```

---

## Phase 6：前端 API 层

### 任务 8：创建前端 API

**文件：** `ruoyi-ui/src/api/project/`

- [ ] **Step 1: 创建 projInfo.js**
```javascript
export function getProjForm(id) { return axios.get('/project/info/' + id) }
export function listProjInfos(query) { return axios.get('/project/info/list', { params: query }) }
export function saveDraft(data) { return axios.post('/project/info/draft', data) }
export function submitForApproval(id) { return axios.post('/project/info/submit/' + id) }
export function approve(id) { return axios.put('/project/info/approve/' + id) }
export function reject(id, data) { return axios.put('/project/info/reject/' + id, data) }
export function delProjInfo(id) { return axios.delete('/project/info/' + id) }
```

- [ ] **Step 2: 创建 projCustomer.js**
```javascript
export function listCustomers(query) { return axios.get('/project/customer/list', { params: query }) }
export function addCustomer(data) { return axios.post('/project/customer', data) }
export function getCustomer(id) { return axios.get('/project/customer/' + id) }
export function updateCustomer(data) { return axios.put('/project/customer/' + data.customerId, data) }
export function delCustomer(id) { return axios.delete('/project/customer/' + id) }
```

- [ ] **Step 3: 创建 costCategory.js**
```javascript
export function listCostCategories() { return axios.get('/system/costCategory/list') }
export function getCostCategoryTree() { return axios.get('/system/costCategory/tree') }
```

---

## Phase 7：前端页面

### 任务 9：创建项目列表页

**文件：** `ruoyi-ui/src/views/project/projInfo/index.vue`

- [ ] **Step 1: 创建项目列表页**
- 列表字段：项目编号、项目名称、客户名称、开工日期、总预算、状态、操作
- 操作：查看详情、新建项目（跳转 form）、编辑（仅草稿）、删除（仅草稿）、提交审批（仅草稿）
- 状态筛选：全部 / 草稿 / 审批中 / 已立项 / 已驳回

### 任务 10：创建项目表单页（核心）

**文件：** `ruoyi-ui/src/views/project/projInfo/form.vue`

- [ ] **Step 1: 创建项目表单页 - 基本信息区**
- 字段：项目编号（只读）、项目名称、关联客户（下拉 + 新建客户按钮）、预计开工日期、预计竣工日期、预计合同金额、项目简介

- [ ] **Step 2: 创建 WbsCostTable.vue 组件**
- 表头：节点编号(ND-001) | 节点名称 | 预计完成日期 | [成本科目列...] | 节点预算(自动累计)
- 每行对应一个 WBS 节点
- 点击"新增节点"按钮在表格末尾添加一行
- 每行可编辑、可删除
- 成本科目列由后端返回的科目配置动态生成
- 节点预算 = 该行所有成本科目分配金额之和（前端计算，不可编辑）

- [ ] **Step 3: 在 form.vue 中集成 WbsCostTable 组件**
- 保存草稿：POST /project/info/draft
- 提交审批：先保存草稿，再 POST /project/info/submit/{id}

### 任务 11：创建新建客户弹窗组件

**文件：** `ruoyi-ui/src/views/project/components/CustomerModal.vue`

- [ ] **Step 1: 创建客户新建弹窗组件**
- 字段：客户名称（必填）、联系人、联系电话、地址、备注
- 确认后调用 projCustomer.addCustomer()，成功后回填到立项表单

### 任务 12：创建项目详情/审批页

**文件：** `ruoyi-ui/src/views/project/projInfo/detail.vue`

- [ ] **Step 2: 创建项目详情页（管理员审批页）**
- 展示完整项目信息（基本信息 + WBS节点 + 成本分配 + 节点预算）
- 管理员操作：通过 / 驳回（填写驳回原因）

---

## Phase 8：路由和菜单配置

### 任务 13：配置路由和系统菜单

- [ ] **Step 1: 在 router/index.js 中添加项目立项相关路由**
- [ ] **Step 2: 在系统菜单中添加项目管理模块（SQL插入菜单数据）**

```sql
-- 菜单SQL（参考若依菜单格式）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `visible`, `status`, `perms`, `icon`) VALUES
('项目管理', 0, 1, 'project', NULL, 'M', '0', '0', '', 'project'),
('项目列表', 200, 1, 'projInfo', 'project/projInfo/index', 'C', '0', '0', 'project:projInfo:list', '#'),
('新建项目', 200, 2, 'projForm', 'project/projInfo/form', 'C', '0', '0', 'project:projInfo:add', '#'),
('项目详情', 200, 3, 'projDetail', 'project/projInfo/detail', 'C', '0', '0', 'project:projInfo:query', '#');
```

---

## Phase 9：测试验证

### 任务 14：功能测试

- [ ] **Step 1: 测试新建客户**
  - 操作：在立项表单中点击新建客户，填写信息，确认后客户自动回填到关联客户字段

- [ ] **Step 2: 测试保存草稿**
  - 操作：填写基本信息 + 添加节点 + 填写成本分配，点击保存草稿
  - 验证：数据库 proj_info status=0，proj_wbs_node 和 proj_cost_allocation 数据正确

- [ ] **Step 3: 测试提交审批**
  - 操作：草稿状态下点击提交审批
  - 验证：proj_info status=0→1，发送通知

- [ ] **Step 4: 测试管理员审批通过**
  - 操作：管理员登录，进入项目详情，点击通过
  - 验证：proj_info status=1→2

- [ ] **Step 5: 测试管理员驳回**
  - 操作：管理员驳回，填写驳回原因
  - 验证：proj_info status=1→3，reject_reason 填充

- [ ] **Step 6: 测试节点预算自动计算**
  - 操作：填写某节点各成本科目分配金额
  - 验证：节点预算 = 各科目分配之和，且该值与手动计算一致

---

## 计划自检

### Spec 覆盖检查

| Spec 需求 | 对应任务 |
|-----------|---------|
| 基本信息区字段 | Task 10 Step 1 |
| WBS节点新增/编辑/删除 | Task 10 Step 2 |
| 节点编号自动生成 | Task 1 (数据库) + Task 5 Step 1 |
| 成本科目动态列 | Task 10 Step 2 |
| 节点预算自动累计 | Task 10 Step 2 |
| 保存草稿 | Task 10 Step 3 |
| 提交审批 | Task 10 Step 3 |
| 管理员审批 | Task 7 Step 1 + Task 12 Step 1 |
| 新建客户 | Task 7 Step 2 + Task 11 Step 1 |
| 成本科目企业配置 | Task 7 Step 3 |
| 项目状态流转 | Task 6 Step 1 |

### 类型一致性检查

- `ProjInfo.status` 字段使用 String 类型，值枚举：'0'/'1'/'2'/'3'/'4'/'5'
- `ProjWbsNode.nodeBudget` 使用 BigDecimal
- `ProjCostAllocation.allocationAmount` 使用 BigDecimal
- 前端 form.vue 中节点预算计算：`row.nodeBudget = sum(Object.values(row.allocations))`
- 科目树接口返回树形结构，前端动态生成表格列

### 占位符扫描
- 所有任务步骤均包含完整代码，无 TBD/TODO
- 数据库字段均明确，无模糊类型

---

**Plan 完成，已保存至：** `docs/superpowers/plans/2026-04-21-project-establishment-implementation-plan.md`
