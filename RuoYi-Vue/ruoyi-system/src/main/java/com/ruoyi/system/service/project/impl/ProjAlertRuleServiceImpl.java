package com.ruoyi.system.service.project.impl;

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
        if (!"0".equals(enabled) && !"1".equals(enabled))
        {
            throw new ServiceException("启用状态不合法");
        }
        return alertRuleMapper.updateAlertRuleEnabled(ruleId, enabled, username);
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
        if (StringUtils.isEmpty(rule.getAlertLevel()))
        {
            throw new ServiceException("预警级别不能为空");
        }
        if (rule.getThresholdValue() == null)
        {
            throw new ServiceException("阈值不能为空");
        }
        if (StringUtils.isEmpty(rule.getCompareOperator()))
        {
            rule.setCompareOperator(">=");
        }
        if (StringUtils.isEmpty(rule.getScopeType()))
        {
            rule.setScopeType("0");
        }
        if (StringUtils.isEmpty(rule.getNotifyEnabled()))
        {
            rule.setNotifyEnabled("1");
        }
        if (StringUtils.isEmpty(rule.getNotifyChannels()))
        {
            rule.setNotifyChannels("SYSTEM");
        }
        if (rule.getNotifySilenceHours() == null)
        {
            rule.setNotifySilenceHours(24);
        }
        if (StringUtils.isEmpty(rule.getEnabled()))
        {
            rule.setEnabled("1");
        }
    }
}
