package com.ruoyi.system.mapper.project;

import java.util.List;
import com.ruoyi.system.domain.project.WorkHour;
import com.ruoyi.system.domain.project.WorkHourSequence;

public interface WorkHourMapper
{
    List<WorkHour> selectWorkHourList(WorkHour workHour);

    WorkHour selectWorkHourById(Long whId);

    int insertWorkHour(WorkHour workHour);

    int updateWorkHour(WorkHour workHour);

    int deleteWorkHourById(Long whId);

    int insertWorkHourSequence(WorkHourSequence sequence);
}
