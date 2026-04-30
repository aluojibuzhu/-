# Commit 文档：工时填报归入成本填报模块

## 提交信息

- Commit Hash：以 `git log -1 --oneline` 输出为准
- Commit Message：`feat: implement cost timesheet module`
- 提交时间：2026-04-30

## 本次变更

1. 完成第二大模块中的 2.1 工时填报能力。
   - 新增工时单、附件、编号序列等数据库结构。
   - 新增工时草稿、提交、审批、驳回、入账、删除等接口。
   - 新增工时列表、新建/编辑、详情页面。

2. 调整模块归属。
   - 新增一级菜单“成本填报”。
   - 将“工时填报 / 新建工时 / 工时详情”从“项目管理”迁移到“成本填报”。
   - 权限前缀由 `project:workHour:*` 调整为 `cost:workHour:*`。
   - 前端页面访问路径由 `/project/workHour` 调整为 `/cost/workHour`。

3. 完善基础配置与菜单体验。
   - 成本科目增加工时单价 `unit_price`。
   - 基础配置页面支持维护工时单价。
   - 项目管理及其子菜单补充可用 SVG 图标。

4. 更新产品与实施文档。
   - `PRD_MVP.md` 标记 2.1 工时填报已完成。
   - 新增工时填报实现计划文档。

## 数据库脚本

- `RuoYi-Vue/sql/2026-04-30_work_hour_schema.sql`
- `RuoYi-Vue/sql/2026-04-30_project_menu_icons.sql`

## 自测结果

- 后端编译：`mvn -pl ruoyi-admin -am -DskipTests compile` 通过。
- 前端构建：`npm run build:prod` 通过。
- 本地数据库已执行工时模块脚本与菜单图标脚本。
- 已验证“工时填报”位于“成本填报”菜单下，“项目管理”下不再存在工时菜单。

## 注意事项

- 需要重启后端服务，使 `/cost/workHour/**` 新接口生效。
- 需要刷新或重新登录前端，使菜单缓存更新。
- 报销申请尚未实现，后续应继续挂载到“成本填报”模块。
