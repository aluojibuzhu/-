package com.ruoyi.system.domain.project;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class ProjAlertLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long alertId;
    private String actionType;
    private String actionBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actionTime;
    private String actionRemark;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getActionBy() { return actionBy; }
    public void setActionBy(String actionBy) { this.actionBy = actionBy; }
    public Date getActionTime() { return actionTime; }
    public void setActionTime(Date actionTime) { this.actionTime = actionTime; }
    public String getActionRemark() { return actionRemark; }
    public void setActionRemark(String actionRemark) { this.actionRemark = actionRemark; }
}
