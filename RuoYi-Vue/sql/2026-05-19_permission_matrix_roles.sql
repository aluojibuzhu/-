-- Permission matrix initialization for the project cost management MVP.
-- Safe to run repeatedly against the RuoYi database.

SET NAMES utf8mb4;

SET @add_menu_check_strictly = IF(
  NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_role'
      AND column_name = 'menu_check_strictly'
  ),
  'ALTER TABLE sys_role ADD COLUMN menu_check_strictly tinyint(1) DEFAULT 1 COMMENT ''菜单树选择项是否关联显示'' AFTER data_scope',
  'SELECT 1'
);
PREPARE stmt FROM @add_menu_check_strictly;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_dept_check_strictly = IF(
  NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_role'
      AND column_name = 'dept_check_strictly'
  ),
  'ALTER TABLE sys_role ADD COLUMN dept_check_strictly tinyint(1) DEFAULT 1 COMMENT ''部门树选择项是否关联显示'' AFTER menu_check_strictly',
  'SELECT 1'
);
PREPARE stmt FROM @add_dept_check_strictly;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

-- 1. Role definitions
-- Keep role_id = 1 because RuoYi treats user_id/role_id = 1 as the built-in super administrator.
UPDATE `sys_role`
SET `role_name` = '超级管理员',
    `role_key` = 'super_admin',
    `role_sort` = 1,
    `data_scope` = 1,
    `menu_check_strictly` = 1,
    `dept_check_strictly` = 1,
    `status` = '0',
    `del_flag` = '0',
    `update_by` = 'admin',
    `update_time` = NOW(),
    `remark` = '超级管理员：拥有全部业务与系统配置权限'
WHERE `role_id` = 1;

INSERT INTO `sys_role` (`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT '管理员', 'admin', 2, 1, 1, 1, '0', '0', 'admin', NOW(), '管理员：全部项目业务权限，不含系统配置'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_key` = 'admin');

INSERT INTO `sys_role` (`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT '项目经理', 'project_manager', 3, 2, 1, 1, '0', '0', 'admin', NOW(), '项目经理：本人负责项目范围'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_key` = 'project_manager');

INSERT INTO `sys_role` (`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT '成本会计', 'cost_accountant', 4, 1, 1, 1, '0', '0', 'admin', NOW(), '成本会计：全部项目成本审批、入账、报表权限'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_key` = 'cost_accountant');

INSERT INTO `sys_role` (`role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT '项目成员', 'project_member', 5, 2, 1, 1, '0', '0', 'admin', NOW(), '项目成员：本人参与项目的填报与看板查看权限'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_key` = 'project_member');

UPDATE `sys_role`
SET `role_name` = CASE `role_key`
                    WHEN 'admin' THEN '管理员'
                    WHEN 'project_manager' THEN '项目经理'
                    WHEN 'cost_accountant' THEN '成本会计'
                    WHEN 'project_member' THEN '项目成员'
                    ELSE `role_name`
                  END,
    `status` = '0',
    `del_flag` = '0',
    `update_by` = 'admin',
    `update_time` = NOW()
WHERE `role_key` IN ('admin', 'project_manager', 'cost_accountant', 'project_member');

-- 2. Permission matrix
DROP TEMPORARY TABLE IF EXISTS `_role_perm_matrix`;
CREATE TEMPORARY TABLE `_role_perm_matrix` (
  `role_key` varchar(100) NOT NULL,
  `perms` varchar(100) NOT NULL,
  PRIMARY KEY (`role_key`, `perms`)
) ENGINE=Memory;

