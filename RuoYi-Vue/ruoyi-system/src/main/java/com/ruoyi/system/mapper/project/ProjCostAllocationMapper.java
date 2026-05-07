package com.ruoyi.system.mapper.project;

import java.util.List;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.project.ProjCostAllocation;

public interface ProjCostAllocationMapper
{
    List<ProjCostAllocation> selectByProjId(Long projId);
    int insertProjCostAllocation(ProjCostAllocation allocation);
    int deleteByProjId(Long projId);
    int increaseActualCost(@Param("projId") Long projId, @Param("nodeId") Long nodeId, @Param("categoryId") Long categoryId, @Param("amount") BigDecimal amount, @Param("username") String username);
}
