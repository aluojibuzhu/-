package com.ruoyi.system.domain.vo.project;

import java.math.BigDecimal;

public class CostDashboardSummaryVO
{
    private Long projectCount;
    private BigDecimal totalBudget;
    private BigDecimal actualCost;
    private BigDecimal budgetBalance;
    private BigDecimal contractAmount;
    private BigDecimal profitMargin;
    private BigDecimal monthPostingAmount;
    private BigDecimal lastMonthPostingAmount;
    private BigDecimal momRate;

    public Long getProjectCount() { return projectCount; }
    public void setProjectCount(Long projectCount) { this.projectCount = projectCount; }
    public BigDecimal getTotalBudget() { return totalBudget; }
    public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }
    public BigDecimal getBudgetBalance() { return budgetBalance; }
    public void setBudgetBalance(BigDecimal budgetBalance) { this.budgetBalance = budgetBalance; }
    public BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(BigDecimal contractAmount) { this.contractAmount = contractAmount; }
    public BigDecimal getProfitMargin() { return profitMargin; }
    public void setProfitMargin(BigDecimal profitMargin) { this.profitMargin = profitMargin; }
    public BigDecimal getMonthPostingAmount() { return monthPostingAmount; }
    public void setMonthPostingAmount(BigDecimal monthPostingAmount) { this.monthPostingAmount = monthPostingAmount; }
    public BigDecimal getLastMonthPostingAmount() { return lastMonthPostingAmount; }
    public void setLastMonthPostingAmount(BigDecimal lastMonthPostingAmount) { this.lastMonthPostingAmount = lastMonthPostingAmount; }
    public BigDecimal getMomRate() { return momRate; }
    public void setMomRate(BigDecimal momRate) { this.momRate = momRate; }
}