-- Admin: project business, project approval, cost approval, monitoring, alert rules, cost dashboard and reports.
INSERT IGNORE INTO `_role_perm_matrix` VALUES
('admin', 'project:projInfo:list'),
('admin', 'project:projInfo:query'),
('admin', 'project:projInfo:approve'),
('admin', 'project:customer:list'),
('admin', 'project:customer:query'),
('admin', 'project:customer:add'),
('admin', 'project:customer:edit'),
('admin', 'project:customer:remove'),
('admin', 'project:costCategory:list'),
('admin', 'project:costCategory:query'),
('admin', 'project:costCategory:add'),
('admin', 'project:costCategory:edit'),
('admin', 'project:costCategory:remove'),
('admin', 'cost:workHour:list'),
('admin', 'cost:workHour:query'),
('admin', 'cost:reimbursement:list'),
('admin', 'cost:reimbursement:query'),
('admin', 'cost:approval:list'),
('admin', 'cost:approval:approve'),
('admin', 'cost:workHour:approve'),
('admin', 'cost:reimbursement:approve'),
('admin', 'alert:dashboard:view'),
('admin', 'alert:record:list'),
('admin', 'alert:record:handle'),
('admin', 'alert:rule:list'),
('admin', 'alert:rule:add'),
('admin', 'alert:rule:edit'),
('admin', 'alert:rule:remove'),
('admin', 'cost:dashboard:view'),
('admin', 'cost:report:preview'),
('admin', 'cost:report:export');

-- Project manager: project creation/editing, project-scoped cost approval, monitoring and reports.
INSERT IGNORE INTO `_role_perm_matrix` VALUES
('project_manager', 'project:projInfo:list'),
('project_manager', 'project:projInfo:query'),
('project_manager', 'project:projInfo:add'),
('project_manager', 'project:projInfo:edit'),
('project_manager', 'project:customer:list'),
('project_manager', 'project:customer:query'),
('project_manager', 'project:customer:add'),
('project_manager', 'project:customer:edit'),
('project_manager', 'project:costCategory:list'),
('project_manager', 'project:costCategory:query'),
('project_manager', 'cost:workHour:list'),
('project_manager', 'cost:workHour:query'),
('project_manager', 'cost:reimbursement:list'),
('project_manager', 'cost:reimbursement:query'),
('project_manager', 'cost:approval:list'),
('project_manager', 'cost:approval:approve'),
('project_manager', 'cost:workHour:approve'),
('project_manager', 'cost:reimbursement:approve'),
('project_manager', 'alert:dashboard:view'),
('project_manager', 'alert:record:list'),
('project_manager', 'alert:record:handle'),
('project_manager', 'cost:dashboard:view'),
('project_manager', 'cost:report:preview'),
('project_manager', 'cost:report:export');

-- Cost accountant: all project cost approval, posting, monitoring, dashboard and reports.
INSERT IGNORE INTO `_role_perm_matrix` VALUES
('cost_accountant', 'project:projInfo:list'),
('cost_accountant', 'project:projInfo:query'),
('cost_accountant', 'cost:workHour:list'),
('cost_accountant', 'cost:workHour:query'),
('cost_accountant', 'cost:reimbursement:list'),
('cost_accountant', 'cost:reimbursement:query'),
('cost_accountant', 'cost:approval:list'),
('cost_accountant', 'cost:approval:approve'),
('cost_accountant', 'cost:approval:post'),
('cost_accountant', 'cost:workHour:approve'),
('cost_accountant', 'cost:workHour:post'),
('cost_accountant', 'cost:reimbursement:approve'),
('cost_accountant', 'cost:reimbursement:post'),
('cost_accountant', 'alert:dashboard:view'),
('cost_accountant', 'alert:record:list'),
('cost_accountant', 'alert:record:handle'),
('cost_accountant', 'cost:dashboard:view'),
('cost_accountant', 'cost:report:preview'),
('cost_accountant', 'cost:report:export');

-- Project member: own project detail, own work-hour/reimbursement submission and dashboard overview.
INSERT IGNORE INTO `_role_perm_matrix` VALUES
('project_member', 'project:projInfo:query'),
('project_member', 'cost:workHour:list'),
('project_member', 'cost:workHour:query'),
('project_member', 'cost:workHour:add'),
('project_member', 'cost:workHour:edit'),
('project_member', 'cost:workHour:remove'),
('project_member', 'cost:reimbursement:list'),
('project_member', 'cost:reimbursement:query'),
('project_member', 'cost:reimbursement:add'),
('project_member', 'cost:reimbursement:edit'),
('project_member', 'cost:reimbursement:remove'),
('project_member', 'cost:dashboard:view');

