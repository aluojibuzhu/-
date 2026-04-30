# 工时填报模块实现计划

| 版本号 | 日期 | 作者 | 说明 |
|--------|------|------|------|
| V1.0 | 2026-04-30 | 产品规划团队 | 基于PRD_MVP.md，制定工时填报模块实现方案 |

> **关联文档**: [PRD_MVP.md](../../../PRD_MVP.md) · [项目立项设计文档](../specs/2026-04-21-project-establishment-design.md) · [前端规范设计文档](../specs/2026-04-29-frontend-specification-design.md)

---

## 1. 模块定位

工时填报是 MVP 闭环的第二阶段。项目成员在已立项且进行中的项目上填报每日工时，经项目经理审批通过后入账，计入项目人工成本。

---

## 2. 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 工作类型 | 若依字典 `wh_work_type` | 业务可配置，无需改代码 |
| 工时单价 | `sys_cost_category` 表新增 `unit_price` 字段 | 单价与科目绑定，企业级统一配置 |
| 附件存储 | 独立 `wh_attachment` 表 | 支持一个工时单多附件，1:N 关系 |
| 审批方式 | 详情页内嵌审批（与项目立项一致） | 统一审批列表在模块3「审批入账」实现 |
| 工时编号 | `WH-YYYYMMDD-NNN`（序列表方案） | 与项目编号 `PRJ-YYYYMMDD-NNN` 格式一致 |
| 状态流转 | 0草稿→1审批中→2已通过/3已驳回→4已入账 | 复用项目立项模块的状态机模式 |

---

## 3. 数据库设计

### 3.1 工时主表 `wh_work_hour`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `wh_id` | bigint PK | 是 | 工时ID，自增 |
| `wh_no` | varchar(30) | 是 | 工时编号，格式 `WH-YYYYMMDD-NNN`，唯一 |
| `proj_id` | bigint FK | 是 | 所属项目ID → `proj_info.proj_id` |
| `proj_name` | varchar(100) | 否 | 项目名称（冗余，便于列表展示） |
| `node_id` | bigint FK | 是 | 关联WBS节点ID → `proj_wbs_node.node_id` |
| `node_name` | varchar(50) | 否 | WBS节点名称（冗余） |
| `category_id` | bigint FK | 是 | 成本科目ID → `sys_cost_category.category_id`（限定人工费类） |
| `category_name` | varchar(50) | 否 | 成本科目名称（冗余） |
| `work_type` | varchar(20) | 是 | 工作类型，字典 `wh_work_type`（design/construct/supervise/manage/meeting/other） |
| `work_date` | date | 是 | 填报日期 |
| `work_hours` | decimal(5,1) | 是 | 工时数，0.5h 倍数 |
| `unit_price` | decimal(14,2) | 否 | 工时单价（取自成本科目配置，提交时快照） |
| `work_cost` | decimal(14,2) | 否 | 工时成本 = 工时数 × 工时单价（自动计算） |
| `work_desc` | varchar(200) | 否 | 工作内容描述 |
| `status` | char(1) | 是 | 状态：0草稿/1审批中/2已通过/3已驳回/4已入账 |
| `submit_time` | datetime | 否 | 提交时间 |
| `approve_by` | varchar(64) | 否 | 审批人 |
| `approve_time` | datetime | 否 | 审批时间 |
| `reject_reason` | varchar(500) | 否 | 驳回原因 |
| 审计字段 | — | — | `del_flag`/`create_by`/`create_time`/`update_by`/`update_time`/`remark` |

### 3.2 附件表 `wh_attachment`

| 字段 | 类型 | 说明 |
|------|------|------|
| `attachment_id` | bigint PK | 附件ID |
| `wh_id` | bigint FK | 关联工时ID → `wh_work_hour.wh_id` |
| `file_name` | varchar(255) | 存储文件名 |
| `original_name` | varchar(255) | 原始文件名 |
| `file_path` | varchar(500) | 文件路径 |
| `file_size` | bigint | 文件大小（字节） |
| `file_type` | varchar(50) | 文件类型/MIME |
| 审计字段 | — | `del_flag`/`create_by`/`create_time` |

### 3.3 序号表 `wh_no_sequence`

单列 `id` bigint AUTO_INCREMENT，用于生成工时编号。

### 3.4 现有表变更

`sys_cost_category` 表新增 `unit_price` decimal(14,2) 字段，仅人工费类（parent_id=1）使用。

### 3.5 字典数据

| 字典类型 | 字典值 | 标签 |
|----------|--------|------|
| `wh_work_type` | design | 设计 |
| | construct | 施工 |
| | supervise | 监理 |
| | manage | 管理 |
| | meeting | 会议 |
| | other | 其他 |

---

## 4. 后端设计

### 4.1 文件清单

