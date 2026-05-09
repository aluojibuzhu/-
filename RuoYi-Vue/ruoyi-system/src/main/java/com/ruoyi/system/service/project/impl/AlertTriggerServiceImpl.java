package com.ruoyi.system.service.project.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.project.CostPostingRecord;
import com.ruoyi.system.domain.project.ProjAlertRecord;
import com.ruoyi.system.domain.project.ProjAlertRule;
import com.ruoyi.system.domain.project.ProjAlertSequence;
import com.ruoyi.system.domain.project.ProjInfo;
import com.ruoyi.system.mapper.project.ProjAlertRecordMapper;
import com.ruoyi.system.mapper.project.ProjAlertRuleMapper;
import com.ruoyi.system.mapper.project.ProjInfoMapper;
import com.ruoyi.system.mapper.project.CostApprovalMapper;
import com.ruoyi.system.service.project.IAlertTriggerService;
import com.ruoyi.system.service.project.IProjAlertLogService;

@Service
public class AlertTriggerServiceImpl implements IAlertTriggerService
{
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Autowired
    private ProjAlertRuleMapper alertRuleMapper;
    @Autowired
    private ProjAlertRecordMapper alertRecordMapper;
    @Autowired
    private ProjInfoMapper projInfoMapper;
    @Autowired
    private CostApprovalMapper costApprovalMapper;
    @Autowired
    private IProjAlertLogService alertLogService;

    @Transactional
    public void checkOnPosting(Long projId, CostPostingRecord posting)
    {
        if (projId == null)
        {
            return;
        }
        ProjInfo project = projInfoMapper.selectProjInfoById(projId);
        if (project == null)
        {
            return;
        }
        List<ProjAlertRule> rules = alertRuleMapper.selectEnabledAlertRules();
        for (ProjAlertRule rule : rules)
        {
            if (!matchesScope(rule, posting))
            {
                continue;
            }
            BigDecimal currentValue = calculateValue(rule.getRuleType(), project, posting);
            if (currentValue == null || !matchesThreshold(currentValue, rule))
            {
                continue;
            }
            Integer hours = rule.getNotifySilenceHours() == null ? 0 : rule.getNotifySilenceHours();
            if (hours > 0 && alertRecordMapper.countRecentAlert(rule.getRuleId(), projId, hours) > 0)
            {
                continue;
            }
            ProjAlertRecord alert = buildAlert(rule, project, posting, currentValue);
            alertRecordMapper.insertAlertRecord(alert);
            alertLogService.insertAlertLog(alert.getAlertId(), "TRIGGER", posting.getPostBy(), alert.getConditionDesc());
            if ("1".equals(rule.getNotifyEnabled()))
            {
                alertLogService.insertAlertLog(alert.getAlertId(), "NOTIFY", "system", "已生成系统预警");
            }
        }
    }

    @Transactional
    public void checkDailyRules()
    {
        List<ProjAlertRule> rules = alertRuleMapper.selectEnabledAlertRules();
        List<ProjInfo> projects = projInfoMapper.selectProjInfoList(new ProjInfo());
        for (ProjAlertRule rule : rules)
        {
            if (!"OVERDUE".equals(rule.getRuleType()) && !"INACTIVE".equals(rule.getRuleType()))
            {
                continue;
            }
            for (ProjInfo project : projects)
            {
                if (!matchesProjectScope(rule, project.getProjId()))
                {
                    continue;
                }
                BigDecimal currentValue = calculateDailyValue(rule.getRuleType(), project);
                if (currentValue == null || !matchesThreshold(currentValue, rule))
                {
                    continue;
                }
                Integer hours = rule.getNotifySilenceHours() == null ? 0 : rule.getNotifySilenceHours();
                if (hours > 0 && alertRecordMapper.countRecentAlert(rule.getRuleId(), project.getProjId(), hours) > 0)
                {
                    continue;
                }
                ProjAlertRecord alert = buildDailyAlert(rule, project, currentValue);
                alertRecordMapper.insertAlertRecord(alert);
                alertLogService.insertAlertLog(alert.getAlertId(), "TRIGGER", "system", alert.getConditionDesc());
                if ("1".equals(rule.getNotifyEnabled()))
                {
                    alertLogService.insertAlertLog(alert.getAlertId(), "NOTIFY", "system", "已生成系统预警");
                }
            }
        }
    }

