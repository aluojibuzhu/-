package com.ruoyi.system.service.project;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.project.ProjAlertLog;
import com.ruoyi.system.domain.project.ProjAlertRecord;

public interface IProjAlertRecordService
{
    ProjAlertRecord selectAlertRecordById(Long alertId);
    List<ProjAlertRecord> selectAlertRecordList(ProjAlertRecord record);
    List<ProjAlertLog> selectAlertLogList(Long alertId);
    int confirm(Long alertId, String remark, String username);
    int ignore(Long alertId, String remark, String username);
    int follow(Long alertId, String remark, String username);
    int close(Long alertId, String remark, String username);
    Map<String, Object> selectSummary();
    List<Map<String, Object>> selectProjectHealth();
    List<Map<String, Object>> selectBudgetTrend(Long projId, String periodType);
    List<Map<String, Object>> selectCategoryCompare(Long projId);
    List<Map<String, Object>> selectTopProjects();
}
