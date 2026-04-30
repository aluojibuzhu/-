package com.ruoyi.system.domain.project;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class Reimbursement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long reimburseId;
    private String reimburseNo;
    private Long projId;
    private String projName;
    private Long nodeId;
    private String nodeName;
    private Long categoryId;
    private String categoryName;
    private String expenseType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expenseDate;
    private BigDecimal amount;
    private String expenseDesc;
    private Integer invoiceCount;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;
    private String approveBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;
    private String rejectReason;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date beginExpenseDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endExpenseDate;

    public Long getReimburseId() { return reimburseId; }
    public void setReimburseId(Long reimburseId) { this.reimburseId = reimburseId; }
    public String getReimburseNo() { return reimburseNo; }
    public void setReimburseNo(String reimburseNo) { this.reimburseNo = reimburseNo; }
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
    public String getExpenseType() { return expenseType; }
    public void setExpenseType(String expenseType) { this.expenseType = expenseType; }
    public Date getExpenseDate() { return expenseDate; }
    public void setExpenseDate(Date expenseDate) { this.expenseDate = expenseDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getExpenseDesc() { return expenseDesc; }
    public void setExpenseDesc(String expenseDesc) { this.expenseDesc = expenseDesc; }
    public Integer getInvoiceCount() { return invoiceCount; }
    public void setInvoiceCount(Integer invoiceCount) { this.invoiceCount = invoiceCount; }
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
    public Date getBeginExpenseDate() { return beginExpenseDate; }
    public void setBeginExpenseDate(Date beginExpenseDate) { this.beginExpenseDate = beginExpenseDate; }
    public Date getEndExpenseDate() { return endExpenseDate; }
    public void setEndExpenseDate(Date endExpenseDate) { this.endExpenseDate = endExpenseDate; }
}
