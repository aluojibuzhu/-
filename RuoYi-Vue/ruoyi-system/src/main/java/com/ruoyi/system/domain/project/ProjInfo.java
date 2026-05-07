package com.ruoyi.system.domain.project;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class ProjInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long projId;
    private String projNo;
    private String projName;
    private Long customerId;
    private String customerName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planEndDate;
    private BigDecimal contractAmount;
    private String projDesc;
    private BigDecimal totalBudget;
    private BigDecimal actualCost;
    private BigDecimal budgetBalance;
    private String status;
    private String rejectReason;
    public Long getProjId() { return projId; }
    public void setProjId(Long projId) { this.projId = projId; }
    public String getProjNo() { return projNo; }
    public void setProjNo(String projNo) { this.projNo = projNo; }
    public String getProjName() { return projName; }
    public void setProjName(String projName) { this.projName = projName; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Date getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(Date planStartDate) { this.planStartDate = planStartDate; }
    public Date getPlanEndDate() { return planEndDate; }
    public void setPlanEndDate(Date planEndDate) { this.planEndDate = planEndDate; }
    public BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(BigDecimal contractAmount) { this.contractAmount = contractAmount; }
    public String getProjDesc() { return projDesc; }
    public void setProjDesc(String projDesc) { this.projDesc = projDesc; }
    public BigDecimal getTotalBudget() { return totalBudget; }
    public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }
    public BigDecimal getBudgetBalance() { return budgetBalance; }
    public void setBudgetBalance(BigDecimal budgetBalance) { this.budgetBalance = budgetBalance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
