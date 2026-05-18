package com.ruoyi.system.service.project;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.system.domain.vo.project.CostReportQuery;

public interface ICostReportService
{
    List<Map<String, Object>> preview(CostReportQuery query);

    List<Map<String, Object>> list(String type, CostReportQuery query);

    void export(CostReportQuery query, HttpServletResponse response);

    void export(String type, CostReportQuery query, HttpServletResponse response);

    Map<String, Object> options();
}
