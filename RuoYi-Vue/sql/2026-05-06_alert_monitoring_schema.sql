-- 监控预警模块：规则、记录、日志与菜单
-- 可重复执行；若已有若依原生“系统监控(/monitor)”，本模块使用 alertMonitor 避免路由冲突。

CREATE TABLE IF NOT EXISTS `proj_alert_rule` (
  `rule_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(30) NOT NULL COMMENT '规则类型',
  `alert_level` char(1) NOT NULL COMMENT '预警级别：1黄 2橙 3红',
  `threshold_value` decimal(18,2) NOT NULL COMMENT '阈值',
  `compare_operator` varchar(10) NOT NULL DEFAULT '>=' COMMENT '比较符',
  `scope_type` char(1) NOT NULL DEFAULT '0' COMMENT '适用范围：0全局 1项目 2科目',
  `scope_value` varchar(500) DEFAULT NULL COMMENT '范围值',
  `notify_enabled` char(1) NOT NULL DEFAULT '1' COMMENT '是否通知',
  `notify_channels` varchar(100) DEFAULT 'SYSTEM' COMMENT '通知渠道',
  `notify_roles` varchar(200) DEFAULT NULL COMMENT '通知角色',
  `notify_silence_hours` int(11) NOT NULL DEFAULT 24 COMMENT '静默小时',
  `enabled` char(1) NOT NULL DEFAULT '1' COMMENT '启用状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`rule_id`),
  KEY `idx_alert_rule_type` (`rule_type`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目预警规则表';

CREATE TABLE IF NOT EXISTS `proj_alert_record` (
  `alert_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预警ID',
  `alert_no` varchar(30) NOT NULL COMMENT '预警编号',
  `rule_id` bigint(20) DEFAULT NULL COMMENT '规则ID',
  `rule_name` varchar(100) DEFAULT NULL COMMENT '规则名称快照',
  `rule_type` varchar(30) NOT NULL COMMENT '规则类型',
  `alert_level` char(1) NOT NULL COMMENT '预警级别',
  `alert_type` varchar(30) NOT NULL DEFAULT 'PROJECT' COMMENT '对象类型',
  `proj_id` bigint(20) NOT NULL COMMENT '项目ID',
  `proj_no` varchar(50) DEFAULT NULL COMMENT '项目编号快照',
  `proj_name` varchar(120) DEFAULT NULL COMMENT '项目名称快照',
  `node_id` bigint(20) DEFAULT NULL COMMENT 'WBS节点ID',
  `node_name` varchar(120) DEFAULT NULL COMMENT 'WBS节点名称快照',
  `category_id` bigint(20) DEFAULT NULL COMMENT '成本科目ID',
  `category_name` varchar(120) DEFAULT NULL COMMENT '成本科目名称快照',
  `total_budget` decimal(18,2) DEFAULT 0.00 COMMENT '总预算快照',
  `actual_cost` decimal(18,2) DEFAULT 0.00 COMMENT '实际成本快照',
  `budget_balance` decimal(18,2) DEFAULT 0.00 COMMENT '预算余额快照',
  `current_value` decimal(18,2) DEFAULT NULL COMMENT '当前值',
  `threshold_value` decimal(18,2) DEFAULT NULL COMMENT '阈值',
  `trigger_bill_type` varchar(30) DEFAULT NULL COMMENT '触发单据类型',
  `trigger_bill_id` bigint(20) DEFAULT NULL COMMENT '触发单据ID',
  `trigger_bill_no` varchar(50) DEFAULT NULL COMMENT '触发单据编号',
  `trigger_amount` decimal(18,2) DEFAULT NULL COMMENT '触发金额',
  `condition_desc` varchar(500) DEFAULT NULL COMMENT '触发条件描述',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态：0待处理 1已确认 2已忽略 3跟进中 4已关闭',
  `handler` varchar(64) DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `closed_by` varchar(64) DEFAULT NULL COMMENT '关闭人',
  `closed_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `close_remark` varchar(500) DEFAULT NULL COMMENT '关闭备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`alert_id`),
  UNIQUE KEY `uk_alert_no` (`alert_no`),
  KEY `idx_alert_project` (`proj_id`, `status`),
  KEY `idx_alert_rule` (`rule_id`, `create_time`),
  KEY `idx_alert_level` (`alert_level`, `status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目预警记录表';

CREATE TABLE IF NOT EXISTS `proj_alert_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `alert_id` bigint(20) NOT NULL COMMENT '预警ID',
  `action_type` varchar(30) NOT NULL COMMENT '动作类型',
  `action_by` varchar(64) DEFAULT '' COMMENT '操作人',
  `action_time` datetime DEFAULT NULL COMMENT '操作时间',
  `action_remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`log_id`),
  KEY `idx_alert_log_alert` (`alert_id`, `action_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目预警处理日志表';

CREATE TABLE IF NOT EXISTS `proj_alert_no_sequence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '序列ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警编号序列表';

INSERT INTO `proj_alert_rule` (`rule_name`, `rule_type`, `alert_level`, `threshold_value`, `compare_operator`, `scope_type`, `notify_enabled`, `notify_channels`, `notify_roles`, `notify_silence_hours`, `enabled`, `remark`, `create_by`, `create_time`)
SELECT '项目预算执行率达到80%', 'EXEC_RATE', '1', 80.00, '>=', '0', '1', 'SYSTEM', 'project_manager,cost_manager', 24, '1', '黄色预算执行率预警', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `proj_alert_rule` WHERE `rule_type` = 'EXEC_RATE' AND `alert_level` = '1');

