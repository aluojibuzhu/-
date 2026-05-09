package com.ruoyi.system.service.project.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.project.ProjAlertLog;
import com.ruoyi.system.domain.project.ProjAlertRecord;
import com.ruoyi.system.mapper.project.ProjAlertRecordMapper;
import com.ruoyi.system.service.project.IProjAlertLogService;
import com.ruoyi.system.service.project.IProjAlertRecordService;

@Service
public class ProjAlertRecordServiceImpl implements IProjAlertRecordService
{
    @Autowired
    private ProjAlertRecordMapper alertRecordMapper;
    @Autowired
    private IProjAlertLogService alertLogService;

    public ProjAlertRecord selectAlertRecordById(Long alertId)
    {
        return alertRecordMapper.selectAlertRecordById(alertId);
    }

    public List<ProjAlertRecord> selectAlertRecordList(ProjAlertRecord record)
    {
        return alertRecordMapper.selectAlertRecordList(record);
    }

    public List<ProjAlertLog> selectAlertLogList(Long alertId)
    {
        return alertLogService.selectAlertLogList(alertId);
    }

    @Transactional
    public int confirm(Long alertId, String remark, String username)
    {
        int rows = updateStatus(alertId, "1", remark, username, false);
        alertLogService.insertAlertLog(alertId, "CONFIRM", username, remark);
        return rows;
    }

    @Transactional
    public int ignore(Long alertId, String remark, String username)
    {
        int rows = updateStatus(alertId, "2", remark, username, false);
        alertLogService.insertAlertLog(alertId, "IGNORE", username, remark);
        return rows;
    }

    @Transactional
    public int follow(Long alertId, String remark, String username)
    {
        int rows = updateStatus(alertId, "3", remark, username, false);
        alertLogService.insertAlertLog(alertId, "FOLLOW", username, remark);
        return rows;
    }

    @Transactional
    public int close(Long alertId, String remark, String username)
    {
        int rows = updateStatus(alertId, "4", remark, username, true);
        alertLogService.insertAlertLog(alertId, "CLOSE", username, remark);
        return rows;
    }

    public Map<String, Object> selectSummary()
    {
        return alertRecordMapper.selectAlertSummary();
    }

    public List<Map<String, Object>> selectProjectHealth()
    {
        return alertRecordMapper.selectProjectHealth();
    }

    public List<Map<String, Object>> selectBudgetTrend(Long projId, String periodType)
    {
        return alertRecordMapper.selectBudgetTrend(projId, periodType);
    }

    public List<Map<String, Object>> selectCategoryCompare(Long projId)
    {
        return alertRecordMapper.selectCategoryCompare(projId);
    }

    public List<Map<String, Object>> selectTopProjects()
    {
        return alertRecordMapper.selectTopProjects();
    }

    private int updateStatus(Long alertId, String status, String remark, String username, boolean close)
    {
        ProjAlertRecord record = new ProjAlertRecord();
        record.setAlertId(alertId);
        record.setStatus(status);
        record.setHandler(username);
        record.setHandleTime(new Date());
        record.setHandleRemark(remark);
        record.setUpdateBy(username);
        if (close)
        {
            record.setClosedBy(username);
            record.setClosedTime(new Date());
            record.setCloseRemark(remark);
        }
        return alertRecordMapper.updateAlertStatus(record);
    }
}
