package com.ruoyi.system.service.project.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.project.ProjAlertRule;
import com.ruoyi.system.mapper.project.ProjAlertRuleMapper;
import com.ruoyi.system.service.project.IProjAlertRuleService;

@Service
public class ProjAlertRuleServiceImpl implements IProjAlertRuleService
{
    private static final BigDecimal MAX_THRESHOLD = new BigDecimal("100000000");

    @Autowired
    private ProjAlertRuleMapper alertRuleMapper;

    public ProjAlertRule selectAlertRuleById(Long ruleId)
    {
        return alertRuleMapper.selectAlertRuleById(ruleId);
    }

    public List<ProjAlertRule> selectAlertRuleList(ProjAlertRule rule)
    {
        return alertRuleMapper.selectAlertRuleList(rule);
    }

    public int insertAlertRule(ProjAlertRule rule, String username)
    {
        normalizeAndValidate(rule);
        rule.setCreateBy(username);
        return alertRuleMapper.insertAlertRule(rule);
    }

    public int updateAlertRule(ProjAlertRule rule, String username)
    {
        if (rule.getRuleId() == null)
        {
            throw new ServiceException("规则ID不能为空");
        }
        normalizeAndValidate(rule);
        rule.setUpdateBy(username);
        return alertRuleMapper.updateAlertRule(rule);
    }

    public int toggleAlertRule(Long ruleId, String enabled, String username)
    {
        if (ruleId == null)
        {
            throw new ServiceException("规则ID不能为空");
        }
        if (!"0".equals(enabled) && !"1".equals(enabled))
        {
            throw new ServiceException("启用状态不合法");
        }
        int rows = alertRuleMapper.updateAlertRuleEnabled(ruleId, enabled, username);
        if (rows == 0)
        {
            throw new ServiceException("规则不存在或已被删除");
        }
        return rows;
    }

    public int deleteAlertRuleByIds(Long[] ruleIds)
    {
        return alertRuleMapper.deleteAlertRuleByIds(ruleIds);
    }

    private void normalizeAndValidate(ProjAlertRule rule)
    {
        if (StringUtils.isEmpty(rule.getRuleName()))
        {
            throw new ServiceException("规则名称不能为空");
        }
        if (StringUtils.isEmpty(rule.getRuleType()))
        {
            throw new ServiceException("规则类型不能为空");
        }
        if (!isOneOf(rule.getRuleType(), "EXEC_RATE", "SINGLE_AMOUNT", "BALANCE_RATE", "OVERDUE", "INACTIVE"))
        {
            throw new ServiceException("规则类型不合法");
        }
        if (StringUtils.isEmpty(rule.getAlertLevel()))
        {
            throw new ServiceException("预警级别不能为空");
        }
        if (!isOneOf(rule.getAlertLevel(), "1", "2", "3"))
        {
            throw new ServiceException("预警级别不合法");
        }
        if (rule.getThresholdValue() == null)
        {
            throw new ServiceException("阈值不能为空");
        }
        if (rule.getThresholdValue().compareTo(BigDecimal.ZERO) < 0 || rule.getThresholdValue().compareTo(MAX_THRESHOLD) > 0)
        {
            throw new ServiceException("阈值范围不合法");
        }
        if (StringUtils.isEmpty(rule.getCompareOperator()))
        {
            rule.setCompareOperator(">=");
        }
        if (!isOneOf(rule.getCompareOperator(), ">", ">=", "<", "<=", "=", "=="))
        {
            throw new ServiceException("比较符不合法");
        }
        if (StringUtils.isEmpty(rule.getScopeType()))
        {
            rule.setScopeType("0");
        }
        if (!isOneOf(rule.getScopeType(), "0", "1", "2"))
        {
            throw new ServiceException("适用范围不合法");
        }
        if (StringUtils.isEmpty(rule.getNotifyEnabled()))
        {
            rule.setNotifyEnabled("1");
        }
        if (!isOneOf(rule.getNotifyEnabled(), "0", "1"))
        {
            throw new ServiceException("通知状态不合法");
        }
        if (StringUtils.isEmpty(rule.getNotifyChannels()))
        {
            rule.setNotifyChannels("SYSTEM");
        }
        if (rule.getNotifySilenceHours() == null)
        {
            rule.setNotifySilenceHours(24);
        }
        if (rule.getNotifySilenceHours() < 0 || rule.getNotifySilenceHours() > 720)
        {
            throw new ServiceException("静默小时范围不合法");
        }
        if (StringUtils.isEmpty(rule.getEnabled()))
        {
            rule.setEnabled("1");
        }
        if (!isOneOf(rule.getEnabled(), "0", "1"))
        {
            throw new ServiceException("启用状态不合法");
        }
    }

    private boolean isOneOf(String value, String... options)
    {
        for (String option : options)
        {
            if (option.equals(value))
            {
                return true;
            }
        }
        return false;
    }
}
