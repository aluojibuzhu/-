package com.ruoyi.system.service.project.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.vo.project.CostReportQuery;
import com.ruoyi.system.mapper.project.CostReportMapper;
import com.ruoyi.system.service.project.ICostReportService;

@Service
public class CostReportServiceImpl implements ICostReportService
{
    private static final int PREVIEW_LIMIT = 100;

    @Autowired
    private CostReportMapper costReportMapper;

    public List<Map<String, Object>> preview(CostReportQuery query)
    {
        ensureQuery(query);
        return costReportMapper.selectReportRows(query, PREVIEW_LIMIT);
    }

    public List<Map<String, Object>> list(String type, CostReportQuery query)
    {
        ensureDateRange(query);
        return selectTypedRows(type, query);
    }

    public void export(CostReportQuery query, HttpServletResponse response)
    {
        ensureQuery(query);
        writeWorkbook("成本报表", buildColumns(query), costReportMapper.selectReportRows(query, null), response);
    }

    public void export(String type, CostReportQuery query, HttpServletResponse response)
    {
        ensureDateRange(query);
        writeWorkbook(reportTitle(type), buildTypedColumns(type), selectTypedRows(type, query), response);
    }

    public Map<String, Object> options()
    {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("projects", costReportMapper.selectProjectOptions());
        options.put("categories", costReportMapper.selectCategoryOptions());
        options.put("nodes", costReportMapper.selectNodeOptions());
        options.put("approvers", costReportMapper.selectApproverOptions());
        return options;
    }

    private void ensureQuery(CostReportQuery query)
    {
        ensureDateRange(query);
        if (StringUtils.isEmpty(query.getGroupBy()))
        {
            List<String> groupBy = new ArrayList<>();
            groupBy.add("project");
            groupBy.add("category");
            query.setGroupBy(groupBy);
        }
    }

    private void ensureDateRange(CostReportQuery query)
    {
        if (query == null || StringUtils.isEmpty(query.getBeginDate()) || StringUtils.isEmpty(query.getEndDate()))
        {
            throw new ServiceException("请选择报表时间范围");
        }
    }

    private List<Map<String, Object>> selectTypedRows(String type, CostReportQuery query)
    {
        if ("projectSummary".equals(type))
        {
            return costReportMapper.selectProjectSummary(query);
        }
        if ("categoryDetail".equals(type))
        {
            return costReportMapper.selectCategoryDetail(query);
        }
        if ("postingFlow".equals(type))
        {
            return costReportMapper.selectPostingFlow(query);
        }
        if ("nodeExecution".equals(type))
        {
            return costReportMapper.selectNodeExecution(query);
        }
        throw new ServiceException("报表类型不正确");
    }

    private String reportTitle(String type)
    {
        if ("projectSummary".equals(type)) return "项目资金汇总表";
        if ("categoryDetail".equals(type)) return "科目成本明细表";
        if ("postingFlow".equals(type)) return "入账流水明细表";
        if ("nodeExecution".equals(type)) return "节点预算执行表";
        return "成本报表";
    }

    private List<ColumnDef> buildTypedColumns(String type)
    {
        if ("projectSummary".equals(type))
        {
            return Arrays.asList(
                new ColumnDef("projNo", "项目编号"),
                new ColumnDef("projName", "项目名称"),
                new ColumnDef("contractAmount", "合同金额"),
                new ColumnDef("totalBudget", "预算总额"),
                new ColumnDef("actualCost", "已执行"),
                new ColumnDef("budgetBalance", "余额"),
                new ColumnDef("profitMargin", "利润空间"),
                new ColumnDef("execRate", "执行率")
            );
        }
        if ("categoryDetail".equals(type))
        {
            return Arrays.asList(
                new ColumnDef("projName", "项目名称"),
                new ColumnDef("rootCategoryName", "一级科目"),
                new ColumnDef("childCategoryName", "二级科目"),
                new ColumnDef("budgetAmount", "预算金额"),
                new ColumnDef("actualCost", "已执行金额"),
                new ColumnDef("budgetBalance", "余额"),
                new ColumnDef("execRate", "执行率")
            );
        }
        if ("postingFlow".equals(type))
        {
            return Arrays.asList(
                new ColumnDef("billNo", "单据编号"),
                new ColumnDef("projName", "项目名称"),
                new ColumnDef("billTypeName", "单据类型"),
                new ColumnDef("categoryName", "成本科目"),
                new ColumnDef("amount", "金额"),
                new ColumnDef("postDate", "入账日期"),
                new ColumnDef("approver", "审批人")
            );
        }
        if ("nodeExecution".equals(type))
        {
            return Arrays.asList(
                new ColumnDef("projName", "项目名称"),
                new ColumnDef("nodeNo", "节点编号"),
                new ColumnDef("nodeName", "节点名称"),
                new ColumnDef("nodeBudget", "节点预算"),
                new ColumnDef("actualCost", "已执行"),
                new ColumnDef("budgetBalance", "余额"),
                new ColumnDef("execRate", "执行率"),
                new ColumnDef("planFinishDate", "计划完成日期")
            );
        }
        throw new ServiceException("报表类型不正确");
    }

    private List<ColumnDef> buildColumns(CostReportQuery query)
    {
        List<ColumnDef> columns = new ArrayList<>();
        List<String> groupBy = query.getGroupBy();
        if (groupBy.contains("project"))
        {
            columns.add(new ColumnDef("projNo", "项目编号"));
            columns.add(new ColumnDef("projName", "项目名称"));
        }
        if (groupBy.contains("category"))
        {
            columns.add(new ColumnDef("categoryName", "成本科目"));
        }
        if (groupBy.contains("node"))
        {
            columns.add(new ColumnDef("nodeName", "WBS节点"));
        }
        if (groupBy.contains("month"))
        {
            columns.add(new ColumnDef("postingMonth", "入账月份"));
        }
        columns.add(new ColumnDef("recordCount", "单据数"));
        columns.add(new ColumnDef("amount", "入账金额"));
        return columns;
    }

    private void writeWorkbook(String title, List<ColumnDef> columns, List<Map<String, Object>> rows, HttpServletResponse response)
    {
        try (Workbook workbook = new XSSFWorkbook())
        {
            Sheet sheet = workbook.createSheet(title);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++)
            {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns.get(i).label);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, Math.max(14, columns.get(i).label.length() + 4) * 256);
            }

            for (int i = 0; i < rows.size(); i++)
            {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> item = rows.get(i);
                for (int j = 0; j < columns.size(); j++)
                {
                    Object value = item.get(columns.get(j).key);
                    Cell cell = row.createCell(j);
                    if (value instanceof Number)
                    {
                        cell.setCellValue(((Number) value).doubleValue());
                    }
                    else
                    {
                        cell.setCellValue(value == null ? "" : String.valueOf(value));
                    }
                }
            }

            String fileName = URLEncoder.encode(title + ".xlsx", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
            workbook.write(response.getOutputStream());
        }
        catch (IOException e)
        {
            throw new ServiceException(title + "导出失败");
        }
    }

    private static class ColumnDef
    {
        private final String key;
        private final String label;

        private ColumnDef(String key, String label)
        {
            this.key = key;
            this.label = label;
        }
    }
}
