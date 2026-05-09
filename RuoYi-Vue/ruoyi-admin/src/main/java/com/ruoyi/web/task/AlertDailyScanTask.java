package com.ruoyi.web.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.system.service.project.IAlertTriggerService;

@Component("alertDailyScanTask")
public class AlertDailyScanTask
{
    @Autowired
    private IAlertTriggerService alertTriggerService;

    public void scan()
    {
        alertTriggerService.checkDailyRules();
    }
}
