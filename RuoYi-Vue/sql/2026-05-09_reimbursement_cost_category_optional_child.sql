-- 报销申请成本科目调整：一级成本科目必填，二级成本科目可选

ALTER TABLE `exp_reimbursement`
  MODIFY COLUMN `category_id` bigint DEFAULT NULL COMMENT '二级成本科目ID',
  MODIFY COLUMN `category_name` varchar(100) DEFAULT NULL COMMENT '二级成本科目名称快照',
  MODIFY COLUMN `expense_type` varchar(30) NOT NULL COMMENT '一级成本科目名称快照';

ALTER TABLE `cost_posting_record`
  MODIFY COLUMN `category_id` bigint DEFAULT NULL COMMENT '成本科目ID',
  MODIFY COLUMN `category_name` varchar(100) DEFAULT NULL COMMENT '成本科目名称快照';
