-- Repair demo business dates so they are both business-valid and real-world-valid.
-- Rule:
--   1. Actual work/reimbursement/posting dates must stay inside each project plan period.
--   2. Actual business dates and audit times must not be in the future.
-- Project plan end dates may remain in the future because they are schedules, not occurred facts.

SET NAMES utf8mb4;

DROP TEMPORARY TABLE IF EXISTS `_project_date_map`;
CREATE TEMPORARY TABLE `_project_date_map` (
  `proj_id` bigint(20) NOT NULL PRIMARY KEY,
  `old_start` date NOT NULL,
  `old_end` date NOT NULL,
  `new_start` date NOT NULL,
  `new_end` date NOT NULL,
  `actual_end` date NOT NULL,
  `old_duration` int NOT NULL,
  `actual_duration` int NOT NULL
) ENGINE=Memory;

INSERT INTO `_project_date_map` (`proj_id`, `old_start`, `old_end`, `new_start`, `new_end`, `actual_end`, `old_duration`, `actual_duration`)
SELECT
  `proj_id`,
  `plan_start_date`,
  `plan_end_date`,
  DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 120 DAY), INTERVAL ((`proj_id` % 5) * 10) DAY) AS `new_start`,
  DATE_ADD(
    DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 120 DAY), INTERVAL ((`proj_id` % 5) * 10) DAY),
    INTERVAL GREATEST(DATEDIFF(`plan_end_date`, `plan_start_date`), 1) DAY
  ) AS `new_end`,
  CURDATE() AS `actual_end`,
  GREATEST(DATEDIFF(`plan_end_date`, `plan_start_date`), 1) AS `old_duration`,
  GREATEST(DATEDIFF(CURDATE(), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 120 DAY), INTERVAL ((`proj_id` % 5) * 10) DAY)), 1) AS `actual_duration`
FROM `proj_info`
WHERE `del_flag` = '0'
  AND `plan_start_date` IS NOT NULL
  AND `plan_end_date` IS NOT NULL;

-- Project plan dates: keep realistic schedules, with starts before today and ends based on original duration.
UPDATE `proj_info` p
JOIN `_project_date_map` m ON m.`proj_id` = p.`proj_id`
SET p.`plan_start_date` = m.`new_start`,
    p.`plan_end_date` = m.`new_end`,
    p.`update_by` = 'admin',
    p.`update_time` = NOW();

-- WBS planned finish dates remain planned dates and are mapped into the new project period.
UPDATE `proj_wbs_node` w
JOIN `_project_date_map` m ON m.`proj_id` = w.`proj_id`
SET w.`plan_finish_date` = LEAST(
      m.`new_end`,
      GREATEST(
        m.`new_start`,
        DATE_ADD(m.`new_start`, INTERVAL ROUND(GREATEST(DATEDIFF(w.`plan_finish_date`, m.`old_start`), 0) / m.`old_duration` * DATEDIFF(m.`new_end`, m.`new_start`)) DAY)
      )
    ),
    w.`update_by` = 'admin',
    w.`update_time` = NOW()
WHERE w.`del_flag` = '0'
  AND w.`plan_finish_date` IS NOT NULL;

DROP TEMPORARY TABLE IF EXISTS `_work_date_fix`;
CREATE TEMPORARY TABLE `_work_date_fix` (
  `wh_id` bigint(20) NOT NULL PRIMARY KEY,
  `new_work_date` date NOT NULL
) ENGINE=Memory;

INSERT INTO `_work_date_fix` (`wh_id`, `new_work_date`)
SELECT
  w.`wh_id`,
  LEAST(
    m.`actual_end`,
    GREATEST(
      m.`new_start`,
      DATE_ADD(m.`new_start`, INTERVAL ROUND(GREATEST(DATEDIFF(w.`work_date`, m.`old_start`), 0) / m.`old_duration` * m.`actual_duration`) DAY)
    )
  ) AS `new_work_date`
FROM `wh_work_hour` w
JOIN `_project_date_map` m ON m.`proj_id` = w.`proj_id`
WHERE w.`work_date` IS NOT NULL;

UPDATE `wh_work_hour` w
JOIN `_work_date_fix` f ON f.`wh_id` = w.`wh_id`
SET w.`work_date` = f.`new_work_date`,
    w.`submit_time` = CASE
      WHEN f.`new_work_date` = CURDATE() THEN DATE_SUB(NOW(), INTERVAL 3 HOUR)
      ELSE TIMESTAMP(f.`new_work_date`, '09:00:00')
    END,
    w.`approve_time` = CASE
      WHEN w.`approve_time` IS NULL THEN NULL
      WHEN f.`new_work_date` = CURDATE() THEN DATE_SUB(NOW(), INTERVAL 2 HOUR)
      ELSE TIMESTAMP(f.`new_work_date`, '10:00:00')
    END,
    w.`update_by` = 'admin',
    w.`update_time` = NOW();

