package com.ruoyi.system.domain.vo.project;

import java.math.BigDecimal;

public class CostApprovalSummaryVO
{
    private Integer pendingCount;
    private Integer approvedCount;
    private Integer postedCount;
    private BigDecimal pendingAmount;

    public Integer getPendingCount() { return pendingCount; }
    public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
    public Integer getApprovedCount() { return approvedCount; }
    public void setApprovedCount(Integer approvedCount) { this.approvedCount = approvedCount; }
    public Integer getPostedCount() { return postedCount; }
    public void setPostedCount(Integer postedCount) { this.postedCount = postedCount; }
    public BigDecimal getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }
}
