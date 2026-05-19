-- Refine sidebar icons so each module and page better matches its business meaning.

SET NAMES utf8mb4;

UPDATE `sys_menu`
SET `icon` = CASE `menu_id`
  WHEN 100 THEN 'clipboard'   -- 项目管理
  WHEN 2013 THEN 'edit'       -- 成本填报
  WHEN 2027 THEN 'checkbox'   -- 审批入账
  WHEN 2030 THEN 'monitor'    -- 监控预警
  WHEN 2038 THEN 'chart'      -- 成本看板
  WHEN 200 THEN 'system'      -- 系统配置
  WHEN 101 THEN 'table'       -- 项目列表
  WHEN 102 THEN 'form'        -- 新建项目
  WHEN 103 THEN 'eye'         -- 项目详情
  WHEN 202 THEN 'dict'        -- 基础配置
  WHEN 201 THEN 'people'      -- 客户管理
  WHEN 2050 THEN 'peoples'    -- 角色权限配置
  WHEN 2004 THEN 'time-range' -- 工时填报
  WHEN 2005 THEN 'time'       -- 新建工时
  WHEN 2014 THEN 'money'      -- 报销申请
  WHEN 2015 THEN 'form'       -- 新建报销
  WHEN 2029 THEN 'checkbox'   -- 立项审批
  WHEN 2023 THEN 'money'      -- 成本审批
  WHEN 2031 THEN 'dashboard'  -- 预警看板
  WHEN 2032 THEN 'bell'       -- 预警中心
  WHEN 2033 THEN 'tree-table' -- 预警规则
  WHEN 2039 THEN 'chart'      -- 成本总览
  WHEN 2040 THEN 'excel'      -- 专项报表
  WHEN 2044 THEN 'table'      -- 项目资金汇总表
  WHEN 2045 THEN 'tree-table' -- 科目成本明细表
  WHEN 2046 THEN 'list'       -- 入账流水明细表
  WHEN 2047 THEN 'tree'       -- 节点预算执行表
  ELSE `icon`
END,
`update_by` = 'admin',
`update_time` = NOW()
WHERE `menu_id` IN (
  100, 2013, 2027, 2030, 2038, 200,
  101, 102, 103, 202, 201, 2050,
  2004, 2005, 2014, 2015,
  2029, 2023,
  2031, 2032, 2033,
  2039, 2040, 2044, 2045, 2046, 2047
);
