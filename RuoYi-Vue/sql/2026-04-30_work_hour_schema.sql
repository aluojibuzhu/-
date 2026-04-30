-- 工时填报模块增量脚本
-- 适用于已导入若依基础表和项目立项模块表的数据库

SET @has_unit_price = (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_cost_category'
    AND COLUMN_NAME = 'unit_price'
);
SET @add_unit_price_sql = IF(
  @has_unit_price = 0,
  'ALTER TABLE `sys_cost_category` ADD COLUMN `unit_price` decimal(14,2) DEFAULT 0.00 COMMENT ''工时单价''',
  'SELECT 1'
);
PREPARE add_unit_price_stmt FROM @add_unit_price_sql;
EXECUTE add_unit_price_stmt;
DEALLOCATE PREPARE add_unit_price_stmt;

UPDATE `sys_cost_category`
SET `unit_price` = 120.00
WHERE `category_id` = 101 AND (`unit_price` IS NULL OR `unit_price` = 0);
UPDATE `sys_cost_category`
SET `unit_price` = 150.00
WHERE `category_id` = 102 AND (`unit_price` IS NULL OR `unit_price` = 0);
UPDATE `sys_cost_category`
SET `unit_price` = 100.00
WHERE `category_id` = 103 AND (`unit_price` IS NULL OR `unit_price` = 0);

