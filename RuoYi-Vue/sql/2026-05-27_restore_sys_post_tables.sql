-- Restore RuoYi default post tables used by user profile APIs.
-- This script is idempotent and only creates/mends missing base data.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_post`
(
  `post_id`     bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code`   varchar(64)  NOT NULL COMMENT '岗位编码',
  `post_name`   varchar(50)  NOT NULL COMMENT '岗位名称',
  `post_sort`   int(4)       NOT NULL COMMENT '显示顺序',
  `status`      char(1)      NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by`   varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
  `remark`      varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位信息表';

CREATE TABLE IF NOT EXISTS `sys_user_post`
(
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与岗位关联表';

INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
  (1, 'ceo', '董事长', 1, '0', 'admin', NOW(), '', NULL, ''),
  (2, 'pm', '项目经理', 2, '0', 'admin', NOW(), '', NULL, ''),
  (3, 'cost_accountant', '成本会计', 3, '0', 'admin', NOW(), '', NULL, ''),
  (4, 'project_member', '项目成员', 4, '0', 'admin', NOW(), '', NULL, ''),
  (5, 'admin', '系统管理员', 5, '0', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE
  `post_code` = VALUES(`post_code`),
  `post_name` = VALUES(`post_name`),
  `post_sort` = VALUES(`post_sort`),
  `status` = VALUES(`status`),
  `update_by` = 'admin',
  `update_time` = NOW();

INSERT IGNORE INTO `sys_user_post` (`user_id`, `post_id`)
SELECT `user_id`, 5
FROM `sys_user`
WHERE `user_name` = 'admin';

INSERT IGNORE INTO `sys_user_post` (`user_id`, `post_id`)
SELECT `user_id`,
       CASE
         WHEN `user_name` = 'project_manager' THEN 2
         WHEN `user_name` = 'cost_accountant' THEN 3
         WHEN `user_name` = 'project_member' THEN 4
         ELSE 4
       END
FROM `sys_user`
WHERE `user_name` IN ('project_manager', 'cost_accountant', 'project_member');
