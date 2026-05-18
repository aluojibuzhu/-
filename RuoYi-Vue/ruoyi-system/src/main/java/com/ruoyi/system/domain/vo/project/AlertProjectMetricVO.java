package com.ruoyi.system.domain.vo.project;

import java.math.BigDecimal;
import java.util.Date;

public class AlertProjectMetricVO
{
    private Long projId;
    private String projNo;
    private String projName;
    private Date planStartDate;
    private Date planEndDate;
    private BigDecimal totalBudget;
    private BigDecimal actualCost;
    private BigDecimal execRate;
    private Integer activeAlertCount;
    private Integer redCount;
    private Integer orangeCount;
    private Integer yellowCount;

    public Long getProjId() { return projId; }
    public void setProjId(Long projId) { this.projId = projId; }
    public String getProjNo() { return projNo; }
    public void setProjNo(String projNo) { this.projNo = projNo; }
    public String getProjName() { return projName; }
    public void setProjName(String projName) { this.projName = projName; }
    public Date getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(Date planStartDate) { this.planStartDate = planStartDate; }
    public Date getPlanEndDate() { return planEndDate; }
    public void setPlanEndDate(Date planEndDate) { this.planEndDate = planEndDate; }
    public BigDecimal getTotalBudget() { return totalBudget; }
    public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }
    public BigDecimal getExecRate() { return execRate; }
    public void setExecRate(BigDecimal execRate) { this.execRate = execRate; }
    public Integer getActiveAlertCount() { return activeAlertCount; }
    public void setActiveAlertCount(Integer activeAlertCount) { this.activeAlertCount = activeAlertCount; }
    public Integer getRedCount() { return redCount; }
    public void setRedCount(Integer redCount) { this.redCount = redCount; }
    public Integer getOrangeCount() { return orangeCount; }
    public void setOrangeCount(Integer orangeCount) { this.orangeCount = orangeCount; }
    public Integer getYellowCount() { return yellowCount; }
    public void setYellowCount(Integer yellowCount) { this.yellowCount = yellowCount; }
}
