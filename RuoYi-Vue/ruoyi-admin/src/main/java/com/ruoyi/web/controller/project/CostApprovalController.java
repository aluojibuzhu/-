package com.ruoyi.web.controller.project;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.system.domain.project.CostApprovalBill;
import com.ruoyi.system.service.project.ICostApprovalService;

@RestController
@RequestMapping("/cost/approval")
public class CostApprovalController extends BaseController
{
    @Autowired
    private ICostApprovalService costApprovalService;

    @PreAuthorize("@ss.hasPermi('cost:approval:list')")
    @GetMapping("/list")
    public TableDataInfo list(CostApprovalBill bill)
    {
        startPage();
        List<CostApprovalBill> list = costApprovalService.selectApprovalBillList(bill);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cost:approval:list')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(costApprovalService.selectSummary());
    }

    @PreAuthorize("@ss.hasPermi('cost:approval:approve') and @ss.hasPermi('cost:approval:post')")
    @PutMapping("/approve/{billType}/{billId}")
    public AjaxResult approve(@PathVariable String billType, @PathVariable Long billId)
    {
        return toAjax(costApprovalService.approveAndPost(billType, billId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:approval:approve')")
    @PutMapping("/reject/{billType}/{billId}")
    public AjaxResult reject(@PathVariable String billType, @PathVariable Long billId, @RequestBody CostApprovalBill bill)
    {
        return toAjax(costApprovalService.reject(billType, billId, bill.getRemark(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('cost:approval:post')")
    @PostMapping("/post/{billType}/{billId}")
    public AjaxResult post(@PathVariable String billType, @PathVariable Long billId)
    {
        return toAjax(costApprovalService.postApproved(billType, billId, getUsername()));
    }
}
