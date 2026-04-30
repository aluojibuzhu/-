-- 报销申请模块增量脚本

CREATE TABLE IF NOT EXISTS `exp_reimbursement` (
  `reimburse_id` bigint NOT NULL AUTO_INCREMENT COMMENT '报销ID',
  `reimburse_no` varchar(30) NOT NULL COMMENT '报销编号',
  `proj_id` bigint NOT NULL COMMENT '项目ID',
  `proj_name` varchar(100) NOT NULL COMMENT '项目名称快照',
  `node_id` bigint NOT NULL COMMENT 'WBS节点ID',
  `node_name` varchar(100) NOT NULL COMMENT 'WBS节点名称快照',
  `category_id` bigint NOT NULL COMMENT '成本科目ID',
  `category_name` varchar(100) NOT NULL COMMENT '成本科目名称快照',
  `expense_type` varchar(30) NOT NULL COMMENT '费用类型',
  `expense_date` date NOT NULL COMMENT '发生日期',
  `amount` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '报销金额',
  `expense_desc` varchar(200) NOT NULL COMMENT '费用说明',
  `invoice_count` int NOT NULL DEFAULT 1 COMMENT '发票张数',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态：0草稿 1审批中 2已通过 3已驳回 4已入账',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `approve_by` varchar(64) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `reject_reason` varchar(200) DEFAULT NULL COMMENT '驳回原因',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`reimburse_id`),
  UNIQUE KEY `uk_exp_reimbursement_no` (`reimburse_no`),
  KEY `idx_exp_reimbursement_proj` (`proj_id`),
  KEY `idx_exp_reimbursement_status` (`status`),
  KEY `idx_exp_reimbursement_date` (`expense_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销申请表';

CREATE TABLE IF NOT EXISTS `exp_attachment` (
  `attachment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `reimburse_id` bigint NOT NULL COMMENT '报销ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`attachment_id`),
  KEY `idx_exp_attachment_reimburse` (`reimburse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销附件表';

CREATE TABLE IF NOT EXISTS `exp_no_sequence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销编号序列表';

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '报销费用类型', 'exp_expense_type', '0', 'admin', NOW(), '报销申请费用类型'
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'exp_expense_type');

SET @exp_dict_id = (SELECT `dict_id` FROM `sys_dict_type` WHERE `dict_type` = 'exp_expense_type' LIMIT 1);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '差旅费', 'travel', 'exp_expense_type', '', 'primary', 'N', '0', 'admin', NOW(), ''
WHERE @exp_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'exp_expense_type' AND `dict_value` = 'travel');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '通讯费', 'communication', 'exp_expense_type', '', 'success', 'N', '0', 'admin', NOW(), ''
WHERE @exp_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'exp_expense_type' AND `dict_value` = 'communication');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '交通费', 'traffic', 'exp_expense_type', '', 'warning', 'N', '0', 'admin', NOW(), ''
WHERE @exp_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'exp_expense_type' AND `dict_value` = 'traffic');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '招待费', 'entertainment', 'exp_expense_type', '', 'danger', 'N', '0', 'admin', NOW(), ''
WHERE @exp_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'exp_expense_type' AND `dict_value` = 'entertainment');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '办公费', 'office', 'exp_expense_type', '', 'info', 'N', '0', 'admin', NOW(), ''
WHERE @exp_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'exp_expense_type' AND `dict_value` = 'office');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 6, '其他', 'other', 'exp_expense_type', '', 'default', 'N', '0', 'admin', NOW(), ''
WHERE @exp_dict_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'exp_expense_type' AND `dict_value` = 'other');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本填报', 0, 2, 'cost', NULL, '', 1, 0, 'M', '0', '0', '', 'money', 'admin', NOW(), '成本填报模块'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '成本填报' AND `parent_id` = 0);

SET @cost_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '成本填报' AND `parent_id` = 0 LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报销申请', @cost_menu_id, 2, 'reimbursement', 'project/reimbursement/index', '', 1, 0, 'C', '0', '0', 'cost:reimbursement:list', 'shopping', 'admin', NOW(), '报销申请列表'
WHERE @cost_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @cost_menu_id AND `path` = 'reimbursement');

SET @reimbursement_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `parent_id` = @cost_menu_id AND `path` = 'reimbursement' LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '新建报销', @cost_menu_id, 3, 'reimbursement/form', 'project/reimbursement/form', '', 1, 0, 'C', '1', '0', 'cost:reimbursement:add', 'form', 'admin', NOW(), '新建报销'
WHERE @cost_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @cost_menu_id AND `path` = 'reimbursement/form');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报销详情', @cost_menu_id, 4, 'reimbursement/detail/:id(\\d+)', 'project/reimbursement/detail', '', 1, 0, 'C', '1', '0', 'cost:reimbursement:query', 'eye-open', 'admin', NOW(), '报销详情'
WHERE @cost_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @cost_menu_id AND `path` = 'reimbursement/detail/:id(\\d+)');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报销查询', @reimbursement_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'cost:reimbursement:query', '#', 'admin', NOW(), ''
WHERE @reimbursement_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @reimbursement_menu_id AND `perms` = 'cost:reimbursement:query');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报销新增', @reimbursement_menu_id, 2, '#', '', '', 1, 0, 'F', '0', '0', 'cost:reimbursement:add', '#', 'admin', NOW(), ''
WHERE @reimbursement_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @reimbursement_menu_id AND `perms` = 'cost:reimbursement:add');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报销修改', @reimbursement_menu_id, 3, '#', '', '', 1, 0, 'F', '0', '0', 'cost:reimbursement:edit', '#', 'admin', NOW(), ''
WHERE @reimbursement_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @reimbursement_menu_id AND `perms` = 'cost:reimbursement:edit');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报销删除', @reimbursement_menu_id, 4, '#', '', '', 1, 0, 'F', '0', '0', 'cost:reimbursement:remove', '#', 'admin', NOW(), ''
WHERE @reimbursement_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @reimbursement_menu_id AND `perms` = 'cost:reimbursement:remove');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报销审批', @reimbursement_menu_id, 5, '#', '', '', 1, 0, 'F', '0', '0', 'cost:reimbursement:approve', '#', 'admin', NOW(), ''
WHERE @reimbursement_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @reimbursement_menu_id AND `perms` = 'cost:reimbursement:approve');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '报销入账', @reimbursement_menu_id, 6, '#', '', '', 1, 0, 'F', '0', '0', 'cost:reimbursement:post', '#', 'admin', NOW(), ''
WHERE @reimbursement_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @reimbursement_menu_id AND `perms` = 'cost:reimbursement:post');