    private ProjAlertRecord buildAlert(ProjAlertRule rule, ProjInfo project, CostPostingRecord posting, BigDecimal currentValue)
    {
        ProjAlertRecord alert = new ProjAlertRecord();
        alert.setAlertNo(nextAlertNo());
        alert.setRuleId(rule.getRuleId());
        alert.setRuleName(rule.getRuleName());
        alert.setRuleType(rule.getRuleType());
        alert.setAlertLevel(rule.getAlertLevel());
        alert.setAlertType("SINGLE_AMOUNT".equals(rule.getRuleType()) ? "CATEGORY" : "PROJECT");
        alert.setProjId(project.getProjId());
        alert.setProjNo(project.getProjNo());
        alert.setProjName(project.getProjName());
        alert.setNodeId(posting.getNodeId());
        alert.setNodeName(posting.getNodeName());
        alert.setCategoryId(posting.getCategoryId());
        alert.setCategoryName(posting.getCategoryName());
        alert.setTotalBudget(nvl(project.getTotalBudget()));
        alert.setActualCost(nvl(project.getActualCost()));
        alert.setBudgetBalance(nvl(project.getBudgetBalance()));
        alert.setCurrentValue(currentValue);
        alert.setThresholdValue(rule.getThresholdValue());
        alert.setTriggerBillType(posting.getBillType());
        alert.setTriggerBillId(posting.getBillId());
        alert.setTriggerBillNo(posting.getBillNo());
        alert.setTriggerAmount(posting.getAmount());
        alert.setStatus("0");
        alert.setCreateBy(posting.getPostBy());
        alert.setConditionDesc(buildConditionDesc(rule, currentValue));
        return alert;
    }

    private String nextAlertNo()
    {
        ProjAlertSequence sequence = new ProjAlertSequence();
        alertRecordMapper.insertAlertNoSequence(sequence);
        String date = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        return "AL-" + date + "-" + String.format("%03d", sequence.getId());
    }

    private ProjAlertRecord buildDailyAlert(ProjAlertRule rule, ProjInfo project, BigDecimal currentValue)
    {
        ProjAlertRecord alert = new ProjAlertRecord();
        alert.setAlertNo(nextAlertNo());
        alert.setRuleId(rule.getRuleId());
        alert.setRuleName(rule.getRuleName());
        alert.setRuleType(rule.getRuleType());
        alert.setAlertLevel(rule.getAlertLevel());
        alert.setAlertType("PROJECT");
        alert.setProjId(project.getProjId());
        alert.setProjNo(project.getProjNo());
        alert.setProjName(project.getProjName());
        alert.setTotalBudget(nvl(project.getTotalBudget()));
        alert.setActualCost(nvl(project.getActualCost()));
        alert.setBudgetBalance(nvl(project.getBudgetBalance()));
        alert.setCurrentValue(currentValue);
        alert.setThresholdValue(rule.getThresholdValue());
        alert.setStatus("0");
        alert.setCreateBy("system");
        alert.setConditionDesc(buildConditionDesc(rule, currentValue));
        return alert;
    }

