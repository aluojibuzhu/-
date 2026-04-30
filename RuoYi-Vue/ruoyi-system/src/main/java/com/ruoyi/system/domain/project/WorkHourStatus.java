package com.ruoyi.system.domain.project;

import com.ruoyi.common.exception.ServiceException;

public enum WorkHourStatus
{
    DRAFT("0", "草稿"),
    PENDING("1", "审批中"),
    APPROVED("2", "已通过"),
    REJECTED("3", "已驳回"),
    POSTED("4", "已入账");

    private final String code;
    private final String label;

    WorkHourStatus(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String code() { return code; }
    public String label() { return label; }

    public static void require(WorkHour workHour, WorkHourStatus... allowed)
    {
        if (workHour == null)
        {
            throw new ServiceException("工时单不存在或已删除");
        }
        for (WorkHourStatus status : allowed)
        {
            if (status.code.equals(workHour.getStatus()))
            {
                return;
            }
        }
        throw new ServiceException("当前工时单状态不允许执行该操作");
    }
}
