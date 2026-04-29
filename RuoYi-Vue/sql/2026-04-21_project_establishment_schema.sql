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