INSERT INTO `proj_alert_rule` (`rule_name`, `rule_type`, `alert_level`, `threshold_value`, `compare_operator`, `scope_type`, `notify_enabled`, `notify_channels`, `notify_roles`, `notify_silence_hours`, `enabled`, `remark`, `create_by`, `create_time`)
SELECT '项目预算执行率达到90%', 'EXEC_RATE', '2', 90.00, '>=', '0', '1', 'SYSTEM', 'project_manager,cost_manager', 24, '1', '橙色预算执行率预警', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `proj_alert_rule` WHERE `rule_type` = 'EXEC_RATE' AND `alert_level` = '2');

INSERT INTO `proj_alert_rule` (`rule_name`, `rule_type`, `alert_level`, `threshold_value`, `compare_operator`, `scope_type`, `notify_enabled`, `notify_channels`, `notify_roles`, `notify_silence_hours`, `enabled`, `remark`, `create_by`, `create_time`)
SELECT '项目预算执行率达到100%', 'EXEC_RATE', '3', 100.00, '>=', '0', '1', 'SYSTEM', 'project_manager,cost_manager', 24, '1', '红色预算执行率预警', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `proj_alert_rule` WHERE `rule_type` = 'EXEC_RATE' AND `alert_level` = '3');

INSERT INTO `proj_alert_rule` (`rule_name`, `rule_type`, `alert_level`, `threshold_value`, `compare_operator`, `scope_type`, `notify_enabled`, `notify_channels`, `notify_roles`, `notify_silence_hours`, `enabled`, `remark`, `create_by`, `create_time`)
SELECT '单笔入账金额超过50000元', 'SINGLE_AMOUNT', '2', 50000.00, '>=', '0', '1', 'SYSTEM', 'cost_manager', 12, '1', '大额成本入账预警', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `proj_alert_rule` WHERE `rule_type` = 'SINGLE_AMOUNT');

INSERT INTO `proj_alert_rule` (`rule_name`, `rule_type`, `alert_level`, `threshold_value`, `compare_operator`, `scope_type`, `notify_enabled`, `notify_channels`, `notify_roles`, `notify_silence_hours`, `enabled`, `remark`, `create_by`, `create_time`)
SELECT '项目预算余额低于10%', 'BALANCE_RATE', '2', 10.00, '<=', '0', '1', 'SYSTEM', 'project_manager,cost_manager', 24, '1', '预算余额不足预警', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `proj_alert_rule` WHERE `rule_type` = 'BALANCE_RATE');

