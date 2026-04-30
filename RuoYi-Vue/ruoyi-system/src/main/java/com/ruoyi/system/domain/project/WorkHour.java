package com.ruoyi.system.domain.project;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class WorkHour extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long whId;
    private String whNo;
    private Long projId;
    private String projName;
    private Long nodeId;
    private String nodeName;
    private Long categoryId;
    private String categoryName;
    private String workType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date workDate;
    private BigDecimal workHours;
    private BigDecimal unitPrice;
    private BigDecimal workCost;
    private String workDesc;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;
    private String approveBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;
    private String rejectReason;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date beginWorkDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endWorkDate;

    public Long getWhId() { return whId; }
    public void setWhId(Long whId) { this.whId = whId; }
    public String getWhNo() { return whNo; }
    public void setWhNo(String whNo) { this.whNo = whNo; }
    public Long getProjId() { return projId; }
    public void setProjId(Long projId) { this.projId = projId; }
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
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }
    public BigDecimal getWorkHours() { return workHours; }
    public void setWorkHours(BigDecimal workHours) { this.workHours = workHours; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getWorkCost() { return workCost; }
    public void setWorkCost(BigDecimal workCost) { this.workCost = workCost; }
    public String getWorkDesc() { return workDesc; }
    public void setWorkDesc(String workDesc) { this.workDesc = workDesc; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public Date getBeginWorkDate() { return beginWorkDate; }
    public void setBeginWorkDate(Date beginWorkDate) { this.beginWorkDate = beginWorkDate; }
    public Date getEndWorkDate() { return endWorkDate; }
    public void setEndWorkDate(Date endWorkDate) { this.endWorkDate = endWorkDate; }
}
