-- 审批入账模块增量脚本
-- 模块3拆分为两个独立功能页面：3.1立项审批、3.2成本审批。

SET @col_exists = (
  SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proj_info' AND COLUMN_NAME = 'actual_cost'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `proj_info` ADD COLUMN `actual_cost` decimal(14,2) DEFAULT 0.00 COMMENT ''已执行成本'' AFTER `total_budget`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proj_info' AND COLUMN_NAME = 'budget_balance'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `proj_info` ADD COLUMN `budget_balance` decimal(14,2) DEFAULT 0.00 COMMENT ''预算余额'' AFTER `actual_cost`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proj_wbs_node' AND COLUMN_NAME = 'actual_cost'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `proj_wbs_node` ADD COLUMN `actual_cost` decimal(14,2) DEFAULT 0.00 COMMENT ''已执行成本'' AFTER `node_budget`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proj_cost_allocation' AND COLUMN_NAME = 'actual_cost'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `proj_cost_allocation` ADD COLUMN `actual_cost` decimal(14,2) DEFAULT 0.00 COMMENT ''已执行成本'' AFTER `allocation_amount`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `proj_info`
SET `actual_cost` = IFNULL(`actual_cost`, 0),
    `budget_balance` = IFNULL(`total_budget`, 0) - IFNULL(`actual_cost`, 0)
WHERE `del_flag` = '0';

UPDATE `proj_wbs_node` SET `actual_cost` = IFNULL(`actual_cost`, 0) WHERE `del_flag` = '0';
UPDATE `proj_cost_allocation` SET `actual_cost` = IFNULL(`actual_cost`, 0) WHERE `del_flag` = '0';

CREATE TABLE IF NOT EXISTS `cost_posting_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '入账记录ID',
  `bill_type` varchar(30) NOT NULL COMMENT '单据类型 WORK_HOUR/REIMBURSEMENT',
  `bill_id` bigint NOT NULL COMMENT '单据ID',
  `bill_no` varchar(30) NOT NULL COMMENT '单据编号',
  `proj_id` bigint NOT NULL COMMENT '项目ID',
  `proj_name` varchar(100) NOT NULL COMMENT '项目名称快照',
  `node_id` bigint NOT NULL COMMENT 'WBS节点ID',
  `node_name` varchar(100) NOT NULL COMMENT 'WBS节点名称快照',
  `category_id` bigint NOT NULL COMMENT '成本科目ID',
  `category_name` varchar(100) NOT NULL COMMENT '成本科目名称快照',
  `amount` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '入账金额',
  `post_by` varchar(64) DEFAULT '' COMMENT '入账人',
  `post_time` datetime DEFAULT NULL COMMENT '入账时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_cost_posting_bill` (`bill_type`, `bill_id`),
  KEY `idx_cost_posting_proj` (`proj_id`),
  KEY `idx_cost_posting_node_category` (`node_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本入账记录表';

UPDATE `sys_menu`
SET `menu_name` = '审批入账',
    `order_num` = 3,
    `path` = 'approvalCenter',
    `component` = NULL,
    `perms` = '',
    `icon` = 'validCode',
    `remark` = '统一审批入账模块'
WHERE `parent_id` = 0 AND `menu_name` IN ('成本审批', '审批入账');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '审批入账', 0, 3, 'approvalCenter', NULL, '', 1, 0, 'M', '0', '0', '', 'validCode', 'admin', NOW(), '统一审批入账模块'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '审批入账' AND `parent_id` = 0);

SET @approval_root_id = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '审批入账' AND `parent_id` = 0 LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '立项审批', @approval_root_id, 1, 'project', 'project/projectApproval/index', '', 1, 0, 'C', '0', '0', 'project:projInfo:approve', 'validCode', 'admin', NOW(), '项目立项审批'
WHERE @approval_root_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @approval_root_id AND `path` = 'project' AND `component` = 'project/projectApproval/index');

UPDATE `sys_menu`
SET `parent_id` = @approval_root_id,
    `order_num` = 1,
    `menu_name` = '立项审批',
    `path` = 'project',
    `component` = 'project/projectApproval/index',
    `menu_type` = 'C',
    `visible` = '0',
    `status` = '0',
    `perms` = 'project:projInfo:approve',
    `icon` = 'validCode',
    `remark` = '项目立项审批'
WHERE @approval_root_id IS NOT NULL
  AND `parent_id` = @approval_root_id
  AND `component` = 'project/projectApproval/index';

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本审批', @approval_root_id, 2, 'cost', 'project/costApproval/index', '', 1, 0, 'C', '0', '0', 'cost:approval:list', 'validCode', 'admin', NOW(), '成本审批入账'
WHERE @approval_root_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'cost:approval:list' AND `menu_type` = 'C');

SET @approval_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `perms` = 'cost:approval:list' AND `menu_type` = 'C' ORDER BY `menu_id` LIMIT 1);

UPDATE `sys_menu`
SET `parent_id` = @approval_root_id,
    `order_num` = 2,
    `menu_name` = '成本审批',
    `path` = 'cost',
    `component` = 'project/costApproval/index',
    `menu_type` = 'C',
    `visible` = '0',
    `status` = '0',
    `perms` = 'cost:approval:list',
    `icon` = 'validCode',
    `remark` = '成本审批入账'
WHERE @approval_root_id IS NOT NULL
  AND `menu_id` = @approval_menu_id;

UPDATE `sys_menu`
SET `parent_id` = @approval_menu_id,
    `order_num` = 1,
    `menu_name` = '审批查询',
    `path` = '#',
    `component` = '',
    `menu_type` = 'F',
    `visible` = '0',
    `status` = '0',
    `perms` = 'cost:approval:list',
    `icon` = '#',
    `remark` = ''
WHERE @approval_menu_id IS NOT NULL
  AND `perms` = 'cost:approval:list'
  AND `menu_id` <> @approval_menu_id;

SET @approval_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `parent_id` = @approval_root_id AND `path` = 'cost' AND `perms` = 'cost:approval:list' LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '审批查询', @approval_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'cost:approval:list', '#', 'admin', NOW(), ''
WHERE @approval_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @approval_menu_id AND `perms` = 'cost:approval:list' AND `menu_type` = 'F');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '审批操作', @approval_menu_id, 2, '#', '', '', 1, 0, 'F', '0', '0', 'cost:approval:approve', '#', 'admin', NOW(), ''
WHERE @approval_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @approval_menu_id AND `perms` = 'cost:approval:approve' AND `menu_type` = 'F');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '入账确认', @approval_menu_id, 3, '#', '', '', 1, 0, 'F', '0', '0', 'cost:approval:post', '#', 'admin', NOW(), ''
WHERE @approval_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @approval_menu_id AND `perms` = 'cost:approval:post' AND `menu_type` = 'F');

DELETE duplicate_menu
FROM `sys_menu` duplicate_menu
JOIN `sys_menu` keep_menu
  ON keep_menu.parent_id = duplicate_menu.parent_id
 AND keep_menu.perms = duplicate_menu.perms
 AND keep_menu.menu_type = duplicate_menu.menu_type
 AND keep_menu.menu_id < duplicate_menu.menu_id
WHERE duplicate_menu.parent_id = @approval_menu_id
  AND duplicate_menu.menu_type = 'F'
  AND duplicate_menu.perms IN ('cost:approval:list', 'cost:approval:approve', 'cost:approval:post');
