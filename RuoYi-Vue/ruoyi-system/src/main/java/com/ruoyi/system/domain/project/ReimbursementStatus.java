package com.ruoyi.system.domain.project;

import com.ruoyi.common.exception.ServiceException;

public enum ReimbursementStatus
{
    DRAFT("0", "草稿"),
    PENDING("1", "审批中"),
    APPROVED("2", "已通过"),
    REJECTED("3", "已驳回"),
    POSTED("4", "已入账");

    private final String code;
    private final String label;

    ReimbursementStatus(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String code() { return code; }
    public String label() { return label; }

    public static void require(Reimbursement reimbursement, ReimbursementStatus... allowed)
    {
        if (reimbursement == null)
        {
            throw new ServiceException("报销单不存在");
        }
        for (ReimbursementStatus status : allowed)
        {
            if (status.code.equals(reimbursement.getStatus()))
            {
                return;
            }
        }
        throw new ServiceException("当前报销单状态不允许执行该操作");
    }
}
