-- 项目立项模块建表脚本
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
  `total_budget` decimal(14,2) DEFAULT 0.00 COMMENT '项目总预算',
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

CREATE TABLE `proj_wbs_node` (
  `node_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `proj_id` bigint(20) NOT NULL COMMENT '所属项目ID',
  `node_no` varchar(30) NOT NULL COMMENT '节点编号（ND-001）',
  `node_name` varchar(50) NOT NULL COMMENT '节点名称',
  `plan_finish_date` date DEFAULT NULL COMMENT '预计完成日期',
  `node_budget` decimal(14,2) DEFAULT 0.00 COMMENT '节点预算',
  `order_num` int(4) DEFAULT 0 COMMENT '节点顺序',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`node_id`),
  KEY `idx_proj_id` (`proj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WBS节点表';

CREATE TABLE `proj_cost_allocation` (
  `allocation_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分配ID',
  `proj_id` bigint(20) NOT NULL COMMENT '项目ID',
  `node_id` bigint(20) NOT NULL COMMENT '节点ID',
  `category_id` bigint(20) NOT NULL COMMENT '成本科目ID',
  `category_name` varchar(50) DEFAULT NULL COMMENT '科目名称',
  `allocation_amount` decimal(14,2) DEFAULT 0.00 COMMENT '分配金额',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`allocation_id`),
  KEY `idx_proj_node` (`proj_id`, `node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本分配表';

CREATE TABLE `proj_no_sequence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目编号序列表';

CREATE TABLE `proj_node_no_sequence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点编号序列表';

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('项目管理', 0, 5, 'project', NULL, '', 1, 0, 'M', '0', '0', '', 'build', 'admin', NOW(), '项目管理目录');
SET @project_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('项目列表', @project_menu_id, 1, 'projInfo', 'project/projInfo/index', '', 1, 0, 'C', '0', '0', 'project:projInfo:list', 'list', 'admin', NOW(), '项目立项列表');
SET @proj_info_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('新建项目', @project_menu_id, 2, 'projInfo/form', 'project/projInfo/form', '', 1, 0, 'C', '1', '0', 'project:projInfo:add', 'form', 'admin', NOW(), '新建项目'),
('项目详情', @project_menu_id, 3, 'projInfo/detail/:id(\\d+)', 'project/projInfo/detail', '', 1, 0, 'C', '1', '0', 'project:projInfo:query', 'eye', 'admin', NOW(), '项目详情');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('基础配置', @project_menu_id, 4, 'basicConfig', 'project/costCategory/index', '', 1, 0, 'C', '0', '0', 'project:costCategory:list', 'tree-table', 'admin', NOW(), '基础配置');
SET @cost_category_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('项目查询', @proj_info_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'project:projInfo:query', '#', 'admin', NOW(), ''),
('项目新增', @proj_info_menu_id, 2, '#', '', '', 1, 0, 'F', '0', '0', 'project:projInfo:add', '#', 'admin', NOW(), ''),
('项目修改', @proj_info_menu_id, 3, '#', '', '', 1, 0, 'F', '0', '0', 'project:projInfo:edit', '#', 'admin', NOW(), ''),
('项目删除', @proj_info_menu_id, 4, '#', '', '', 1, 0, 'F', '0', '0', 'project:projInfo:remove', '#', 'admin', NOW(), ''),
('项目审批', @proj_info_menu_id, 5, '#', '', '', 1, 0, 'F', '0', '0', 'project:projInfo:approve', '#', 'admin', NOW(), ''),
('客户查询', @proj_info_menu_id, 6, '#', '', '', 1, 0, 'F', '0', '0', 'project:customer:list', '#', 'admin', NOW(), ''),
('客户详情', @proj_info_menu_id, 7, '#', '', '', 1, 0, 'F', '0', '0', 'project:customer:query', '#', 'admin', NOW(), ''),
('客户新增', @proj_info_menu_id, 8, '#', '', '', 1, 0, 'F', '0', '0', 'project:customer:add', '#', 'admin', NOW(), ''),
('客户修改', @proj_info_menu_id, 9, '#', '', '', 1, 0, 'F', '0', '0', 'project:customer:edit', '#', 'admin', NOW(), ''),
('客户删除', @proj_info_menu_id, 10, '#', '', '', 1, 0, 'F', '0', '0', 'project:customer:remove', '#', 'admin', NOW(), '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('成本科目查询', @cost_category_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'project:costCategory:query', '#', 'admin', NOW(), ''),
('成本科目新增', @cost_category_menu_id, 2, '#', '', '', 1, 0, 'F', '0', '0', 'project:costCategory:add', '#', 'admin', NOW(), ''),
('成本科目修改', @cost_category_menu_id, 3, '#', '', '', 1, 0, 'F', '0', '0', 'project:costCategory:edit', '#', 'admin', NOW(), ''),
('成本科目删除', @cost_category_menu_id, 4, '#', '', '', 1, 0, 'F', '0', '0', 'project:costCategory:remove', '#', 'admin', NOW(), '');
