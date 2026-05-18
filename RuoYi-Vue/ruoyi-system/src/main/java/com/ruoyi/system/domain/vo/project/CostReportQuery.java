package com.ruoyi.system.domain.vo.project;

import java.util.List;

public class CostReportQuery
{
    private String beginDate;
    private String endDate;
    private List<Long> projIds;
    private List<Long> categoryIds;
    private List<Long> nodeIds;
    private List<String> approvers;
    private List<String> groupBy;
    private String billType;

    public String getBeginDate() { return beginDate; }
    public void setBeginDate(String beginDate) { this.beginDate = beginDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public List<Long> getProjIds() { return projIds; }
    public void setProjIds(List<Long> projIds) { this.projIds = projIds; }
    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
    public List<Long> getNodeIds() { return nodeIds; }
    public void setNodeIds(List<Long> nodeIds) { this.nodeIds = nodeIds; }
    public List<String> getApprovers() { return approvers; }
    public void setApprovers(List<String> approvers) { this.approvers = approvers; }
    public List<String> getGroupBy() { return groupBy; }
    public void setGroupBy(List<String> groupBy) { this.groupBy = groupBy; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
}
