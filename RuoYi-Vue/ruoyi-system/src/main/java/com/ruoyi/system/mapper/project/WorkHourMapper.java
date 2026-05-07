package com.ruoyi.system.mapper.project;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.project.WorkHour;
import com.ruoyi.system.domain.project.WorkHourSequence;

public interface WorkHourMapper
{
    List<WorkHour> selectWorkHourList(WorkHour workHour);

    WorkHour selectWorkHourById(Long whId);

    int insertWorkHour(WorkHour workHour);

    int updateWorkHour(WorkHour workHour);

    int updateWorkHourIfStatus(@Param("workHour") WorkHour workHour, @Param("expectedStatus") String expectedStatus);

    int deleteWorkHourById(Long whId);

    int insertWorkHourSequence(WorkHourSequence sequence);
}
