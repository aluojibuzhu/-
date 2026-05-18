package com.ruoyi.web.controller.project;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.vo.project.CostDashboardProjectVO;
import com.ruoyi.system.service.project.ICostDashboardService;

@RestController
@RequestMapping("/cost/dashboard")
public class CostDashboardController extends BaseController
{
    @Autowired
    private ICostDashboardService costDashboardService;

    @PreAuthorize("@ss.hasPermi('cost:dashboard:view')")
    @GetMapping("/summary")
    public AjaxResult summary(String status)
    {
        return success(costDashboardService.selectSummary(status));
    }

    @PreAuthorize("@ss.hasPermi('cost:dashboard:view')")
    @GetMapping("/list")
    public TableDataInfo list(String status, String keyword)
    {
        startPage();
        List<CostDashboardProjectVO> list = costDashboardService.selectProjectList(status, keyword);
        return getDataTable(list);
    }
}
