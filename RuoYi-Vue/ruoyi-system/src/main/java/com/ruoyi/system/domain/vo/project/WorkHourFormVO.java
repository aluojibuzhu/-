package com.ruoyi.system.domain.vo.project;

import java.util.List;
import com.ruoyi.system.domain.project.WorkHour;
import com.ruoyi.system.domain.project.WorkHourAttachment;

public class WorkHourFormVO
{
    private WorkHour workHour;
    private List<WorkHourAttachment> attachments;

    public WorkHour getWorkHour() { return workHour; }
    public void setWorkHour(WorkHour workHour) { this.workHour = workHour; }
    public List<WorkHourAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<WorkHourAttachment> attachments) { this.attachments = attachments; }
}
