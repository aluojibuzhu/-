package com.ruoyi.system.domain.project;

import com.ruoyi.common.exception.ServiceException;

public enum ProjectStatus
{
    DRAFT("0", "草稿"),
    PENDING("1", "审批中"),
    APPROVED("2", "已立项"),
    REJECTED("3", "已驳回"),
    IN_PROGRESS("4", "进行中"),
    COMPLETED("5", "已完工");

    private final String code;
    private final String label;

    ProjectStatus(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String code()
    {
        return code;
    }

    public String label()
    {
        return label;
    }

    public static void require(ProjInfo info, ProjectStatus... allowed)
    {
        if (info == null)
        {
            throw new ServiceException("项目不存在或已删除");
        }
        for (ProjectStatus status : allowed)
        {
            if (status.code.equals(info.getStatus()))
            {
                return;
            }
        }
        throw new ServiceException("当前项目状态不允许执行该操作");
    }
}
