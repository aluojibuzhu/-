package com.ruoyi.system.service.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjAlertLog;

public interface IProjAlertLogService
{
    List<ProjAlertLog> selectAlertLogList(Long alertId);
    int insertAlertLog(Long alertId, String actionType, String actionBy, String actionRemark);
}
