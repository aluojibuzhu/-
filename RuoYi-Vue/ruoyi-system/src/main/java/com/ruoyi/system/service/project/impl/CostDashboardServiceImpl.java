package com.ruoyi.system.service.project.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.vo.project.CostDashboardProjectVO;
import com.ruoyi.system.domain.vo.project.CostDashboardSummaryVO;
import com.ruoyi.system.mapper.project.CostDashboardMapper;
import com.ruoyi.system.mapper.project.ProjInfoMapper;
import com.ruoyi.system.service.project.ICostDashboardService;

@Service
public class CostDashboardServiceImpl implements ICostDashboardService
{
    @Autowired
    private CostDashboardMapper costDashboardMapper;
    @Autowired
    private ProjInfoMapper projInfoMapper;

    public CostDashboardSummaryVO selectSummary(String status)
    {
        refreshProjectStatusByDate();
        CostDashboardSummaryVO summary = costDashboardMapper.selectSummary(parseStatusList(status));
        if (summary == null)
        {
            summary = new CostDashboardSummaryVO();
        }
        summary.setProjectCount(summary.getProjectCount() == null ? 0L : summary.getProjectCount());
        summary.setTotalBudget(nvl(summary.getTotalBudget()));
        summary.setActualCost(nvl(summary.getActualCost()));
        summary.setBudgetBalance(nvl(summary.getBudgetBalance()));
        summary.setContractAmount(nvl(summary.getContractAmount()));
        summary.setProfitMargin(nvl(summary.getProfitMargin()));
        summary.setMonthPostingAmount(nvl(summary.getMonthPostingAmount()));
        summary.setLastMonthPostingAmount(nvl(summary.getLastMonthPostingAmount()));
        summary.setMomRate(calcMom(summary.getMonthPostingAmount(), summary.getLastMonthPostingAmount()));
        return summary;
    }

    public List<CostDashboardProjectVO> selectProjectList(String status, String keyword)
    {
        refreshProjectStatusByDate();
        return costDashboardMapper.selectProjectList(parseStatusList(status), keyword);
    }

    private void refreshProjectStatusByDate()
    {
        projInfoMapper.refreshProjectStatusByDate("system");
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal calcMom(BigDecimal current, BigDecimal previous)
    {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0)
        {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
        }
        return current.subtract(previous).multiply(new BigDecimal("100")).divide(previous, 2, RoundingMode.HALF_UP);
    }

    private List<String> parseStatusList(String status)
    {
        if (status == null || status.trim().isEmpty())
        {
            return null;
        }
        List<String> statusList = new ArrayList<>();
        for (String item : status.split(","))
        {
            String value = item.trim();
            if (!value.isEmpty())
            {
                statusList.add(value);
            }
        }
        return statusList.isEmpty() ? null : statusList;
    }
}
