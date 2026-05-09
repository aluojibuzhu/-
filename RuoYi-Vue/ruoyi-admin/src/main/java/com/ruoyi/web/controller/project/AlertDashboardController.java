package com.ruoyi.web.controller.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.project.IProjAlertRecordService;

@RestController
@RequestMapping("/alert/dashboard")
public class AlertDashboardController extends BaseController
{
    @Autowired
    private IProjAlertRecordService alertRecordService;

    @PreAuthorize("@ss.hasPermi('alert:dashboard:view')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(alertRecordService.selectSummary());
    }

    @PreAuthorize("@ss.hasPermi('alert:dashboard:view')")
    @GetMapping("/health")
    public AjaxResult health()
    {
        return success(alertRecordService.selectProjectHealth());
    }

    @PreAuthorize("@ss.hasPermi('alert:dashboard:view')")
    @GetMapping("/budgetTrend")
    public AjaxResult budgetTrend(Long projId, String periodType)
    {
        return success(alertRecordService.selectBudgetTrend(projId, periodType));
    }

    @PreAuthorize("@ss.hasPermi('alert:dashboard:view')")
    @GetMapping("/categoryCompare")
    public AjaxResult categoryCompare(Long projId)
    {
        return success(alertRecordService.selectCategoryCompare(projId));
    }

    @PreAuthorize("@ss.hasPermi('alert:dashboard:view')")
    @GetMapping("/top10")
    public AjaxResult top10()
    {
        return success(alertRecordService.selectTopProjects());
    }
}
