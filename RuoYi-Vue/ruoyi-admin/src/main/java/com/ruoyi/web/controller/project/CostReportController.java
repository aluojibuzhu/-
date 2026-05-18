package com.ruoyi.web.controller.project;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.vo.project.CostReportQuery;
import com.ruoyi.system.service.project.ICostReportService;

@RestController
@RequestMapping("/cost/report")
public class CostReportController extends BaseController
{
    @Autowired
    private ICostReportService costReportService;

    @PreAuthorize("@ss.hasPermi('cost:report:export')")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(costReportService.options());
    }

    @PreAuthorize("@ss.hasPermi('cost:report:export')")
    @GetMapping("/{type}")
    public TableDataInfo list(@PathVariable String type, CostReportQuery query)
    {
        startPage();
        return getDataTable(costReportService.list(type, query));
    }

    @PreAuthorize("@ss.hasPermi('cost:report:preview')")
    @PostMapping("/preview")
    public AjaxResult preview(@RequestBody CostReportQuery query)
    {
        return success(costReportService.preview(query));
    }

    @PreAuthorize("@ss.hasPermi('cost:report:export')")
    @PostMapping("/export")
    public void export(@RequestBody CostReportQuery query, HttpServletResponse response)
    {
        costReportService.export(query, response);
    }

    @PreAuthorize("@ss.hasPermi('cost:report:export')")
    @PostMapping("/export/{type}")
    public void exportTyped(@PathVariable String type, @RequestBody CostReportQuery query, HttpServletResponse response)
    {
        costReportService.export(type, query, response);
    }
}
