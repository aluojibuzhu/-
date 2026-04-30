-- 成本科目维护菜单增量脚本
-- 适用于已导入 2026-04-21_project_establishment_schema.sql 的数据库

SET @project_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '项目管理' AND parent_id = 0 LIMIT 1);

-- 项目详情是隐藏路由，只能从项目列表携带项目ID进入，不能作为左侧菜单直接打开
UPDATE sys_menu
SET visible = '1', status = '0'
WHERE component = 'project/projInfo/detail'
   OR path IN ('projDetail', 'projInfo/detail/:id(\\d+)');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '基础配置', @project_menu_id, 4, 'basicConfig', 'project/costCategory/index', '', 1, 0, 'C', '0', '0', 'project:costCategory:list', 'tree-table', 'admin', NOW(), '基础配置'
WHERE @project_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu
    WHERE menu_type = 'C'
      AND (path IN ('basicConfig', 'costCategory')
        OR perms IN ('project:costCategory:list', 'system:costCategory:list')
        OR component IN ('project/costCategory/index', 'system/costCategory/index'))
  );

UPDATE sys_menu
SET menu_name = '基础配置',
    parent_id = @project_menu_id,
    order_num = 4,
    path = 'basicConfig',
    component = 'project/costCategory/index',
    perms = 'project:costCategory:list',
    icon = 'tree-table',
    visible = '0',
    status = '0',
    remark = '基础配置'
WHERE path IN ('basicConfig', 'costCategory')
  AND menu_type = 'C'
  AND @project_menu_id IS NOT NULL
  AND (perms IN ('project:costCategory:list', 'system:costCategory:list') OR component IN ('project/costCategory/index', 'system/costCategory/index'));

SET @cost_category_menu_id = (SELECT menu_id FROM sys_menu WHERE parent_id = @project_menu_id AND path = 'basicConfig' LIMIT 1);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本科目查询', @cost_category_menu_id, 1, '#', '', '', 1, 0, 'F', '0', '0', 'project:costCategory:query', '#', 'admin', NOW(), ''
WHERE @cost_category_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @cost_category_menu_id AND perms = 'project:costCategory:query');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本科目新增', @cost_category_menu_id, 2, '#', '', '', 1, 0, 'F', '0', '0', 'project:costCategory:add', '#', 'admin', NOW(), ''
WHERE @cost_category_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @cost_category_menu_id AND perms = 'project:costCategory:add');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本科目修改', @cost_category_menu_id, 3, '#', '', '', 1, 0, 'F', '0', '0', 'project:costCategory:edit', '#', 'admin', NOW(), ''
WHERE @cost_category_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @cost_category_menu_id AND perms = 'project:costCategory:edit');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '成本科目删除', @cost_category_menu_id, 4, '#', '', '', 1, 0, 'F', '0', '0', 'project:costCategory:remove', '#', 'admin', NOW(), ''
WHERE @cost_category_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = @cost_category_menu_id AND perms = 'project:costCategory:remove');

UPDATE sys_menu
SET parent_id = @cost_category_menu_id, status = '0', visible = '0'
WHERE perms IN ('system:costCategory:query', 'system:costCategory:add', 'system:costCategory:edit', 'system:costCategory:remove');

UPDATE sys_menu
SET perms = REPLACE(perms, 'system:costCategory', 'project:costCategory')
WHERE perms LIKE 'system:costCategory%';
