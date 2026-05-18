package com.ruoyi.system.domain.vo.project;

import java.math.BigDecimal;

public class CostDashboardProjectVO
{
    private Long projId;
    private String projNo;
    private String projName;
    private String status;
    private BigDecimal contractAmount;
    private BigDecimal totalBudget;
    private BigDecimal actualCost;
    private BigDecimal budgetBalance;
    private BigDecimal profitMargin;
    private BigDecimal monthPostingAmount;
    private BigDecimal execRate;

    public Long getProjId() { return projId; }
    public void setProjId(Long projId) { this.projId = projId; }
    public String getProjNo() { return projNo; }
    public void setProjNo(String projNo) { this.projNo = projNo; }
    public String getProjName() { return projName; }
    public void setProjName(String projName) { this.projName = projName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(BigDecimal contractAmount) { this.contractAmount = contractAmount; }
    public BigDecimal getTotalBudget() { return totalBudget; }
    public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }
    public BigDecimal getBudgetBalance() { return budgetBalance; }
    public void setBudgetBalance(BigDecimal budgetBalance) { this.budgetBalance = budgetBalance; }
    public BigDecimal getProfitMargin() { return profitMargin; }
    public void setProfitMargin(BigDecimal profitMargin) { this.profitMargin = profitMargin; }
    public BigDecimal getMonthPostingAmount() { return monthPostingAmount; }
    public void setMonthPostingAmount(BigDecimal monthPostingAmount) { this.monthPostingAmount = monthPostingAmount; }
    public BigDecimal getExecRate() { return execRate; }
    public void setExecRate(BigDecimal execRate) { this.execRate = execRate; }
}
