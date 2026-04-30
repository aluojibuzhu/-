# Commit 文档：报销申请模块与 Review 修复

## 提交信息

- Commit Hash：以 `git log -1 --oneline` 输出为准
- Commit Message：`feat: implement reimbursement module`
- 提交时间：2026-04-30

## 本次变更

1. 修复 review 建议项。
   - 成本科目表格“科目名称”列改为居中对齐。
   - 工时填报去除 `parentId = 1` 人工费硬编码，改为按一级科目名称“人工费”查询真实 ID。
   - 工时表单前端同步改为动态识别人工费父级科目。

2. 完成 2.2 报销申请模块。
   - 新增报销申请、报销附件、报销编号序列表。
   - 新增费用类型字典 `exp_expense_type`。
   - 新增报销申请列表、新建/编辑、详情/审批页面。
   - 支持草稿、提交审批、审批通过、驳回、入账、删除状态流转。
   - 支持项目、WBS节点、成本科目联动与发票附件上传。

3. 菜单与权限。
   - 报销申请挂载到“成本填报”模块。
   - 新增权限前缀 `cost:reimbursement:*`。
   - 新增前端路由 `/cost/reimbursement`。

4. 文档更新。
   - `PRD_MVP.md` 标记 2.2 报销申请完成。
   - 成本填报模块完成度更新为 100%。

## 数据库脚本

- `RuoYi-Vue/sql/2026-04-30_reimbursement_schema.sql`

## 自测结果

- 已在本地 `ry-vue` 数据库执行报销模块 SQL。
- 已验证 `exp_reimbursement`、`exp_attachment`、`exp_no_sequence` 表存在。
- 已验证 `exp_expense_type` 字典存在。
- 已验证“成本填报 / 报销申请”菜单和 `cost:reimbursement:*` 权限存在。
- 后端编译：`mvn -pl ruoyi-admin -am -DskipTests compile` 通过。
- 前端构建：`npm run build:prod` 通过。

## 注意事项

- 需要重启后端服务，使 `/cost/reimbursement/**` 新接口生效。
- 需要刷新或重新登录前端，使新增菜单和权限缓存更新。
