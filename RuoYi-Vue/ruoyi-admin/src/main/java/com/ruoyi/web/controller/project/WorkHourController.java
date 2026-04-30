package com.ruoyi.web.controller.project;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.project.ProjWbsNode;
import com.ruoyi.system.domain.project.WorkHour;
import com.ruoyi.system.domain.vo.project.WorkHourFormVO;
import com.ruoyi.system.service.project.IWorkHourService;

@RestController
@RequestMapping("/cost/workHour")
public class WorkHourController extends BaseController
{
    @Autowired
    private IWorkHourService workHourService;

    @PreAuthorize("@ss.hasPermi('cost:workHour:list')")
    @GetMapping("/list")
    public TableDataInfo list(WorkHour workHour)
    {
        startPage();
        List<WorkHour> list = workHourService.selectWorkHourList(workHour);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cost:workHour:query')")
    @GetMapping("/{whId}")
    public AjaxResult getInfo(@PathVariable Long whId)
    {
        return success(workHourService.getWorkHourForm(whId));
    }

    @PreAuthorize("@ss.hasPermi('cost:workHour:add')")
    @GetMapping("/wbsNodes/{projId}")
    public AjaxResult wbsNodes(@PathVariable Long projId)
    {
        List<ProjWbsNode> list = workHourService.selectWbsNodesByProjId(projId);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('cost:workHour:add')")
    @PostMapping("/draft")
    public AjaxResult saveDraft(@RequestBody WorkHourFormVO form)
    {
        return success(workHourService.saveDraft(form, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:workHour:edit')")
    @PostMapping("/submit/{whId}")
    public AjaxResult submit(@PathVariable Long whId)
    {
        return toAjax(workHourService.submitForApproval(whId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:workHour:approve')")
    @PutMapping("/approve/{whId}")
    public AjaxResult approve(@PathVariable Long whId)
    {
        return toAjax(workHourService.approve(whId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:workHour:approve')")
    @PutMapping("/reject/{whId}")
    public AjaxResult reject(@PathVariable Long whId, @RequestBody WorkHour workHour)
    {
        return toAjax(workHourService.reject(whId, workHour.getRejectReason(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:workHour:post')")
    @PutMapping("/post/{whId}")
    public AjaxResult post(@PathVariable Long whId)
    {
        return toAjax(workHourService.postCost(whId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:workHour:remove')")
    @DeleteMapping("/{whId}")
    public AjaxResult remove(@PathVariable Long whId)
    {
        return toAjax(workHourService.deleteWorkHourById(whId));
    }
}
