-- 就诊通知功能增强 - 数据库更新脚本

-- 1. 为 jiuzhentongzhi 表添加新字段
ALTER TABLE `jiuzhentongzhi` 
ADD COLUMN `sendstatus` INT(11) DEFAULT 0 COMMENT '发送状态：0-待发送，1-已发送，2-发送失败' AFTER `tongzhibeizhu`,
ADD COLUMN `retrycount` INT(11) DEFAULT 0 COMMENT '重试次数' AFTER `sendstatus`,
ADD COLUMN `failreason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因' AFTER `retrycount`,
ADD COLUMN `lastsendtime` DATETIME DEFAULT NULL COMMENT '最后发送时间' AFTER `failreason`;

-- 2. 创建 tongzhijilu 通知记录表
DROP TABLE IF EXISTS `tongzhijilu`;
CREATE TABLE `tongzhijilu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `addtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `tongzhiid` bigint(20) DEFAULT NULL COMMENT '关联就诊通知id',
  `tongzhibianhao` varchar(200) DEFAULT NULL COMMENT '通知编号',
  `sendtime` datetime DEFAULT NULL COMMENT '发送时间',
  `sendstatus` int(11) DEFAULT NULL COMMENT '发送状态：0-失败，1-成功',
  `failreason` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `operator` varchar(200) DEFAULT NULL COMMENT '操作人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表';