DROP TEMPORARY TABLE IF EXISTS `_reimburse_date_fix`;
CREATE TEMPORARY TABLE `_reimburse_date_fix` (
  `reimburse_id` bigint(20) NOT NULL PRIMARY KEY,
  `new_expense_date` date NOT NULL
) ENGINE=Memory;

INSERT INTO `_reimburse_date_fix` (`reimburse_id`, `new_expense_date`)
SELECT
  e.`reimburse_id`,
  LEAST(
    m.`actual_end`,
    GREATEST(
      m.`new_start`,
      DATE_ADD(m.`new_start`, INTERVAL ROUND(GREATEST(DATEDIFF(e.`expense_date`, m.`old_start`), 0) / m.`old_duration` * m.`actual_duration`) DAY)
    )
  ) AS `new_expense_date`
FROM `exp_reimbursement` e
JOIN `_project_date_map` m ON m.`proj_id` = e.`proj_id`
WHERE e.`expense_date` IS NOT NULL;

UPDATE `exp_reimbursement` e
JOIN `_reimburse_date_fix` f ON f.`reimburse_id` = e.`reimburse_id`
SET e.`expense_date` = f.`new_expense_date`,
    e.`submit_time` = CASE
      WHEN f.`new_expense_date` = CURDATE() THEN DATE_SUB(NOW(), INTERVAL 3 HOUR)
      ELSE TIMESTAMP(f.`new_expense_date`, '09:15:00')
    END,
    e.`approve_time` = CASE
      WHEN e.`approve_time` IS NULL THEN NULL
      WHEN f.`new_expense_date` = CURDATE() THEN DATE_SUB(NOW(), INTERVAL 2 HOUR)
      ELSE TIMESTAMP(f.`new_expense_date`, '10:15:00')
    END,
    e.`update_by` = 'admin',
    e.`update_time` = NOW();

-- Posting follows its source bill approval time, and is capped before now when the source date is today.
UPDATE `cost_posting_record` r
JOIN `wh_work_hour` w ON r.`bill_type` = 'WORK_HOUR' AND r.`bill_id` = w.`wh_id`
SET r.`post_time` = CASE
      WHEN w.`work_date` = CURDATE() THEN DATE_SUB(NOW(), INTERVAL 1 HOUR)
      ELSE DATE_ADD(w.`approve_time`, INTERVAL 30 MINUTE)
    END,
    r.`create_time` = CASE
      WHEN w.`work_date` = CURDATE() THEN DATE_SUB(NOW(), INTERVAL 1 HOUR)
      ELSE DATE_ADD(w.`approve_time`, INTERVAL 30 MINUTE)
    END;

UPDATE `cost_posting_record` r
JOIN `exp_reimbursement` e ON r.`bill_type` = 'REIMBURSEMENT' AND r.`bill_id` = e.`reimburse_id`
SET r.`post_time` = CASE
      WHEN e.`expense_date` = CURDATE() THEN DATE_SUB(NOW(), INTERVAL 1 HOUR)
      ELSE DATE_ADD(e.`approve_time`, INTERVAL 30 MINUTE)
    END,
    r.`create_time` = CASE
      WHEN e.`expense_date` = CURDATE() THEN DATE_SUB(NOW(), INTERVAL 1 HOUR)
      ELSE DATE_ADD(e.`approve_time`, INTERVAL 30 MINUTE)
    END;

-- Alert timestamps are operational records. Keep them before now and internally ordered.
UPDATE `proj_alert_record`
SET `handle_time` = CASE
      WHEN `handle_time` IS NOT NULL AND `handle_time` < `create_time` THEN DATE_ADD(`create_time`, INTERVAL 10 MINUTE)
      ELSE `handle_time`
    END,
    `closed_time` = CASE
      WHEN `closed_time` IS NOT NULL AND `handle_time` IS NOT NULL AND `closed_time` < `handle_time` THEN DATE_ADD(`handle_time`, INTERVAL 10 MINUTE)
      WHEN `closed_time` IS NOT NULL AND `closed_time` < `create_time` THEN DATE_ADD(`create_time`, INTERVAL 20 MINUTE)
      ELSE `closed_time`
    END,
    `update_by` = 'admin',
    `update_time` = NOW();

UPDATE `proj_alert_log` l
JOIN `proj_alert_record` a ON a.`alert_id` = l.`alert_id`
SET l.`action_time` = CASE
      WHEN l.`action_time` < a.`create_time` THEN DATE_ADD(a.`create_time`, INTERVAL 5 MINUTE)
      WHEN l.`action_time` > NOW() THEN DATE_SUB(NOW(), INTERVAL 5 MINUTE)
      ELSE l.`action_time`
    END;

DROP TEMPORARY TABLE IF EXISTS `_reimburse_date_fix`;
DROP TEMPORARY TABLE IF EXISTS `_work_date_fix`;
DROP TEMPORARY TABLE IF EXISTS `_project_date_map`;
