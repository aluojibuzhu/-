package com.ruoyi.system.service.project.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.project.ProjAlertLog;
import com.ruoyi.system.mapper.project.ProjAlertLogMapper;
import com.ruoyi.system.service.project.IProjAlertLogService;

@Service
public class ProjAlertLogServiceImpl implements IProjAlertLogService
{
    @Autowired
    private ProjAlertLogMapper alertLogMapper;

    public List<ProjAlertLog> selectAlertLogList(Long alertId)
    {
        return alertLogMapper.selectAlertLogList(alertId);
    }

    public int insertAlertLog(Long alertId, String actionType, String actionBy, String actionRemark)
    {
        ProjAlertLog log = new ProjAlertLog();
        log.setAlertId(alertId);
        log.setActionType(actionType);
        log.setActionBy(actionBy);
        log.setActionTime(new Date());
        log.setActionRemark(actionRemark);
        return alertLogMapper.insertAlertLog(log);
    }
}
