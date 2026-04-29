package com.ruoyi.system.mapper.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjCostAllocation;

public interface ProjCostAllocationMapper
{
    List<ProjCostAllocation> selectByProjId(Long projId);
    int insertProjCostAllocation(ProjCostAllocation allocation);
    int deleteByProjId(Long projId);
}

