package com.ruoyi.system.mapper.project;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.vo.project.CostReportQuery;

public interface CostReportMapper
{
    List<Map<String, Object>> selectReportRows(@Param("query") CostReportQuery query, @Param("limit") Integer limit);

    List<Map<String, Object>> selectProjectSummary(CostReportQuery query);

    List<Map<String, Object>> selectCategoryDetail(CostReportQuery query);

    List<Map<String, Object>> selectPostingFlow(CostReportQuery query);

    List<Map<String, Object>> selectNodeExecution(CostReportQuery query);

    List<Map<String, Object>> selectProjectOptions();

    List<Map<String, Object>> selectCategoryOptions();

    List<Map<String, Object>> selectNodeOptions();

    List<Map<String, Object>> selectApproverOptions();
}