CREATE TABLE IF NOT EXISTS `wh_work_hour` (
  `wh_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工时ID',
  `wh_no` varchar(30) NOT NULL COMMENT '工时编号',
  `proj_id` bigint(20) NOT NULL COMMENT '所属项目ID',
  `proj_name` varchar(100) DEFAULT NULL COMMENT '项目名称',
  `node_id` bigint(20) NOT NULL COMMENT 'WBS节点ID',
  `node_name` varchar(50) DEFAULT NULL COMMENT 'WBS节点名称',
  `category_id` bigint(20) NOT NULL COMMENT '成本科目ID',
  `category_name` varchar(50) DEFAULT NULL COMMENT '成本科目名称',
  `work_type` varchar(20) NOT NULL COMMENT '工作类型',
  `work_date` date NOT NULL COMMENT '填报日期',
  `work_hours` decimal(5,1) NOT NULL COMMENT '工时数',
  `unit_price` decimal(14,2) DEFAULT 0.00 COMMENT '工时单价',
  `work_cost` decimal(14,2) DEFAULT 0.00 COMMENT '工时成本',
  `work_desc` varchar(200) NOT NULL COMMENT '工作内容',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0草稿 1审批中 2已通过 3已驳回 4已入账）',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `approve_by` varchar(64) DEFAULT '' COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回原因',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`wh_id`),
  UNIQUE KEY `uk_wh_no` (`wh_no`),
  KEY `idx_wh_proj` (`proj_id`),
  KEY `idx_wh_node` (`node_id`),
  KEY `idx_wh_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工时填报表';

CREATE TABLE IF NOT EXISTS `wh_attachment` (
  `attachment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `wh_id` bigint(20) NOT NULL COMMENT '工时ID',
  `file_name` varchar(255) DEFAULT NULL COMMENT '存储文件名',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`attachment_id`),
  KEY `idx_wh_attachment` (`wh_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工时附件表';

CREATE TABLE IF NOT EXISTS `wh_no_sequence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工时编号序列表';

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '工时工作类型', 'wh_work_type', '0', 'admin', NOW(), '工时填报工作类型'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'wh_work_type');

SET @wh_dict_id = (SELECT `dict_id` FROM `sys_dict_type` WHERE `dict_type` = 'wh_work_type' LIMIT 1);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '设计', 'design', 'wh_work_type', '', 'primary', 'Y', '0', 'admin', NOW(), ''
WHERE @wh_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'wh_work_type' AND `dict_value` = 'design');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '施工', 'construct', 'wh_work_type', '', 'success', 'N', '0', 'admin', NOW(), ''
WHERE @wh_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'wh_work_type' AND `dict_value` = 'construct');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '监理', 'supervise', 'wh_work_type', '', 'warning', 'N', '0', 'admin', NOW(), ''
WHERE @wh_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'wh_work_type' AND `dict_value` = 'supervise');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '管理', 'manage', 'wh_work_type', '', 'info', 'N', '0', 'admin', NOW(), ''
WHERE @wh_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'wh_work_type' AND `dict_value` = 'manage');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '会议', 'meeting', 'wh_work_type', '', 'default', 'N', '0', 'admin', NOW(), ''
WHERE @wh_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'wh_work_type' AND `dict_value` = 'meeting');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 6, '其他', 'other', 'wh_work_type', '', 'default', 'N', '0', 'admin', NOW(), ''
WHERE @wh_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'wh_work_type' AND `dict_value` = 'other');

SET @project_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '项目管理' AND `parent_id` = 0 LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本填报', 0, 2, 'cost', NULL, '', 1, 0, 'M', '0', '0', '', 'money', 'admin', NOW(), '成本填报模块'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '成本填报' AND `parent_id` = 0);

SET @cost_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '成本填报' AND `parent_id` = 0 LIMIT 1);

UPDATE `sys_menu`
SET `parent_id` = @cost_menu_id,
    `order_num` = 1,
    `perms` = 'cost:workHour:list'
WHERE @cost_menu_id IS NOT NULL
  AND `path` = 'workHour'
  AND `perms` = 'project:workHour:list';

UPDATE `sys_menu`
SET `parent_id` = @cost_menu_id,
    `order_num` = 2,
    `perms` = 'cost:workHour:add'
WHERE @cost_menu_id IS NOT NULL
  AND `path` = 'workHour/form'
  AND `perms` = 'project:workHour:add';

UPDATE `sys_menu`
SET `parent_id` = @cost_menu_id,
    `order_num` = 3,
    `perms` = 'cost:workHour:query'
WHERE @cost_menu_id IS NOT NULL
  AND `path` = 'workHour/detail/:id(\\d+)'
  AND `perms` = 'project:workHour:query';

UPDATE `sys_menu`
SET `perms` = REPLACE(`perms`, 'project:workHour:', 'cost:workHour:')
WHERE `perms` LIKE 'project:workHour:%';

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工时填报', @cost_menu_id, 1, 'workHour', 'project/workHour/index', '', 1, 0, 'C', '0', '0', 'cost:workHour:list', 'time-range', 'admin', NOW(), '工时填报列表'
WHERE @cost_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @cost_menu_id AND `path` = 'workHour');

SET @work_hour_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `parent_id` = @cost_menu_id AND `path` = 'workHour' LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '新建工时', @cost_menu_id, 2, 'workHour/form', 'project/workHour/form', '', 1, 0, 'C', '1', '0', 'cost:workHour:add', 'form', 'admin', NOW(), '新建工时'
WHERE @cost_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @cost_menu_id AND `path` = 'workHour/form');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工时详情', @cost_menu_id, 3, 'workHour/detail/:id(\\d+)', 'project/workHour/detail', '', 1, 0, 'C', '1', '0', 'cost:workHour:query', 'eye-open', 'admin', NOW(), '工时详情'
WHERE @cost_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @cost_menu_id AND `path` = 'workHour/detail/:id(\\d+)');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工时查询', @work_hour_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'cost:workHour:query', '#', 'admin', NOW(), ''
WHERE @work_hour_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @work_hour_menu_id AND `perms` = 'cost:workHour:query');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工时新增', @work_hour_menu_id, 2, '#', '', '', 1, 0, 'F', '0', '0', 'cost:workHour:add', '#', 'admin', NOW(), ''
WHERE @work_hour_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @work_hour_menu_id AND `perms` = 'cost:workHour:add');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工时修改', @work_hour_menu_id, 3, '#', '', '', 1, 0, 'F', '0', '0', 'cost:workHour:edit', '#', 'admin', NOW(), ''
WHERE @work_hour_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @work_hour_menu_id AND `perms` = 'cost:workHour:edit');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工时删除', @work_hour_menu_id, 4, '#', '', '', 1, 0, 'F', '0', '0', 'cost:workHour:remove', '#', 'admin', NOW(), ''
WHERE @work_hour_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @work_hour_menu_id AND `perms` = 'cost:workHour:remove');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工时审批', @work_hour_menu_id, 5, '#', '', '', 1, 0, 'F', '0', '0', 'cost:workHour:approve', '#', 'admin', NOW(), ''
WHERE @work_hour_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @work_hour_menu_id AND `perms` = 'cost:workHour:approve');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '工时入账', @work_hour_menu_id, 6, '#', '', '', 1, 0, 'F', '0', '0', 'cost:workHour:post', '#', 'admin', NOW(), ''
WHERE @work_hour_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @work_hour_menu_id AND `perms` = 'cost:workHour:post');
