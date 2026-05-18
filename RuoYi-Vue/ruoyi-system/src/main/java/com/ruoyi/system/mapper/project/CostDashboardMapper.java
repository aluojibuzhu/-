package com.ruoyi.system.mapper.project;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.vo.project.CostDashboardProjectVO;
import com.ruoyi.system.domain.vo.project.CostDashboardSummaryVO;

public interface CostDashboardMapper
{
    CostDashboardSummaryVO selectSummary(@Param("statusList") List<String> statusList);

    List<CostDashboardProjectVO> selectProjectList(@Param("statusList") List<String> statusList, @Param("keyword") String keyword);
}
