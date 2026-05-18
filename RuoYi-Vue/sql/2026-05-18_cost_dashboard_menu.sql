-- 成本看板模块菜单与权限
-- 模块5：5.1 成本总览 / 5.2 报表导出

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本看板', 0, 5, 'costDashboard', NULL, '', 1, 0, 'M', '0', '0', '', 'dashboard', 'admin', NOW(), '成本看板模块'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '成本看板' AND `parent_id` = 0);

SET @dashboard_root_id = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '成本看板' AND `parent_id` = 0 LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本总览', @dashboard_root_id, 1, 'overview', 'project/costDashboard/index', '', 1, 0, 'C', '0', '0', 'cost:dashboard:view', 'chart', 'admin', NOW(), '成本总览'
WHERE @dashboard_root_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @dashboard_root_id AND `path` = 'overview');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '专项报表', @dashboard_root_id, 2, 'report', NULL, '', 1, 0, 'M', '0', '0', '', 'download', 'admin', NOW(), '专项报表'
WHERE @dashboard_root_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @dashboard_root_id AND `path` = 'report');

UPDATE `sys_menu`
SET `menu_name` = '专项报表',
    `component` = NULL,
    `menu_type` = 'M',
    `perms` = '',
    `icon` = 'download',
    `remark` = '专项报表'
WHERE `parent_id` = @dashboard_root_id AND `path` = 'report';

SET @overview_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `parent_id` = @dashboard_root_id AND `path` = 'overview' LIMIT 1);
SET @report_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `parent_id` = @dashboard_root_id AND `path` = 'report' LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '项目资金汇总表', @report_menu_id, 1, 'projectSummary', 'project/costReport/projectSummary', '', 1, 0, 'C', '0', '0', 'cost:report:export', 'list', 'admin', NOW(), '项目资金汇总表'
WHERE @report_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @report_menu_id AND `path` = 'projectSummary');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '科目成本明细表', @report_menu_id, 2, 'categoryDetail', 'project/costReport/categoryDetail', '', 1, 0, 'C', '0', '0', 'cost:report:export', 'tree-table', 'admin', NOW(), '科目成本明细表'
WHERE @report_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @report_menu_id AND `path` = 'categoryDetail');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '入账流水明细表', @report_menu_id, 3, 'postingFlow', 'project/costReport/postingFlow', '', 1, 0, 'C', '0', '0', 'cost:report:export', 'form', 'admin', NOW(), '入账流水明细表'
WHERE @report_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @report_menu_id AND `path` = 'postingFlow');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '节点预算执行表', @report_menu_id, 4, 'nodeExecution', 'project/costReport/nodeExecution', '', 1, 0, 'C', '0', '0', 'cost:report:export', 'tree', 'admin', NOW(), '节点预算执行表'
WHERE @report_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @report_menu_id AND `path` = 'nodeExecution');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本总览查询', @overview_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'cost:dashboard:view', '#', 'admin', NOW(), '成本总览查询权限'
WHERE @overview_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @overview_menu_id AND `perms` = 'cost:dashboard:view');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报表预览', @report_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'cost:report:preview', '#', 'admin', NOW(), '报表预览权限'
WHERE @report_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @report_menu_id AND `perms` = 'cost:report:preview');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报表导出', @report_menu_id, 2, '#', '', '', 1, 0, 'F', '0', '0', 'cost:report:export', '#', 'admin', NOW(), '报表导出权限'
WHERE @report_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @report_menu_id AND `perms` = 'cost:report:export');