```
ruoyi-system/src/main/java/com/ruoyi/system/
├── domain/project/
│   ├── WorkHour.java              # 工时实体（extends BaseEntity）
│   ├── WorkHourAttachment.java    # 附件实体
│   ├── WorkHourSequence.java      # 序号实体（仅 id 字段）
│   └── WorkHourStatus.java        # 状态枚举（DRAFT/PENDING/APPROVED/REJECTED/POSTED）
├── domain/vo/project/
│   └── WorkHourFormVO.java        # 表单VO（WorkHour + List<WorkHourAttachment>）
├── mapper/project/
│   ├── WorkHourMapper.java        # Mapper 接口
│   └── WorkHourAttachmentMapper.java
├── resources/mapper/project/
│   ├── WorkHourMapper.xml         # 动态SQL + 软删除
│   └── WorkHourAttachmentMapper.xml
└── service/project/
    ├── IWorkHourService.java      # Service 接口
    └── impl/WorkHourServiceImpl.java  # 核心业务逻辑

ruoyi-admin/src/main/java/com/ruoyi/web/controller/project/
└── WorkHourController.java        # REST 控制器（extends BaseController）
```

### 4.2 WorkHourStatus 状态枚举

| 常量 | 值 | 标签 | 允许的流转目标 |
|------|-----|------|---------------|
| DRAFT | "0" | 草稿 | PENDING |
| PENDING | "1" | 审批中 | APPROVED, REJECTED |
| APPROVED | "2" | 已通过 | POSTED |
| REJECTED | "3" | 已驳回 | DRAFT（重新编辑后提交） |
| POSTED | "4" | 已入账 | （终态） |

含 `require(WorkHour, WorkHourStatus... allowed)` 静态守卫方法，模式与 `ProjectStatus` 一致。

### 4.3 Service 核心方法

| 方法 | 逻辑 |
|------|------|
| `saveDraft(form, username)` | 校验必填字段 → 查 unit_price → 计算 work_cost → 生成 wh_no（新纪录） → insert/update 主表 → 删除旧附件 → 批量 insert 新附件 |
| `submitForApproval(whId, username)` | 状态守卫 DRAFT/REJECTED → PENDING，记录 submit_time |
| `approve(whId, username)` | 状态守卫 PENDING → APPROVED，记录 approve_by/time |
| `reject(whId, reason, username)` | 状态守卫 PENDING → REJECTED，必填 reject_reason |
| `postCost(whId, username)` | 状态守卫 APPROVED → POSTED（入账触发点，预算扣减逻辑在模块3实现） |
| `deleteWorkHourById(whId)` | 状态守卫仅 DRAFT/REJECTED 可删除（软删除） |

### 4.4 校验规则

| 规则 | 错误提示 |
|------|----------|
| 项目必须存在 | "所属项目不存在" |
| WBS节点必须属于所选项目 | "WBS节点与项目不匹配" |
| 成本科目限定人工费类（parent_id=1） | "请选择人工费类科目" |
| 工时数 > 0 | "工时数必须大于0" |
| 工时数是 0.5 的倍数 | "工时数必须为0.5的整数倍" |
| 工作描述 ≤ 200字 | "工作描述不能超过200字" |
| 填报日期必填 | "填报日期不能为空" |

### 4.5 Controller API

| 方法 | URL | 权限 | 返回 |
|------|-----|------|------|
| GET | `/project/workHour/list` | `project:workHour:list` | TableDataInfo |
| GET | `/project/workHour/{whId}` | `project:workHour:query` | AjaxResult |
| GET | `/project/workHour/wbsNodes/{projId}` | `project:workHour:add` | AjaxResult（WBS节点列表，供级联下拉） |
| POST | `/project/workHour/draft` | `project:workHour:add` | AjaxResult |
| POST | `/project/workHour/submit/{whId}` | `project:workHour:edit` | AjaxResult |
| PUT | `/project/workHour/approve/{whId}` | `project:workHour:approve` | AjaxResult |
| PUT | `/project/workHour/reject/{whId}` | `project:workHour:approve` | AjaxResult |
| PUT | `/project/workHour/post/{whId}` | `project:workHour:post` | AjaxResult |
| DELETE | `/project/workHour/{whId}` | `project:workHour:remove` | AjaxResult |

---

## 5. 前端设计

### 5.1 文件清单

```
ruoyi-ui/src/
├── api/project/
│   └── workHour.js                    # 9个 API 函数
├── utils/
│   └── project.js                     # 新增 WH_STATUS_OPTIONS / whStatusLabel 等
└── views/project/workHour/
    ├── index.vue                       # 工时列表页
    ├── form.vue                        # 工时填报/编辑表单
    └── detail.vue                      # 工时详情/审批页
```

### 5.2 列表页 `index.vue`

遵循前端规范的四层结构：

```
page-heading: "工时填报" + 新建工时按钮
filter-panel:  项目(下拉) / WBS节点(级联) / 工作类型(字典) / 状态 / 日期范围 / 搜索+重置
table-panel:   表格（编号/项目/节点/类型/日期/工时数/成本/状态/操作） + 分页
              操作列：详情 / 编辑(草稿或已驳回) / 提交(草稿或已驳回) / 删除(草稿或已驳回)
```

### 5.3 表单页 `form.vue`

