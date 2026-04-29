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
import com.ruoyi.system.domain.project.ProjInfo;
import com.ruoyi.system.domain.vo.project.ProjInfoFormVO;
import com.ruoyi.system.service.project.IProjInfoService;

@RestController
@RequestMapping("/project/info")
public class ProjInfoController extends BaseController
{
    @Autowired
    private IProjInfoService projInfoService;

    @PreAuthorize("@ss.hasPermi('project:projInfo:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjInfo projInfo)
    {
        startPage();
        List<ProjInfo> list = projInfoService.selectProjInfoList(projInfo);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('project:projInfo:query')")
    @GetMapping("/{projId}")
    public AjaxResult getInfo(@PathVariable Long projId)
    {
        return success(projInfoService.getProjForm(projId));
    }

    @PreAuthorize("@ss.hasPermi('project:projInfo:add')")
    @PostMapping("/draft")
    public AjaxResult saveDraft(@RequestBody ProjInfoFormVO form)
    {
        projInfoService.saveDraft(form, getUsername());
        return success(form.getProjInfo());
    }

    @PreAuthorize("@ss.hasPermi('project:projInfo:edit')")
    @PostMapping("/submit/{projId}")
    public AjaxResult submit(@PathVariable Long projId)
    {
        return toAjax(projInfoService.submitForApproval(projId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('project:projInfo:approve')")
    @PutMapping("/approve/{projId}")
    public AjaxResult approve(@PathVariable Long projId)
    {
        return toAjax(projInfoService.approve(projId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('project:projInfo:approve')")
    @PutMapping("/reject/{projId}")
    public AjaxResult reject(@PathVariable Long projId, @RequestBody ProjInfo projInfo)
    {
        return toAjax(projInfoService.reject(projId, projInfo.getRejectReason(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('project:projInfo:remove')")
    @DeleteMapping("/{projId}")
    public AjaxResult remove(@PathVariable Long projId)
    {
        return toAjax(projInfoService.deleteProjInfoById(projId));
    }
}