-- 3. Rebuild role-menu bindings
DROP TEMPORARY TABLE IF EXISTS `_role_menu_selected`;
CREATE TEMPORARY TABLE `_role_menu_selected` (
  `role_id` bigint(20) NOT NULL,
  `menu_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=Memory;

DROP TEMPORARY TABLE IF EXISTS `_role_menu_parent_candidates`;
CREATE TEMPORARY TABLE `_role_menu_parent_candidates` (
  `role_id` bigint(20) NOT NULL,
  `menu_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=Memory;

-- Super administrator gets all menus.
INSERT IGNORE INTO `_role_menu_selected` (`role_id`, `menu_id`)
SELECT 1, `menu_id`
FROM `sys_menu`;

-- Business roles get menus/buttons by permission code.
INSERT IGNORE INTO `_role_menu_selected` (`role_id`, `menu_id`)
SELECT r.`role_id`, m.`menu_id`
FROM `sys_role` r
JOIN `_role_perm_matrix` matrix ON matrix.`role_key` = r.`role_key`
JOIN `sys_menu` m ON m.`perms` = matrix.`perms`
WHERE r.`role_key` IN ('admin', 'project_manager', 'cost_accountant', 'project_member')
  AND r.`del_flag` = '0';

-- Add parent menus so the front-end route tree remains visible.
TRUNCATE TABLE `_role_menu_parent_candidates`;
INSERT IGNORE INTO `_role_menu_parent_candidates` (`role_id`, `menu_id`)
SELECT s.`role_id`, parent.`menu_id`
FROM `_role_menu_selected` s
JOIN `sys_menu` child ON child.`menu_id` = s.`menu_id`
JOIN `sys_menu` parent ON parent.`menu_id` = child.`parent_id`
WHERE child.`parent_id` <> 0;
INSERT IGNORE INTO `_role_menu_selected` (`role_id`, `menu_id`)
SELECT `role_id`, `menu_id`
FROM `_role_menu_parent_candidates`;

TRUNCATE TABLE `_role_menu_parent_candidates`;
INSERT IGNORE INTO `_role_menu_parent_candidates` (`role_id`, `menu_id`)
SELECT s.`role_id`, parent.`menu_id`
FROM `_role_menu_selected` s
JOIN `sys_menu` child ON child.`menu_id` = s.`menu_id`
JOIN `sys_menu` parent ON parent.`menu_id` = child.`parent_id`
WHERE child.`parent_id` <> 0;
INSERT IGNORE INTO `_role_menu_selected` (`role_id`, `menu_id`)
SELECT `role_id`, `menu_id`
FROM `_role_menu_parent_candidates`;

TRUNCATE TABLE `_role_menu_parent_candidates`;
INSERT IGNORE INTO `_role_menu_parent_candidates` (`role_id`, `menu_id`)
SELECT s.`role_id`, parent.`menu_id`
FROM `_role_menu_selected` s
JOIN `sys_menu` child ON child.`menu_id` = s.`menu_id`
JOIN `sys_menu` parent ON parent.`menu_id` = child.`parent_id`
WHERE child.`parent_id` <> 0;
INSERT IGNORE INTO `_role_menu_selected` (`role_id`, `menu_id`)
SELECT `role_id`, `menu_id`
FROM `_role_menu_parent_candidates`;

DELETE rm
FROM `sys_role_menu` rm
JOIN `sys_role` r ON r.`role_id` = rm.`role_id`
WHERE r.`role_key` IN ('super_admin', 'admin', 'project_manager', 'cost_accountant', 'project_member')
   OR rm.`role_id` = 1;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `role_id`, `menu_id`
FROM `_role_menu_selected`;

-- 4. Ensure user 1 remains bound to the built-in super administrator role.
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

DROP TEMPORARY TABLE IF EXISTS `_role_menu_parent_candidates`;
DROP TEMPORARY TABLE IF EXISTS `_role_menu_selected`;
DROP TEMPORARY TABLE IF EXISTS `_role_perm_matrix`;
