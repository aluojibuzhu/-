package com.ruoyi.system.service.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjWbsNode;
import com.ruoyi.system.domain.project.WorkHour;
import com.ruoyi.system.domain.vo.project.WorkHourFormVO;

public interface IWorkHourService
{
    List<WorkHour> selectWorkHourList(WorkHour workHour);

    WorkHourFormVO getWorkHourForm(Long whId);

    List<ProjWbsNode> selectWbsNodesByProjId(Long projId);

    WorkHour saveDraft(WorkHourFormVO form, String username);

    int submitForApproval(Long whId, String username);

    int approve(Long whId, String username);

    int reject(Long whId, String reason, String username);

    int postCost(Long whId, String username);

    int deleteWorkHourById(Long whId);
}
