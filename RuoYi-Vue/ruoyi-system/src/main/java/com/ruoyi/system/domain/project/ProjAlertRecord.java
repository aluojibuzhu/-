package com.ruoyi.system.domain.project;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class ProjAlertRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long alertId;
    private String alertNo;
    private Long ruleId;
    private String ruleName;
    private String ruleType;
    private String alertLevel;
    private String alertType;
    private Long projId;
    private String projNo;
    private String projName;
    private Long nodeId;
    private String nodeName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalBudget;
    private BigDecimal actualCost;
    private BigDecimal budgetBalance;
    private BigDecimal currentValue;
    private BigDecimal thresholdValue;
    private String triggerBillType;
    private Long triggerBillId;
    private String triggerBillNo;
    private BigDecimal triggerAmount;
    private String conditionDesc;
    private String status;
    private String handler;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;
    private String handleRemark;
    private String closedBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closedTime;
    private String closeRemark;
    private String beginTime;
    private String endTime;

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public String getAlertNo() { return alertNo; }
    public void setAlertNo(String alertNo) { this.alertNo = alertNo; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public Long getProjId() { return projId; }
    public void setProjId(Long projId) { this.projId = projId; }
    public String getProjNo() { return projNo; }
    public void setProjNo(String projNo) { this.projNo = projNo; }
    public String getProjName() { return projName; }
    public void setProjName(String projName) { this.projName = projName; }
    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeId) { this.nodeId = nodeId; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getTotalBudget() { return totalBudget; }
    public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }
    public BigDecimal getBudgetBalance() { return budgetBalance; }
    public void setBudgetBalance(BigDecimal budgetBalance) { this.budgetBalance = budgetBalance; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }
    public String getTriggerBillType() { return triggerBillType; }
    public void setTriggerBillType(String triggerBillType) { this.triggerBillType = triggerBillType; }
    public Long getTriggerBillId() { return triggerBillId; }
    public void setTriggerBillId(Long triggerBillId) { this.triggerBillId = triggerBillId; }
    public String getTriggerBillNo() { return triggerBillNo; }
    public void setTriggerBillNo(String triggerBillNo) { this.triggerBillNo = triggerBillNo; }
    public BigDecimal getTriggerAmount() { return triggerAmount; }
    public void setTriggerAmount(BigDecimal triggerAmount) { this.triggerAmount = triggerAmount; }
    public String getConditionDesc() { return conditionDesc; }
    public void setConditionDesc(String conditionDesc) { this.conditionDesc = conditionDesc; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
    public Date getHandleTime() { return handleTime; }
    public void setHandleTime(Date handleTime) { this.handleTime = handleTime; }
    public String getHandleRemark() { return handleRemark; }
    public void setHandleRemark(String handleRemark) { this.handleRemark = handleRemark; }
    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }
    public Date getClosedTime() { return closedTime; }
    public void setClosedTime(Date closedTime) { this.closedTime = closedTime; }
    public String getCloseRemark() { return closeRemark; }
    public void setCloseRemark(String closeRemark) { this.closeRemark = closeRemark; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
