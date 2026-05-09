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
import com.ruoyi.system.domain.project.ProjAlertRule;
import com.ruoyi.system.service.project.IProjAlertRuleService;

@RestController
@RequestMapping("/alert/rule")
public class AlertRuleController extends BaseController
{
    @Autowired
    private IProjAlertRuleService alertRuleService;

    @PreAuthorize("@ss.hasPermi('alert:rule:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjAlertRule rule)
    {
        startPage();
        List<ProjAlertRule> list = alertRuleService.selectAlertRuleList(rule);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('alert:rule:list')")
    @GetMapping("/{ruleId}")
    public AjaxResult getInfo(@PathVariable Long ruleId)
    {
        return success(alertRuleService.selectAlertRuleById(ruleId));
    }

    @PreAuthorize("@ss.hasPermi('alert:rule:add')")
    @PostMapping
    public AjaxResult add(@RequestBody ProjAlertRule rule)
    {
        return toAjax(alertRuleService.insertAlertRule(rule, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('alert:rule:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody ProjAlertRule rule)
    {
        return toAjax(alertRuleService.updateAlertRule(rule, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('alert:rule:edit')")
    @PutMapping("/toggle/{ruleId}/{enabled}")
    public AjaxResult toggle(@PathVariable Long ruleId, @PathVariable String enabled)
    {
        return toAjax(alertRuleService.toggleAlertRule(ruleId, enabled, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('alert:rule:remove')")
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return toAjax(alertRuleService.deleteAlertRuleByIds(ruleIds));
    }
}
