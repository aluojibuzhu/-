package com.ruoyi.system.service.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjAlertRule;

public interface IProjAlertRuleService
{
    ProjAlertRule selectAlertRuleById(Long ruleId);
    List<ProjAlertRule> selectAlertRuleList(ProjAlertRule rule);
    int insertAlertRule(ProjAlertRule rule, String username);
    int updateAlertRule(ProjAlertRule rule, String username);
    int toggleAlertRule(Long ruleId, String enabled, String username);
    int deleteAlertRuleByIds(Long[] ruleIds);
}
