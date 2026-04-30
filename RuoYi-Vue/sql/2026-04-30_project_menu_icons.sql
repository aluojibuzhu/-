-- 项目管理菜单图标修正
-- 使用 ruoyi-ui/src/assets/icons/svg 中已存在的图标，避免侧栏出现空白图标。

UPDATE `sys_menu`
SET `icon` = 'build'
WHERE `menu_name` = '项目管理'
  AND `parent_id` = 0;

UPDATE `sys_menu`
SET `icon` = 'list'
WHERE `menu_name` = '项目列表'
  AND `parent_id` = (SELECT `menu_id` FROM (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '项目管理' AND `parent_id` = 0 LIMIT 1) p);

UPDATE `sys_menu`
SET `icon` = 'form'
WHERE `menu_name` = '新建项目'
  AND `parent_id` = (SELECT `menu_id` FROM (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '项目管理' AND `parent_id` = 0 LIMIT 1) p);

UPDATE `sys_menu`
SET `icon` = 'eye'
WHERE `menu_name` = '项目详情'
  AND `parent_id` = (SELECT `menu_id` FROM (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '项目管理' AND `parent_id` = 0 LIMIT 1) p);

UPDATE `sys_menu`
SET `icon` = 'tool'
WHERE `menu_name` = '基础配置'
  AND `parent_id` = (SELECT `menu_id` FROM (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '项目管理' AND `parent_id` = 0 LIMIT 1) p);
