package com.ruoyi.web.controller.project;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.project.ProjAlertRecord;
import com.ruoyi.system.service.project.IProjAlertRecordService;

@RestController
@RequestMapping("/alert/record")
public class AlertRecordController extends BaseController
{
    @Autowired
    private IProjAlertRecordService alertRecordService;

    @PreAuthorize("@ss.hasPermi('alert:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjAlertRecord record)
    {
        startPage();
        List<ProjAlertRecord> list = alertRecordService.selectAlertRecordList(record);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('alert:record:list')")
    @GetMapping("/{alertId}")
    public AjaxResult getInfo(@PathVariable Long alertId)
    {
        return success(alertRecordService.selectAlertRecordById(alertId));
    }

    @PreAuthorize("@ss.hasPermi('alert:record:list')")
    @GetMapping("/log/{alertId}")
    public AjaxResult log(@PathVariable Long alertId)
    {
        return success(alertRecordService.selectAlertLogList(alertId));
    }

    @PreAuthorize("@ss.hasPermi('alert:record:handle')")
    @PutMapping("/confirm/{alertId}")
    public AjaxResult confirm(@PathVariable Long alertId, @RequestBody ProjAlertRecord record)
    {
        return toAjax(alertRecordService.confirm(alertId, record.getHandleRemark(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('alert:record:handle')")
    @PutMapping("/ignore/{alertId}")
    public AjaxResult ignore(@PathVariable Long alertId, @RequestBody ProjAlertRecord record)
    {
        return toAjax(alertRecordService.ignore(alertId, record.getHandleRemark(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('alert:record:handle')")
    @PutMapping("/follow/{alertId}")
    public AjaxResult follow(@PathVariable Long alertId, @RequestBody ProjAlertRecord record)
    {
        return toAjax(alertRecordService.follow(alertId, record.getHandleRemark(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('alert:record:handle')")
    @PutMapping("/close/{alertId}")
    public AjaxResult close(@PathVariable Long alertId, @RequestBody ProjAlertRecord record)
    {
        return toAjax(alertRecordService.close(alertId, record.getCloseRemark(), getUsername()));
    }
}
