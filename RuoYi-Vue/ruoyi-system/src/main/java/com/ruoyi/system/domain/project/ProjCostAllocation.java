package com.ruoyi.system.domain.project;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

public class ProjCostAllocation extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long allocationId;
    private Long projId;
    private Long nodeId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal allocationAmount;
    private BigDecimal actualCost;
    public Long getAllocationId() { return allocationId; }
    public void setAllocationId(Long allocationId) { this.allocationId = allocationId; }
    public Long getProjId() { return projId; }
    public void setProjId(Long projId) { this.projId = projId; }
    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeId) { this.nodeId = nodeId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getAllocationAmount() { return allocationAmount; }
    public void setAllocationAmount(BigDecimal allocationAmount) { this.allocationAmount = allocationAmount; }
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(BigDecimal actualCost) { this.actualCost = actualCost; }
}
