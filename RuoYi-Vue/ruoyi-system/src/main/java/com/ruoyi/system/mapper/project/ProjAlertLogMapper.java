package com.ruoyi.system.mapper.project;

import java.util.List;
import com.ruoyi.system.domain.project.ProjAlertLog;

public interface ProjAlertLogMapper
{
    List<ProjAlertLog> selectAlertLogList(Long alertId);
    int insertAlertLog(ProjAlertLog log);
}