INSERT INTO `proj_alert_rule` (`rule_name`, `rule_type`, `alert_level`, `threshold_value`, `compare_operator`, `scope_type`, `notify_enabled`, `notify_channels`, `notify_roles`, `notify_silence_hours`, `enabled`, `remark`, `create_by`, `create_time`)
SELECT '项目逾期超过30天', 'OVERDUE', '2', 30.00, '>=', '0', '1', 'SYSTEM', 'project_manager,cost_manager', 24, '1', '项目计划竣工后仍未完工的逾期预警', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `proj_alert_rule` WHERE `rule_type` = 'OVERDUE');

INSERT INTO `proj_alert_rule` (`rule_name`, `rule_type`, `alert_level`, `threshold_value`, `compare_operator`, `scope_type`, `notify_enabled`, `notify_channels`, `notify_roles`, `notify_silence_hours`, `enabled`, `remark`, `create_by`, `create_time`)
SELECT '项目连续3个月无入账', 'INACTIVE', '1', 3.00, '>=', '0', '1', 'SYSTEM', 'project_manager,cost_manager', 24, '1', '项目长期无成本入账的静默预警', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `proj_alert_rule` WHERE `rule_type` = 'INACTIVE');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '监控预警', 0, 4, 'alertMonitor', NULL, '', 1, 0, 'M', '0', '0', '', 'warning', 'admin', NOW(), '监控预警模块'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '监控预警' AND `parent_id` = 0);

SET @alert_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '监控预警' AND `parent_id` = 0 LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '预警看板', @alert_menu_id, 1, 'dashboard', 'project/alertDashboard/index', '', 1, 0, 'C', '0', '0', 'alert:dashboard:view', 'dashboard', 'admin', NOW(), '预警看板'
WHERE @alert_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @alert_menu_id AND `path` = 'dashboard');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '预警中心', @alert_menu_id, 2, 'record', 'project/alertRecord/index', '', 1, 0, 'C', '0', '0', 'alert:record:list', 'bell', 'admin', NOW(), '预警中心'
WHERE @alert_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @alert_menu_id AND `path` = 'record');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '预警规则', @alert_menu_id, 3, 'rule', 'project/alertRule/index', '', 1, 0, 'C', '0', '0', 'alert:rule:list', 'tree-table', 'admin', NOW(), '预警规则配置'
WHERE @alert_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @alert_menu_id AND `path` = 'rule');

SET @alert_record_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `parent_id` = @alert_menu_id AND `path` = 'record' LIMIT 1);
SET @alert_rule_menu_id = (SELECT `menu_id` FROM `sys_menu` WHERE `parent_id` = @alert_menu_id AND `path` = 'rule' LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '预警处理', @alert_record_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'alert:record:handle', '#', 'admin', NOW(), ''
WHERE @alert_record_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @alert_record_menu_id AND `perms` = 'alert:record:handle');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '预警规则新增', @alert_rule_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'alert:rule:add', '#', 'admin', NOW(), ''
WHERE @alert_rule_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @alert_rule_menu_id AND `perms` = 'alert:rule:add');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '预警规则修改', @alert_rule_menu_id, 2, '#', '', '', 1, 0, 'F', '0', '0', 'alert:rule:edit', '#', 'admin', NOW(), ''
WHERE @alert_rule_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @alert_rule_menu_id AND `perms` = 'alert:rule:edit');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '预警规则删除', @alert_rule_menu_id, 3, '#', '', '', 1, 0, 'F', '0', '0', 'alert:rule:remove', '#', 'admin', NOW(), ''
WHERE @alert_rule_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `parent_id` = @alert_rule_menu_id AND `perms` = 'alert:rule:remove');

INSERT INTO `sys_job` (`job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `remark`)
SELECT '预警每日扫描', 'DEFAULT', 'alertDailyScanTask.scan', '0 0 2 * * ?', '3', '1', '0', 'admin', NOW(), '扫描项目逾期与长期无入账规则，默认暂停，可在系统监控-定时任务中启用'
WHERE EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sys_job')
  AND NOT EXISTS (SELECT 1 FROM `sys_job` WHERE `invoke_target` = 'alertDailyScanTask.scan');
