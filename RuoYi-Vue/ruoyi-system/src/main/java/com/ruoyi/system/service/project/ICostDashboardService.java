package com.ruoyi.system.service.project;

import java.util.List;
import com.ruoyi.system.domain.vo.project.CostDashboardProjectVO;
import com.ruoyi.system.domain.vo.project.CostDashboardSummaryVO;

public interface ICostDashboardService
{
    CostDashboardSummaryVO selectSummary(String status);

    List<CostDashboardProjectVO> selectProjectList(String status, String keyword);
}
