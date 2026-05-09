package com.ruoyi.system.service.project;

import com.ruoyi.system.domain.project.CostPostingRecord;

public interface IAlertTriggerService
{
    void checkOnPosting(Long projId, CostPostingRecord record);
    void checkDailyRules();
}
