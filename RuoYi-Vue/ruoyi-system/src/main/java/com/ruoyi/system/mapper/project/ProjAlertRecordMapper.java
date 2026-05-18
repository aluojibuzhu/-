package com.ruoyi.system.mapper.project;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.project.ProjAlertRecord;
import com.ruoyi.system.domain.project.ProjAlertSequence;
import com.ruoyi.system.domain.vo.project.AlertProjectMetricVO;

public interface ProjAlertRecordMapper
{
    ProjAlertRecord selectAlertRecordById(Long alertId);
    List<ProjAlertRecord> selectAlertRecordList(ProjAlertRecord record);
    int insertAlertRecord(ProjAlertRecord record);
    int updateAlertStatus(ProjAlertRecord record);
    int insertAlertNoSequence(ProjAlertSequence sequence);
    int countRecentAlert(@Param("ruleId") Long ruleId, @Param("projId") Long projId, @Param("hours") Integer hours);
    Map<String, Object> selectAlertSummary();
    List<AlertProjectMetricVO> selectProjectHealth();
    List<Map<String, Object>> selectBudgetTrend(@Param("projId") Long projId, @Param("periodType") String periodType, @Param("categoryId") Long categoryId);
    List<Map<String, Object>> selectCategoryCompare(@Param("projId") Long projId);
    List<AlertProjectMetricVO> selectTopProjects();
}
