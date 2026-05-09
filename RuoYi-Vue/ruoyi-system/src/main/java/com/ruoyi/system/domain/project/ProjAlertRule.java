package com.ruoyi.system.domain.project;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

public class ProjAlertRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long ruleId;
    private String ruleName;
    private String ruleType;
    private String alertLevel;
    private BigDecimal thresholdValue;
    private String compareOperator;
    private String scopeType;
    private String scopeValue;
    private String notifyEnabled;
    private String notifyChannels;
    private String notifyRoles;
    private Integer notifySilenceHours;
    private String enabled;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }
    public String getCompareOperator() { return compareOperator; }
    public void setCompareOperator(String compareOperator) { this.compareOperator = compareOperator; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public String getScopeValue() { return scopeValue; }
    public void setScopeValue(String scopeValue) { this.scopeValue = scopeValue; }
    public String getNotifyEnabled() { return notifyEnabled; }
    public void setNotifyEnabled(String notifyEnabled) { this.notifyEnabled = notifyEnabled; }
    public String getNotifyChannels() { return notifyChannels; }
    public void setNotifyChannels(String notifyChannels) { this.notifyChannels = notifyChannels; }
    public String getNotifyRoles() { return notifyRoles; }
    public void setNotifyRoles(String notifyRoles) { this.notifyRoles = notifyRoles; }
    public Integer getNotifySilenceHours() { return notifySilenceHours; }
    public void setNotifySilenceHours(Integer notifySilenceHours) { this.notifySilenceHours = notifySilenceHours; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
}