遵循前端规范的表单结构：

```
page-heading: "新建/编辑工时填报" + 返回列表 + 保存草稿
form-body:
  form-group "工时信息":
    所属项目   [el-select: 项目列表，只显示已立项/进行中项目]
    WBS节点    [el-select: 根据项目联动加载]
    成本科目   [el-select: 限定人工费类（parent_id=1）]
    工作类型   [el-select: 字典 wh_work_type]
    填报日期   [el-date-picker]
    工时数     [el-input-number :step="0.5" :min="0.5"]
    工时单价   [只读，选择科目后自动回填]
    工时成本   [只读，工时数×单价实时计算]
  form-group "附件上传":
    el-upload 组件，调 /common/upload
action-bar (底部固定):
  工时成本合计 + 保存草稿 + 提交审批 + 取消
```

**交互要点**：
- 项目下拉 → 选中后触发 WBS 节点级联加载
- 成本科目下拉 → 选中后 unitPrice 自动回填
- 工时数修改 → workCost 实时计算
- `beforeRouteLeave` 守卫：未保存提示

### 5.4 详情页 `detail.vue`

遵循前端规范的详情卡片结构：

```
detail-heading: 工时编号 + 状态标签 + 操作按钮
  - 审批中：通过 / 驳回（仅 project:workHour:approve 权限）
  - 已通过：入账（仅 project:workHour:post 权限）
summary-row:   工时数 / 工时单价 / 工时成本（3个统计卡片）
detail-card:   基本信息（项目/WBS/科目/类型/日期）
detail-card:   工作描述 / 驳回原因（驳回时显示）
detail-card:   附件列表（下载链接）
审计轨迹:      创建人/创建时间/提交时间/审批人/审批时间
```

---

## 6. 菜单和权限

### 6.1 菜单项

| 菜单名 | 类型 | 路径 | 权限标识 | 可见性 |
|--------|------|------|----------|--------|
| 工时填报 | C | `workHour` | `project:workHour:list` | 侧边栏可见 |
| 新建工时 | C | `workHour/form` | `project:workHour:add` | 隐藏 |
| 工时详情 | C | `workHour/detail/:id(\d+)` | `project:workHour:query` | 隐藏 |

### 6.2 权限按钮

| 权限名 | 权限标识 |
|--------|----------|
| 工时查询 | `project:workHour:query` |
| 工时新增 | `project:workHour:add` |
| 工时修改 | `project:workHour:edit` |
| 工时删除 | `project:workHour:remove` |
| 工时审批 | `project:workHour:approve` |
| 工时入账 | `project:workHour:post` |

父菜单挂载在"项目管理"目录下（parent_id = 项目管理菜单ID），order_num 接在项目详情之后（order_num = 4）。

---

## 7. 数据流

```
┌──────────┐    选择项目     ┌──────────────┐
│ proj_info │───────────────▶│ wh_work_hour │
│  proj_id  │                │   proj_id    │
│ proj_name │                │   node_id ──────▶ proj_wbs_node
└──────────┘                │  category_id ──▶ sys_cost_category
                                   │            (unit_price)
                                   │
                            workCost = workHours × unitPrice
                                   │
                                   ▼
                            wh_attachment (1:N)
```

**保存流程**：
1. 用户选择项目 → WBS节点级联加载
2. 用户选择科目 → 单价自动回填
3. 用户填写工时数 → 成本实时计算
4. 附件通过 `/common/upload` 上传，返回路径
5. 保存草稿 → Service 校验 → 生成编号 → insert 主表 + 附件表

**审批流程**：
1. 提交审批：DRAFT/REJECTED → PENDING
2. 审批通过：PENDING → APPROVED
3. 驳回：PENDING → REJECTED（必填原因）
4. 入账：APPROVED → POSTED（模块3扩展预算扣减）

---

## 8. 与后续模块的关系

| 后续模块 | 依赖关系 |
|----------|----------|
| 2.2 报销申请 | 共用审批流模式、附件上传模式 |
| 3. 审批入账 | `postCost` 方法中扩展预算余额扣减、节点已执行成本更新、触发预警判断 |
| 4. 监控预警 | 入账时触发的预警数据来源 |
| 5. 成本看板 | 人工成本数据来源 |

---

## 9. 实施步骤

| Phase | 内容 | 产出物 |
|-------|------|--------|
| 1 | 数据库建表 + 字典 + 菜单 | SQL 脚本 |
| 2 | Entity + VO + Mapper + XML | 后端数据访问层 |
| 3 | Service + ServiceImpl | 后端业务逻辑层 |
| 4 | Controller | 后端 REST API |
| 5 | 前端 API 模块 + 工具函数 | 前端数据层 |
| 6 | 前端页面（列表/表单/详情） | 前端视图层 |
| 7 | 联调测试 | 验收 |

---

*本计划为工时填报模块的完整技术方案，具体编码时请遵循 [前端规范设计文档](../specs/2026-04-29-frontend-specification-design.md) 和若依框架分层约定。*
