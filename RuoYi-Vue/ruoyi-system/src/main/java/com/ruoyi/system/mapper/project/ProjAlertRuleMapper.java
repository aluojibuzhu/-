package com.ruoyi.system.mapper.project;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.project.ProjAlertRule;

public interface ProjAlertRuleMapper
{
    ProjAlertRule selectAlertRuleById(Long ruleId);
    List<ProjAlertRule> selectAlertRuleList(ProjAlertRule rule);
    List<ProjAlertRule> selectEnabledAlertRules();
    int insertAlertRule(ProjAlertRule rule);
    int updateAlertRule(ProjAlertRule rule);
    int updateAlertRuleEnabled(@Param("ruleId") Long ruleId, @Param("enabled") String enabled, @Param("username") String username);
    int deleteAlertRuleByIds(Long[] ruleIds);
}