    private BigDecimal calculateValue(String ruleType, ProjInfo project, CostPostingRecord posting)
    {
        if ("EXEC_RATE".equals(ruleType))
        {
            BigDecimal totalBudget = nvl(project.getTotalBudget());
            if (totalBudget.compareTo(BigDecimal.ZERO) <= 0)
            {
                return BigDecimal.ZERO;
            }
            return nvl(project.getActualCost()).multiply(HUNDRED).divide(totalBudget, 2, RoundingMode.HALF_UP);
        }
        if ("SINGLE_AMOUNT".equals(ruleType))
        {
            return nvl(posting.getAmount());
        }
        if ("BALANCE_RATE".equals(ruleType))
        {
            BigDecimal totalBudget = nvl(project.getTotalBudget());
            if (totalBudget.compareTo(BigDecimal.ZERO) <= 0)
            {
                return BigDecimal.ZERO;
            }
            return nvl(project.getBudgetBalance()).multiply(HUNDRED).divide(totalBudget, 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private BigDecimal calculateDailyValue(String ruleType, ProjInfo project)
    {
        Date now = new Date();
        if ("OVERDUE".equals(ruleType))
        {
            if ("5".equals(project.getStatus()) || project.getPlanEndDate() == null || !now.after(project.getPlanEndDate()))
            {
                return BigDecimal.ZERO;
            }
            long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - project.getPlanEndDate().getTime());
            return BigDecimal.valueOf(days);
        }
        if ("INACTIVE".equals(ruleType))
        {
            Date lastPosting = costApprovalMapper.selectLastPostingTime(project.getProjId());
            Date baseline = lastPosting != null ? lastPosting : project.getPlanStartDate();
            if (baseline == null)
            {
                return BigDecimal.ZERO;
            }
            long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - baseline.getTime());
            return BigDecimal.valueOf(days).divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private boolean matchesThreshold(BigDecimal currentValue, ProjAlertRule rule)
    {
        int result = currentValue.compareTo(rule.getThresholdValue());
        String operator = rule.getCompareOperator();
        if (">".equals(operator)) return result > 0;
        if (">=".equals(operator)) return result >= 0;
        if ("<".equals(operator)) return result < 0;
        if ("<=".equals(operator)) return result <= 0;
        if ("=".equals(operator) || "==".equals(operator)) return result == 0;
        return result >= 0;
    }

    private boolean matchesScope(ProjAlertRule rule, CostPostingRecord posting)
    {
        if ("0".equals(rule.getScopeType()) || StringUtils.isEmpty(rule.getScopeValue()))
        {
            return true;
        }
        if ("1".equals(rule.getScopeType()))
        {
            return containsId(rule.getScopeValue(), posting.getProjId());
        }
        if ("2".equals(rule.getScopeType()))
        {
            return containsId(rule.getScopeValue(), posting.getCategoryId());
        }
        return true;
    }

    private boolean matchesProjectScope(ProjAlertRule rule, Long projId)
    {
        if ("0".equals(rule.getScopeType()) || StringUtils.isEmpty(rule.getScopeValue()))
        {
            return true;
        }
        if ("1".equals(rule.getScopeType()))
        {
            return containsId(rule.getScopeValue(), projId);
        }
        return true;
    }

    private boolean containsId(String source, Long value)
    {
        if (value == null)
        {
            return false;
        }
        String target = String.valueOf(value);
        String[] parts = source.split(",");
        for (String part : parts)
        {
            if (target.equals(part.trim()))
            {
                return true;
            }
        }
        return false;
    }

    private String buildConditionDesc(ProjAlertRule rule, BigDecimal currentValue)
    {
        if ("EXEC_RATE".equals(rule.getRuleType()))
        {
            return "预算执行率 " + currentValue + "% " + rule.getCompareOperator() + " " + rule.getThresholdValue() + "%";
        }
        if ("BALANCE_RATE".equals(rule.getRuleType()))
        {
            return "预算余额率 " + currentValue + "% " + rule.getCompareOperator() + " " + rule.getThresholdValue() + "%";
        }
        if ("SINGLE_AMOUNT".equals(rule.getRuleType()))
        {
            return "单笔入账金额 " + currentValue + " " + rule.getCompareOperator() + " " + rule.getThresholdValue();
        }
        if ("OVERDUE".equals(rule.getRuleType()))
        {
            return "项目逾期天数 " + currentValue + " " + rule.getCompareOperator() + " " + rule.getThresholdValue();
        }
        if ("INACTIVE".equals(rule.getRuleType()))
        {
            return "连续无入账月数 " + currentValue + " " + rule.getCompareOperator() + " " + rule.getThresholdValue();
        }
        return rule.getRuleName();
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}
