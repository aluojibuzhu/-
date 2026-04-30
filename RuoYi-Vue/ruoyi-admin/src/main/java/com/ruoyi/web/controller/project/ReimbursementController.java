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
import com.ruoyi.system.domain.project.Reimbursement;
import com.ruoyi.system.domain.vo.project.ReimbursementFormVO;
import com.ruoyi.system.service.project.IReimbursementService;

@RestController
@RequestMapping("/cost/reimbursement")
public class ReimbursementController extends BaseController
{
    @Autowired
    private IReimbursementService reimbursementService;

    @PreAuthorize("@ss.hasPermi('cost:reimbursement:list')")
    @GetMapping("/list")
    public TableDataInfo list(Reimbursement reimbursement)
    {
        startPage();
        List<Reimbursement> list = reimbursementService.selectReimbursementList(reimbursement);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cost:reimbursement:query')")
    @GetMapping("/{reimburseId}")
    public AjaxResult getInfo(@PathVariable Long reimburseId)
    {
        return success(reimbursementService.getReimbursementForm(reimburseId));
    }

    @PreAuthorize("@ss.hasPermi('cost:reimbursement:add')")
    @GetMapping("/wbsNodes/{projId}")
    public AjaxResult wbsNodes(@PathVariable Long projId)
    {
        List<ProjWbsNode> list = reimbursementService.selectWbsNodesByProjId(projId);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('cost:reimbursement:add')")
    @PostMapping("/draft")
    public AjaxResult saveDraft(@RequestBody ReimbursementFormVO form)
    {
        return success(reimbursementService.saveDraft(form, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:reimbursement:edit')")
    @PostMapping("/submit/{reimburseId}")
    public AjaxResult submit(@PathVariable Long reimburseId)
    {
        return toAjax(reimbursementService.submitForApproval(reimburseId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:reimbursement:approve')")
    @PutMapping("/approve/{reimburseId}")
    public AjaxResult approve(@PathVariable Long reimburseId)
    {
        return toAjax(reimbursementService.approve(reimburseId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:reimbursement:approve')")
    @PutMapping("/reject/{reimburseId}")
    public AjaxResult reject(@PathVariable Long reimburseId, @RequestBody Reimbursement reimbursement)
    {
        return toAjax(reimbursementService.reject(reimburseId, reimbursement.getRejectReason(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:reimbursement:post')")
    @PutMapping("/post/{reimburseId}")
    public AjaxResult post(@PathVariable Long reimburseId)
    {
        return toAjax(reimbursementService.postCost(reimburseId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:reimbursement:remove')")
    @DeleteMapping("/{reimburseId}")
    public AjaxResult remove(@PathVariable Long reimburseId)
    {
        return toAjax(reimbursementService.deleteReimbursementById(reimburseId));
    }
}
