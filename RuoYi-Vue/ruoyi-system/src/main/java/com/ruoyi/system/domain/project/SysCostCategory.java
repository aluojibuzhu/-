package com.ruoyi.system.domain.project;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

public class SysCostCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long categoryId;
    private Long parentId;
    private String categoryName;
    private Integer categoryLevel;
    private Integer orderNum;
    private String status;
    private BigDecimal unitPrice;
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getCategoryLevel() { return categoryLevel; }
    public void setCategoryLevel(Integer categoryLevel) { this.categoryLevel = categoryLevel; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
