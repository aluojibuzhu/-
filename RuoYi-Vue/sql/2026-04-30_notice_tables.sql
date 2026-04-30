-- Fix missing notice tables for existing databases.
-- The header notice widget calls /system/notice/listTop after login.
-- Run this script against the ry-vue database if sys_notice is missing.

CREATE TABLE IF NOT EXISTS `sys_notice` (
  `notice_id` int(4) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) NOT NULL COMMENT '公告标题',
  `notice_type` char(1) NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob DEFAULT NULL COMMENT '公告内容',
  `status` char(1) DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

CREATE TABLE IF NOT EXISTS `sys_notice_read` (
  `read_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '已读主键',
  `notice_id` int(4) NOT NULL COMMENT '公告ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `read_time` datetime NOT NULL COMMENT '阅读时间',
  PRIMARY KEY (`read_id`),
  UNIQUE KEY `uk_user_notice` (`user_id`,`notice_id`) COMMENT '同一用户同一公告只记录一次'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录表';

INSERT INTO `sys_notice` (
  `notice_id`,
  `notice_title`,
  `notice_type`,
  `notice_content`,
  `status`,
  `create_by`,
  `create_time`,
  `remark`
)
SELECT
  1,
  '系统欢迎',
  '2',
  '欢迎使用工程项目成本管理系统',
  '0',
  'admin',
  NOW(),
  '初始化公告'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_notice` WHERE `notice_id` = 1
);
