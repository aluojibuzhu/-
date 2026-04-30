package com.ruoyi.system.mapper.project;

import java.util.List;
import com.ruoyi.system.domain.project.WorkHourAttachment;

public interface WorkHourAttachmentMapper
{
    List<WorkHourAttachment> selectByWhId(Long whId);

    int insertWorkHourAttachment(WorkHourAttachment attachment);

    int deleteByWhId(Long whId);
}
