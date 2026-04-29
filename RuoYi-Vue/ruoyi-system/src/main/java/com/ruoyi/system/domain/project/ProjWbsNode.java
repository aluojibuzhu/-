package com.ruoyi.system.domain.project;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class ProjWbsNode extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long nodeId;
    private Long projId;
    private String nodeNo;
    private String nodeName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planFinishDate;
    private BigDecimal nodeBudget;
    private Integer orderNum;
    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeId) { this.nodeId = nodeId; }
    public Long getProjId() { return projId; }
    public void setProjId(Long projId) { this.projId = projId; }
    public String getNodeNo() { return nodeNo; }
    public void setNodeNo(String nodeNo) { this.nodeNo = nodeNo; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public Date getPlanFinishDate() { return planFinishDate; }
    public void setPlanFinishDate(Date planFinishDate) { this.planFinishDate = planFinishDate; }
    public BigDecimal getNodeBudget() { return nodeBudget; }
    public void setNodeBudget(BigDecimal nodeBudget) { this.nodeBudget = nodeBudget; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
}

