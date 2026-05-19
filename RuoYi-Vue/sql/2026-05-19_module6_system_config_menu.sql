-- Module 6: system configuration menu.
-- Provides MVP role permission configuration by reusing the existing RuoYi role page.

SET NAMES utf8mb4;

-- Keep customer management inside project management. Some old seed data placed it under system.
UPDATE `sys_menu`
SET `parent_id` = 100,
    `order_num` = 5,
    `update_by` = 'admin',
    `update_time` = NOW()
WHERE `menu_name` = '客户管理'
  AND `perms` = 'project:customer:list';

-- Reuse the existing system root menu as module 6.
UPDATE `sys_menu`
SET `menu_name` = '系统配置',
    `parent_id` = 0,
    `order_num` = 6,
    `path` = 'system',
    `component` = NULL,
    `menu_type` = 'M',
    `visible` = '0',
    `status` = '0',
    `perms` = '',
    `icon` = 'system',
    `update_by` = 'admin',
    `update_time` = NOW(),
    `remark` = '模块6系统配置目录'
WHERE `menu_id` = 200;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 2050, '角色权限配置', 200, 1, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', NOW(), '模块6角色权限配置'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2050);

UPDATE `sys_menu`
SET `menu_name` = '角色权限配置',
    `parent_id` = 200,
    `order_num` = 1,
    `path` = 'role',
    `component` = 'system/role/index',
    `menu_type` = 'C',
    `visible` = '0',
    `status` = '0',
    `perms` = 'system:role:list',
    `icon` = 'peoples',
    `update_by` = 'admin',
    `update_time` = NOW(),
    `remark` = '模块6角色权限配置'
WHERE `menu_id` = 2050;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 2051, '角色查询', 2050, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', NOW(), '角色查询权限'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2051);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 2052, '角色新增', 2050, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', NOW(), '角色新增权限'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2052);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 2053, '角色修改', 2050, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', NOW(), '角色修改与权限分配'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2053);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 2054, '角色删除', 2050, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', NOW(), '角色删除权限'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2054);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 2055, '角色导出', 2050, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', NOW(), '角色导出权限'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2055);

-- Grant module 6 only to super administrator.
DELETE rm
FROM `sys_role_menu` rm
JOIN `sys_role` r ON r.`role_id` = rm.`role_id`
WHERE r.`role_key` <> 'super_admin'
  AND rm.`menu_id` IN (200, 2050, 2051, 2052, 2053, 2054, 2055);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`role_id`, m.`menu_id`
FROM `sys_role` r
JOIN `sys_menu` m ON m.`menu_id` IN (200, 2050, 2051, 2052, 2053, 2054, 2055)
WHERE r.`role_key` = 'super_admin'
  AND r.`del_flag` = '0';
