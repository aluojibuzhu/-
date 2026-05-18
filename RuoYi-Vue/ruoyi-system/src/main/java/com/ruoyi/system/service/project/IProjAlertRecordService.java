package com.ruoyi.system.service.project;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.project.ProjAlertLog;
import com.ruoyi.system.domain.project.ProjAlertRecord;
import com.ruoyi.system.domain.vo.project.AlertProjectMetricVO;

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
    List<AlertProjectMetricVO> selectProjectHealth();
    List<Map<String, Object>> selectBudgetTrend(Long projId, String periodType, Long categoryId);
    List<Map<String, Object>> selectCategoryCompare(Long projId);
    List<AlertProjectMetricVO> selectTopProjects();
}
